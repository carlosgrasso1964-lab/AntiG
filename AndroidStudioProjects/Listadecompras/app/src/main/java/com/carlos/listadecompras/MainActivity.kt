package com.carlos.listadecompras

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.carlos.listadecompras.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var produtosAdapter: ProdutoAdapter
    private var listaProdutos = mutableListOf<Produto>()

    private val cadastroLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val id = data.getIntExtra(CadastroActivity.EXTRA_PRODUTO_ID, 0)
                val nome = data.getStringExtra(CadastroActivity.EXTRA_PRODUTO_NOME) ?: ""
                val qtd = data.getIntExtra(CadastroActivity.EXTRA_PRODUTO_QTD, 0)
                val valor = data.getDoubleExtra(CadastroActivity.EXTRA_PRODUTO_VALOR, 0.0)
                val fotoByteArray = data.getByteArrayExtra(CadastroActivity.EXTRA_PRODUTO_FOTO)
                val editMode = data.getBooleanExtra(CadastroActivity.EXTRA_EDIT_MODE, false)

                if (nome.isNotEmpty()) {
                    val bitmap = fotoByteArray?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                    val produto = Produto(id, nome, qtd, valor, bitmap)

                    val db = AppDatabase.getDatabase(this)
                    if (editMode) {
                        db.produtoDao().update(produto)
                    } else {
                        db.produtoDao().insert(produto)
                    }
                    carregarProdutos()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        produtosAdapter = ProdutoAdapter(this)
        binding.listViewProdutos.adapter = produtosAdapter

        carregarProdutos()

        binding.listViewProdutos.setOnItemClickListener { _, _, position, _ ->
            val item = listaProdutos[position]
            val intent = Intent(this, CadastroActivity::class.java).apply {
                putExtra(CadastroActivity.EXTRA_EDIT_MODE, true)
                putExtra(CadastroActivity.EXTRA_PRODUTO_ID, item.id)
                putExtra(CadastroActivity.EXTRA_PRODUTO_NOME, item.nome)
                putExtra(CadastroActivity.EXTRA_PRODUTO_QTD, item.quantidade)
                putExtra(CadastroActivity.EXTRA_PRODUTO_VALOR, item.valor)
                item.foto?.let { bitmap ->
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                    putExtra(CadastroActivity.EXTRA_PRODUTO_FOTO, stream.toByteArray())
                }
            }
            cadastroLauncher.launch(intent)
        }

        binding.listViewProdutos.setOnItemLongClickListener { _, _, position, _ ->
            val item = listaProdutos[position]
            val db = AppDatabase.getDatabase(this)
            db.produtoDao().delete(item)
            carregarProdutos()
            true
        }

        binding.btnAdicionar.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            cadastroLauncher.launch(intent)
        }
    }

    private fun carregarProdutos() {
        val db = AppDatabase.getDatabase(this)
        listaProdutos.clear()
        listaProdutos.addAll(db.produtoDao().getAll())
        
        produtosAdapter.clear()
        produtosAdapter.addAll(listaProdutos)
        atualizarTotal()
    }

    private fun atualizarTotal() {
        val total = listaProdutos.sumOf { it.valor * it.quantidade }
        val formatter = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())
        binding.txtTotal.text = getString(R.string.total_label, formatter.format(total))
    }
}
