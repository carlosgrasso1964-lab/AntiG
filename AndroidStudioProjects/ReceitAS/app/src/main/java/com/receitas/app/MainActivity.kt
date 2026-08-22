package com.receitas.app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import kotlinx.coroutines.launch
import com.receitas.app.data.Receita
import com.receitas.app.databinding.ActivityMainBinding
import com.receitas.app.ui.MainViewModel
import com.receitas.app.ui.ReceitaAdapter
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ReceitaAdapter

    private var imageBytesForAdd: ByteArray? = null
    private var isFormVisible = false
    private var receitaBeingEdited: Receita? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { loadImageFromUri(it) }
    }

    private val restoreLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreDatabase(it) }
    }

    private val backupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/x-sqlite3")
    ) { uri ->
        uri?.let {
            val outputStream = contentResolver.openOutputStream(it)
            if (outputStream != null) {
                viewModel.backupDatabase(outputStream)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Caderno de Receitas"

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setupRecyclerView()
        setupForm()
        setupFab()
        observeRestore()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allReceitas.collect { receitas ->
                    adapter.submitList(receitas)
                    binding.tvEmpty.visibility =
                        if (receitas.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                }
            }
        }
    }

    private fun observeRestore() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.restoreOk.collect { ok ->
                    if (ok) {
                        viewModel.clearRestoreOk()
                        Toast.makeText(this@MainActivity,
                            "Restore conclu\u00eddo! Reabra o app.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (isFormVisible) {
            hideForm()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_backup -> {
                backupLauncher.launch("receitas_backup_${System.currentTimeMillis()}.db")
                true
            }
            R.id.action_restore -> {
                restoreLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3"))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapter = ReceitaAdapter(
            onDeleteClick = { receita -> confirmDelete(receita) },
            onEditClick = { receita -> showForm(receita) },
            onItemClick = { receita -> showDetails(receita) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.setHasFixedSize(true)
    }

    private fun setupForm() {
        binding.tvAdicionarFoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        binding.btnBuscarWeb.setOnClickListener {
            buscarNaWeb(binding.etNome.text.toString().trim())
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            if (isFormVisible) {
                salvarReceita()
            } else {
                showForm(null)
            }
        }
    }

    private fun showForm(receita: Receita?) {
        receitaBeingEdited = receita
        isFormVisible = true
        binding.layoutLista.visibility = android.view.View.GONE
        binding.layoutForm.visibility = android.view.View.VISIBLE
        binding.fab.setImageResource(R.drawable.ic_save)

        if (receita != null) {
            binding.toolbar.title = "Editar Receita"
            binding.etNome.setText(receita.nome)
            binding.etIngredientes.setText(receita.ingredientes)
            binding.etModo.setText(receita.modo)
            binding.etTempo.setText(receita.tempo)
            binding.etObs.setText(receita.obs)
            imageBytesForAdd = receita.imagem
            if (receita.imagem != null) {
                val preview = BitmapFactory.decodeByteArray(receita.imagem, 0, receita.imagem.size)
                binding.ivFotoPreview.setImageBitmap(preview)
                binding.ivFotoPreview.visibility = android.view.View.VISIBLE
            } else {
                binding.ivFotoPreview.visibility = android.view.View.GONE
                binding.ivFotoPreview.setImageDrawable(null)
            }
        } else {
            binding.toolbar.title = "Nova Receita"
            limparCampos()
        }
    }

    private fun hideForm() {
        isFormVisible = false
        receitaBeingEdited = null
        binding.layoutLista.visibility = android.view.View.VISIBLE
        binding.layoutForm.visibility = android.view.View.GONE
        binding.fab.setImageResource(R.drawable.ic_add)
        binding.toolbar.title = "Caderno de Receitas"
        limparCampos()
    }

    private fun salvarReceita() {
        val nome = binding.etNome.text.toString().trim()
        if (nome.isEmpty()) {
            binding.etNome.error = "Digite o nome da receita"
            binding.etNome.requestFocus()
            return
        }

        val edit = receitaBeingEdited
        if (edit != null) {
            viewModel.updateReceita(
                edit.copy(
                    nome = nome,
                    ingredientes = binding.etIngredientes.text.toString().trim(),
                    modo = binding.etModo.text.toString().trim(),
                    tempo = binding.etTempo.text.toString().trim(),
                    obs = binding.etObs.text.toString().trim(),
                    imagem = imageBytesForAdd
                )
            )
            Toast.makeText(this, "Receita atualizada!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addReceita(
                nome = nome,
                ingredientes = binding.etIngredientes.text.toString().trim(),
                modo = binding.etModo.text.toString().trim(),
                tempo = binding.etTempo.text.toString().trim(),
                obs = binding.etObs.text.toString().trim(),
                imagem = imageBytesForAdd
            )
            Toast.makeText(this, "Receita salva!", Toast.LENGTH_SHORT).show()
        }
        hideForm()
    }

    private fun limparCampos() {
        binding.etNome.text?.clear()
        binding.etIngredientes.text?.clear()
        binding.etModo.text?.clear()
        binding.etTempo.text?.clear()
        binding.etObs.text?.clear()
        imageBytesForAdd = null
        binding.ivFotoPreview.visibility = android.view.View.GONE
        binding.ivFotoPreview.setImageDrawable(null)
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                imageBytesForAdd = stream.toByteArray()
                stream.close()

                val previewBitmap = BitmapFactory.decodeByteArray(
                    imageBytesForAdd, 0, imageBytesForAdd!!.size
                )
                binding.ivFotoPreview.setImageBitmap(previewBitmap)
                binding.ivFotoPreview.visibility = android.view.View.VISIBLE
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDelete(receita: Receita) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Receita")
            .setMessage("Tem certeza que deseja excluir \"${receita.nome}\"?")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.deleteReceita(receita)
                Toast.makeText(this, "Receita exclu\u00edda", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDetails(receita: Receita) {
        val dialogBinding = com.receitas.app.databinding.DialogDetalhesReceitaBinding.inflate(layoutInflater)

        dialogBinding.tvIngredientes.text = receita.ingredientes
        dialogBinding.tvModo.text = receita.modo
        dialogBinding.tvTempo.text = "\u23F1 ${receita.tempo}"
        dialogBinding.tvObs.text = receita.obs

        if (receita.imagem != null) {
            dialogBinding.ivReceita.load(receita.imagem)
            dialogBinding.ivReceita.visibility = android.view.View.VISIBLE
        } else {
            dialogBinding.ivReceita.visibility = android.view.View.GONE
        }

        AlertDialog.Builder(this)
            .setTitle(receita.nome)
            .setView(dialogBinding.root)
            .setPositiveButton("Fechar", null)
            .setNeutralButton("Buscar na Web") { _, _ -> buscarNaWeb(receita.nome) }
            .setNegativeButton("Editar") { _, _ -> showForm(receita) }
            .show()
    }

    private fun buscarNaWeb(query: String) {
        if (query.isEmpty()) {
            Toast.makeText(this, "Digite o nome da receita primeiro!", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/search?q=receita+$query")
        )
        startActivity(intent)
    }
}
