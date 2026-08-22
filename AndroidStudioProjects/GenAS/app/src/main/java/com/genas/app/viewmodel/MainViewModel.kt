package com.genas.app.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.genas.app.GenASApp
import com.genas.app.data.AppDatabase
import com.genas.app.data.Partnership
import com.genas.app.data.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class PersonNode(
    val person: Person,
    val spouses: List<Person> = emptyList(),
    val children: List<PersonNode> = emptyList()
)

data class TreeState(
    val familyId: Long = 0,
    val familyName: String = "",
    val rootNodes: List<PersonNode> = emptyList(),
    val isLoading: Boolean = true,
    val hasFamily: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as GenASApp).repository

    private val _state = MutableStateFlow(TreeState())
    val state: StateFlow<TreeState> = _state.asStateFlow()

    private val _people = MutableStateFlow<List<Person>>(emptyList())
    val people: StateFlow<List<Person>> = _people.asStateFlow()

    private val _backupFile = MutableStateFlow<File?>(null)
    val backupFile: StateFlow<File?> = _backupFile.asStateFlow()

    private var familyNameCache: String = ""

    init {
        loadFamily()
    }

    private fun loadFamily() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val family = repository.getFirstFamily()
            if (family != null) {
                familyNameCache = family.name
                _state.value = _state.value.copy(
                    hasFamily = true,
                    familyId = family.id,
                    familyName = family.name,
                    isLoading = false
                )
                loadPeople(family.id)
            } else {
                _state.value = _state.value.copy(
                    hasFamily = false,
                    isLoading = false
                )
            }
        }
    }

    fun createFamily(name: String) {
        viewModelScope.launch {
            val id = repository.createFamily(name)
            familyNameCache = name
            _state.value = _state.value.copy(
                hasFamily = true,
                familyId = id,
                familyName = name
            )
            loadPeople(id)
        }
    }

    fun updateFamilyName(newName: String) {
        viewModelScope.launch {
            val familyId = _state.value.familyId
            if (familyId != 0L) {
                repository.updateFamilyName(familyId, newName)
                familyNameCache = newName
                _state.value = _state.value.copy(familyName = newName)
            }
        }
    }

    private fun loadPeople(familyId: Long) {
        viewModelScope.launch {
            repository.observePeople(familyId).collect { peopleList ->
                val partnerships = repository.getPartnerships(familyId)
                _people.value = peopleList
                _state.value = _state.value.copy(
                    rootNodes = buildTree(peopleList, partnerships)
                )
            }
        }
    }

    private fun buildTree(
        people: List<Person>,
        partnerships: List<Partnership>
    ): List<PersonNode> {
        val roots = people.filter { it.parentId == null }
        val nodes = mutableListOf<PersonNode>()
        val processedIds = mutableSetOf<Long>()

        // Função recursiva interna para construir a árvore e rastrear IDs já incluídos
        fun buildNode(person: Person): PersonNode {
            processedIds.add(person.id)
            
            val spouses = partnerships
                .filter { it.person1Id == person.id || it.person2Id == person.id }
                .mapNotNull { partnership ->
                    val spouseId = if (partnership.person1Id == person.id)
                        partnership.person2Id else partnership.person1Id
                    people.find { it.id == spouseId }
                }
            
            // Marca os cônjuges como processados para que não iniciem árvores redundantes no topo
            spouses.forEach { processedIds.add(it.id) }
            
            // Busca filhos vinculados a qualquer um dos cônjuges
            val children = people.filter { p ->
                (p.parentId == person.id || spouses.any { s -> p.parentId == s.id }) &&
                !processedIds.contains(p.id) // Evita ciclos e duplicações infinitas
            }
            
            return PersonNode(
                person = person,
                spouses = spouses,
                children = children.distinctBy { it.id }.map { buildNode(it) }
            )
        }

        for (root in roots) {
            // Se esta pessoa já apareceu como cônjuge ou descendente em outra árvore, pula
            if (processedIds.contains(root.id)) continue
            nodes.add(buildNode(root))
        }
        return nodes
    }

    fun addPerson(name: String, role: String, parentId: Long?, partnerOfId: Long?) {
        viewModelScope.launch {
            val familyId = _state.value.familyId
            
            // O usuário agora verá todos os registros raiz na tela, facilitando a identificação de duplicatas.

            val person = Person(
                familyId = familyId,
                name = name,
                role = role.ifBlank { if (partnerOfId != null) "Cônjuge" else "Membro" },
                parentId = parentId
            )
            val newPersonId = repository.addPerson(person)

            if (partnerOfId != null) {
                repository.addPartnership(partnerOfId, newPersonId)
            }

            refreshTree(familyId)
        }
    }

    private suspend fun refreshTree(familyId: Long) {
        val people = repository.getPeopleByFamily(familyId)
        val partnerships = repository.getPartnerships(familyId)
        _people.value = people
        _state.value = _state.value.copy(
            rootNodes = buildTree(people, partnerships)
        )
    }

    fun updatePerson(id: Long, newName: String, newRole: String, newParentId: Long?) {
        viewModelScope.launch {
            val person = repository.getPersonById(id) ?: return@launch
            repository.updatePerson(person.copy(
                name = newName.ifBlank { person.name },
                role = newRole.ifBlank { person.role },
                parentId = newParentId
            ))
            refreshTree(person.familyId)
        }
    }

    fun deletePerson(id: Long) {
        viewModelScope.launch {
            val person = repository.getPersonById(id) ?: return@launch
            repository.deletePerson(person)
        }
    }

    fun deleteAllAndRestart() {
        viewModelScope.launch {
            val familyId = _state.value.familyId
            if (familyId != 0L) {
                repository.deleteFamilyAndPeople(familyId)
            }
            _state.value = TreeState(isLoading = false, hasFamily = false)
            familyNameCache = ""
        }
    }

    fun backupDatabase(context: android.content.Context) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            _backupFile.value = null
            try {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(context)
                    val sqldb = db.openHelper.writableDatabase
                    sqldb.query(androidx.sqlite.db.SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)")).close()

                    val dbFile = context.getDatabasePath("genas_database")
                    if (!dbFile.exists()) {
                        throw IllegalStateException("Banco de dados não encontrado em ${dbFile.absolutePath}")
                    }
                    android.util.Log.d("GenAS", "DB size: ${dbFile.length()} bytes")

                    val safeFamilyName = familyNameCache.replace(' ', '_')
                    val name = "genas_backup_${safeFamilyName}_${System.currentTimeMillis()}.db"
                    val outFile = File(context.cacheDir, name)

                    dbFile.inputStream().use { inp ->
                        outFile.outputStream().use { out ->
                            val bytes = inp.copyTo(out)
                            out.flush()
                            android.util.Log.d("GenAS", "Copiados $bytes bytes para cache")
                        }
                    }

                    if (outFile.length() == 0L) {
                        throw IllegalStateException("Arquivo de backup ficou vazio")
                    }
                    _backupFile.value = outFile
                }
            } catch (e: Exception) {
                android.util.Log.e("GenAS", "Backup failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun clearBackupFile() {
        _backupFile.value = null
    }

    fun restoreDatabase(context: android.content.Context, inputUri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                withContext(Dispatchers.IO) {
                    AppDatabase.closeAndReset()

                    val dbFile = context.getDatabasePath("genas_database")
                    File(dbFile.absolutePath + "-wal").delete()
                    File(dbFile.absolutePath + "-shm").delete()

                    val inputStream = context.contentResolver.openInputStream(inputUri)
                    if (inputStream == null) {
                        throw IllegalStateException("Não foi possível ler o arquivo de backup")
                    }
                    inputStream.use { inp ->
                        dbFile.outputStream().use { out ->
                            inp.copyTo(out)
                        }
                    }
                }
                val packageCtx = context.applicationContext
                val intent = packageCtx.packageManager.getLaunchIntentForPackage(packageCtx.packageName!!)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Erro no restore: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun exportTreeAsText(): String {
        val nodes = _state.value.rootNodes
        if (nodes.isEmpty()) return ""
        val sb = StringBuilder()
        sb.appendLine("╔═══════════════════════════════════")
        sb.appendLine("║  Árvore Genealógica - ${_state.value.familyName}")
        sb.appendLine("╚═══════════════════════════════════")
        sb.appendLine()
        nodes.forEach { node ->
            appendNodeToText(sb, node, "", true)
            sb.appendLine()
        }
        return sb.toString()
    }

    private fun appendNodeToText(sb: StringBuilder, node: PersonNode, prefix: String, isLast: Boolean) {
        val connector = if (isLast) "└── " else "├── "
        sb.append(prefix).append(connector).append(node.person.name)
        if (node.person.role.isNotEmpty()) {
            sb.append(" (").append(node.person.role).append(")")
        }
        sb.appendLine()

        for (spouse in node.spouses) {
            val spouseConnector = if (node.children.isEmpty() && node.spouses.last() == spouse) "    └── " else "    ├── "
            sb.append(prefix).append(spouseConnector)
                .append("♥ ").append(spouse.name)
                .append(" (").append(spouse.role).append(")")
                .appendLine()
        }

        val newPrefix = if (isLast) "    " else "│   "
        val children = node.children
        children.forEachIndexed { index, child ->
            appendNodeToText(sb, child, prefix + newPrefix, index == children.size - 1)
        }
    }
}
