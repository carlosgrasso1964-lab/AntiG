package com.genas.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genas.app.data.Person
import com.genas.app.ui.tree.FamilyTreeView
import com.genas.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        !state.hasFamily -> {
            FamilySetupScreen(
                onConfirm = { name -> viewModel.createFamily(name) }
            )
        }

        else -> {
            TreeMainScreen(
                familyName = state.familyName,
                rootNodes = state.rootNodes,
                people = viewModel.people.collectAsState().value,
                onAddPerson = { name, role, parentId, partnerOfId ->
                    viewModel.addPerson(name, role, parentId, partnerOfId)
                },
                onEditPerson = { id, name, role, parentId ->
                    viewModel.updatePerson(id, name, role, parentId)
                },
                onDeletePerson = { id ->
                    viewModel.deletePerson(id)
                },
                onUpdateFamilyName = { name ->
                    viewModel.updateFamilyName(name)
                },
                onDeleteAll = {
                    viewModel.deleteAllAndRestart()
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun FamilySetupScreen(onConfirm: (String) -> Unit) {
    var familyName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "\uD83C\uDF33", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Bem-vindo ao GenAS",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Crie sua \u00e1rvore geneal\u00f3gica",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = familyName,
                    onValueChange = {
                        familyName = it
                        error = false
                    },
                    label = { Text("Nome da Fam\u00edlia") },
                    placeholder = { Text("Ex: Grasso, Silva, Rodrigues") },
                    singleLine = true,
                    isError = error,
                    supportingText = if (error) {
                        { Text("Por favor, informe um nome") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (familyName.isBlank()) {
                            error = true
                        } else {
                            onConfirm(familyName.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Come\u00e7ar", fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TreeMainScreen(
    familyName: String,
    rootNodes: List<com.genas.app.viewmodel.PersonNode>,
    people: List<Person>,
    onAddPerson: (String, String, Long?, Long?) -> Unit,
    onEditPerson: (Long, String, String, Long?) -> Unit,
    onDeletePerson: (Long) -> Unit,
    onUpdateFamilyName: (String) -> Unit,
    onDeleteAll: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Person?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMaintenanceDialog by remember { mutableStateOf(false) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreDatabase(context, it) }
    }

    val backupFile by viewModel.backupFile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Fam\u00edlia $familyName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (rootNodes.isNotEmpty()) {
                            Text(
                                text = "\u00c1rvore Geneal\u00f3gica",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Renomear",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { showMaintenanceDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Manutenção",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Excluir tudo",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar pessoa"
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (rootNodes.isEmpty()) {
                EmptyTreeMessage(familyName)
            } else {
                FamilyTreeView(
                    rootNodes = rootNodes,
                    onEdit = { person -> showEditDialog = person },
                    onDelete = { onDeletePerson(it.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddPersonDialog(
            people = people,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, role, parentId, partnerOfId ->
                onAddPerson(name, role, parentId, partnerOfId)
                showAddDialog = false
            }
        )
    }

    showEditDialog?.let { person ->
        EditPersonDialog(
            person = person,
            people = people.filter { it.id != person.id },
            onDismiss = { showEditDialog = null },
            onConfirm = { name, role, parentId ->
                onEditPerson(person.id, name, role, parentId)
                showEditDialog = null
            }
        )
    }

    if (showRenameDialog) {
        RenameFamilyDialog(
            currentName = familyName,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                onUpdateFamilyName(newName)
                showRenameDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir tudo?") },
            text = {
                Text(
                    "Toda a \u00e1rvore e dados ser\u00e3o perdidos. " +
                            "Esta a\u00e7\u00e3o n\u00e3o pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAll()
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showMaintenanceDialog) {
        MaintenanceDialog(
            onDismiss = { showMaintenanceDialog = false },
            onBackup = {
                showMaintenanceDialog = false
                viewModel.backupDatabase(context)
            },
            onRestore = {
                showMaintenanceDialog = false
                restoreLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3"))
            },
            onExport = {
                showMaintenanceDialog = false
                val treeText = viewModel.exportTreeAsText()
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, treeText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Compartilhar árvore"))
            }
        )
    }

    if (backupFile != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearBackupFile() },
            title = { Text("Backup concluído!") },
            text = {
                Text("Arquivo salvo no cache do app.\n" +
                     "Use \"Compartilhar\" para salvar em outro local.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = FileProvider.getUriForFile(context,
                        "${context.packageName}.fileprovider", backupFile!!)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/octet-stream"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Salvar backup em..."))
                    viewModel.clearBackupFile()
                }) {
                    Text("Compartilhar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearBackupFile() }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
private fun MaintenanceDialog(
    onDismiss: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onExport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Manutenção do Banco de Dados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Escolha uma opção:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Button(
                    onClick = onBackup,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("💾 Fazer Backup", fontSize = 16.sp)
                }
                Button(
                    onClick = onRestore,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("📂 Restaurar Backup", fontSize = 16.sp)
                }
                Button(
                    onClick = onExport,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("📤 Exportar árvore (texto)", fontSize = 16.sp)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
private fun EmptyTreeMessage(familyName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "\uD83C\uDF31", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Fam\u00edlia $familyName",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sua \u00e1rvore est\u00e1 vazia",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Toque no bot\u00e3o + para adicionar a primeira pessoa",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AddPersonDialog(
    people: List<Person>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?, Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var relationType by remember { mutableStateOf("child") }
    var selectedParentId by remember { mutableStateOf<Long?>(null) }
    var selectedPartnerId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar pessoa") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    placeholder = { Text("Ex: Ant\u00f4nio Grasso") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Parentesco") },
                    placeholder = { Text("Ex: Filho, Av\u00f4") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tipo de rela\u00e7\u00e3o:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { relationType = "child" },
                        colors = if (relationType == "child")
                            ButtonDefaults.buttonColors()
                        else
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Filho(a) de...", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { relationType = "spouse" },
                        colors = if (relationType == "spouse")
                            ButtonDefaults.buttonColors()
                        else
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("C\u00f4njuge de...", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (relationType == "child") {
                    PersonSelector(
                        label = "Conectar a quem?",
                        people = people,
                        selectedId = selectedParentId,
                        onSelect = { selectedParentId = it }
                    )
                    if (selectedParentId == null) {
                        Text(
                            text = "Ser\u00e1 a raiz da \u00e1rvore",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    PersonSelector(
                        label = "C\u00f4njuge de:",
                        people = people,
                        selectedId = selectedPartnerId,
                        onSelect = { selectedPartnerId = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val parentId = if (relationType == "child") selectedParentId else null
                        val partnerOfId = if (relationType == "spouse") selectedPartnerId else null
                        onConfirm(name.trim(), role.trim(), parentId, partnerOfId)
                    }
                }
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun PersonSelector(
    label: String,
    people: List<Person>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (people.isEmpty()) {
            Text(
                text = "Nenhuma pessoa cadastrada",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)
            ) {
                item(key = "none") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(null) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = if (selectedId == null) "\u25C9" else "\u25CB"
                        Text(text = icon, modifier = Modifier.padding(end = 8.dp))
                        Text("(Ningu\u00e9m - ser\u00e1 raiz)")
                    }
                }
                itemsIndexed(people, key = { _, p -> p.id }) { _, person ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(person.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = if (selectedId == person.id) "\u25C9" else "\u25CB"
                        Text(text = icon, modifier = Modifier.padding(end = 8.dp))
                        Column {
                            Text(
                                text = person.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (person.role.isNotEmpty()) {
                                Text(
                                    text = person.role,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditPersonDialog(
    person: Person,
    people: List<Person>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?) -> Unit
) {
    var name by remember { mutableStateOf(person.name) }
    var role by remember { mutableStateOf(person.role) }
    var selectedParentId by remember { mutableStateOf<Long?>(person.parentId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar pessoa") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Parentesco") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                PersonSelector(
                    label = "Filho(a) de:",
                    people = people,
                    selectedId = selectedParentId,
                    onSelect = { selectedParentId = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, role, selectedParentId) }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun RenameFamilyDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renomear fam\u00edlia") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome da Fam\u00edlia") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) onConfirm(name.trim())
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
