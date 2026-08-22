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
import androidx.core.view.updatePadding
import com.carlos.listadecompras.databinding.ActivityCadastroBinding
import java.io.ByteArrayOutputStream

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private var imageBitmap: Bitmap? = null

    companion object {
        const val EXTRA_PRODUTO_ID = "com.carlos.listadecompras.PRODUTO_ID"
        const val EXTRA_PRODUTO_NOME = "com.carlos.listadecompras.PRODUTO_NOME"
        const val EXTRA_PRODUTO_QTD = "com.carlos.listadecompras.PRODUTO_QTD"
        const val EXTRA_PRODUTO_VALOR = "com.carlos.listadecompras.PRODUTO_VALOR"
        const val EXTRA_PRODUTO_FOTO = "com.carlos.listadecompras.PRODUTO_FOTO"
        const val EXTRA_EDIT_MODE = "com.carlos.listadecompras.EDIT_MODE"
        
        private const val MAX_IMAGE_SIZE = 500 // Tamanho máximo para evitar erro de Intent
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            contentResolver.openInputStream(it)?.use { inputStream ->
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                imageBitmap = redimensionarBitmap(originalBitmap, MAX_IMAGE_SIZE)
                binding.imgFotoProduto.setImageBitmap(imageBitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

        binding.imgFotoProduto.setOnClickListener {
            abrirGaleria()
        }

        val editMode = intent.getBooleanExtra(EXTRA_EDIT_MODE, false)
        val produtoId = intent.getIntExtra(EXTRA_PRODUTO_ID, 0)

        if (editMode) {
            binding.btnInserir.text = getString(R.string.btn_atualizar)
            binding.txtProduto.setText(intent.getStringExtra(EXTRA_PRODUTO_NOME) ?: "")
            binding.txtQtd.setText(intent.getIntExtra(EXTRA_PRODUTO_QTD, 0).toString())
            binding.txtValor.setText(intent.getDoubleExtra(EXTRA_PRODUTO_VALOR, 0.0).toString())
            intent.getByteArrayExtra(EXTRA_PRODUTO_FOTO)?.let { bytes ->
                imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding.imgFotoProduto.setImageBitmap(imageBitmap)
            }
        }

        binding.btnInserir.setOnClickListener {
            val nomeProduto = binding.txtProduto.text.toString()
            val qtdStr = binding.txtQtd.text.toString()
            val valorStr = binding.txtValor.text.toString()

            var hasError = false

            if (nomeProduto.isBlank()) {
                binding.txtProduto.error = "Preencha o nome do produto"
                hasError = true
            }

            if (qtdStr.isBlank()) {
                binding.txtQtd.error = "Preencha a quantidade"
                hasError = true
            }

            if (valorStr.isBlank()) {
                binding.txtValor.error = "Preencha o valor"
                hasError = true
            }

            if (!hasError) {
                val resultadoIntent = Intent()
                resultadoIntent.putExtra(EXTRA_PRODUTO_NOME, nomeProduto)
                resultadoIntent.putExtra(EXTRA_PRODUTO_QTD, qtdStr.toIntOrNull() ?: 0)
                resultadoIntent.putExtra(EXTRA_PRODUTO_VALOR, valorStr.toDoubleOrNull() ?: 0.0)
                resultadoIntent.putExtra(EXTRA_EDIT_MODE, editMode)
                resultadoIntent.putExtra(EXTRA_PRODUTO_ID, produtoId)
                
                imageBitmap?.let { bitmap ->
                    val stream = ByteArrayOutputStream()
                    // Usar JPEG com compressão para garantir que caiba no Intent
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                    resultadoIntent.putExtra(EXTRA_PRODUTO_FOTO, stream.toByteArray())
                }
                
                setResult(RESULT_OK, resultadoIntent)
                finish()
            }
        }
    }

    private fun redimensionarBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun abrirGaleria() {
        getContent.launch("image/*")
    }
}