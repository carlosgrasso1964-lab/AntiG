package com.carlos.indextracker.activities

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.carlos.indextracker.R
import com.carlos.indextracker.database.IndexTrackerDbHelper
import com.carlos.indextracker.database.Indice
import com.carlos.indextracker.databinding.ActivityIndexFormBinding
import com.carlos.indextracker.ui.Formato
import com.google.gson.Gson

class IndexFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndexFormBinding
    private lateinit var db: IndexTrackerDbHelper
    private var indiceEditando: Indice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndexFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = IndexTrackerDbHelper(this)

        indiceEditando = intent.getStringExtra(MainActivity.EXTRA_INDICE)?.let {
            runCatching { Gson().fromJson(it, Indice::class.java) }.getOrNull()
        }

        configurarSpinner()
        preencherCampos()
        configurarSeletorCor()

        binding.btnSalvar.setOnClickListener { salvar() }
        binding.btnCancelar.setOnClickListener { finish() }
    }

    private fun configurarSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.tipos_indice, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTipo.adapter = adapter
        binding.spTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                atualizarVisibilidade()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun preencherCampos() {
        val indice = indiceEditando
        binding.tvTitle.text = getString(
            if (indice != null) R.string.form_editar_titulo else R.string.form_novo_titulo
        )

        if (indice == null) {
            binding.spTipo.setSelection(0)
            binding.etSufixo.setText(" R$")
            binding.etCasasDecimais.setText("2")
            aplicarCor(COR_PADRAO)
            binding.etValorPadrao.setText("")
            return
        }

        binding.etNome.setText(indice.nome)
        binding.spTipo.setSelection(
            when (indice.fonte) {
                Indice.FONTE_MOEDA -> 1
                Indice.FONTE_CUSTOM -> 2
                else -> 0
            }
        )
        binding.etCodigo.setText(indice.codigo)
        binding.etApiUrl.setText(indice.apiUrl)
        binding.etJsonPath.setText(indice.jsonPath)
        binding.etSufixo.setText(indice.sufixo)
        binding.etCasasDecimais.setText(indice.casasDecimais.toString())
        aplicarCor(indice.cor)
        binding.etValorPadrao.setText(Formato.valor(indice.valorPadrao, indice.casasDecimais))
    }

    // ---------- Seletor de cor ----------

    private fun configurarSeletorCor() {
        val abrir = { abrirPaletaCores() }
        binding.etCor.setOnClickListener { abrir() }
        binding.tilCor.setOnClickListener { abrir() }
        binding.viewCorPrevia.setOnClickListener { abrir() }
    }

    private fun aplicarCor(hex: String) {
        val cor = Formato.parseCor(hex, Formato.parseCor(COR_PADRAO))
        binding.etCor.setText(String.format("#%06X", 0xFFFFFF and cor))
        binding.viewCorPrevia.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(cor)
            setStroke(dp(1), ContextCompat.getColor(this@IndexFormActivity, R.color.divider))
        }
    }

    private fun abrirPaletaCores() {
        val grade = GridLayout(this).apply {
            columnCount = 4
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }

        val dialogo = AlertDialog.Builder(this)
            .setTitle(R.string.form_escolher_cor)
            .setView(grade)
            .setNegativeButton(R.string.cancelar, null)
            .create()

        val corAtual = binding.etCor.text?.toString()?.trim()?.lowercase()
        val tamanho = dp(46)
        val margem = dp(6)

        CORES_DISPONIVEIS.forEach { hex ->
            val selecionada = hex.equals(corAtual, ignoreCase = true)
            val amostra = View(this).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Formato.parseCor(hex))
                    setStroke(
                        dp(if (selecionada) 3 else 1),
                        ContextCompat.getColor(
                            this@IndexFormActivity,
                            if (selecionada) R.color.text_primary else R.color.divider
                        )
                    )
                }
                contentDescription = hex
                setOnClickListener {
                    aplicarCor(hex)
                    dialogo.dismiss()
                }
            }
            val lp = GridLayout.LayoutParams().apply {
                width = tamanho
                height = tamanho
                setMargins(margem, margem, margem, margem)
            }
            grade.addView(amostra, lp)
        }

        dialogo.show()
    }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).toInt()

    private fun atualizarVisibilidade() {
        val tipo = tipoSelecionado()
        binding.tilCodigo.isVisible = tipo == Indice.FONTE_MOEDA
        binding.tilApiUrl.isVisible = tipo == Indice.FONTE_CUSTOM
        binding.tilJsonPath.isVisible = tipo == Indice.FONTE_CUSTOM
    }

    private fun tipoSelecionado(): String = when (binding.spTipo.selectedItemPosition) {
        1 -> Indice.FONTE_MOEDA
        2 -> Indice.FONTE_CUSTOM
        else -> Indice.FONTE_MANUAL
    }

    private fun salvar() {
        val nome = binding.etNome.text?.toString()?.trim().orEmpty()
        if (nome.isEmpty()) {
            Toast.makeText(this, R.string.preencha_nome, Toast.LENGTH_SHORT).show()
            return
        }

        val tipo = tipoSelecionado()
        val codigo = binding.etCodigo.text?.toString()?.trim()?.uppercase().orEmpty()
        val apiUrl = binding.etApiUrl.text?.toString()?.trim().orEmpty()
        val jsonPath = binding.etJsonPath.text?.toString()?.trim().orEmpty()

        if (tipo == Indice.FONTE_MOEDA && codigo.isEmpty()) {
            Toast.makeText(this, R.string.preencha_codigo, Toast.LENGTH_SHORT).show()
            return
        }
        if (tipo == Indice.FONTE_CUSTOM && apiUrl.isEmpty()) {
            Toast.makeText(this, R.string.preencha_url, Toast.LENGTH_SHORT).show()
            return
        }

        val valorPadrao = Formato.parseNumero(binding.etValorPadrao.text?.toString())
        if (valorPadrao == null || valorPadrao < 0) {
            Toast.makeText(this, R.string.preencha_valor, Toast.LENGTH_SHORT).show()
            return
        }

        val casas = (binding.etCasasDecimais.text?.toString()?.toIntOrNull() ?: 2).coerceIn(0, 6)
        val sufixo = binding.etSufixo.text?.toString()?.trim().orEmpty()
        val cor = binding.etCor.text?.toString()?.trim()?.let {
            if (it.startsWith("#")) it else "#$it"
        }?.takeIf { it.length == 7 } ?: "#3a6fbf"

        val indice = Indice(
            id = indiceEditando?.id ?: 0,
            nome = nome,
            fonte = tipo,
            codigo = if (tipo == Indice.FONTE_MOEDA) codigo else "",
            apiUrl = if (tipo == Indice.FONTE_CUSTOM) apiUrl else "",
            jsonPath = if (tipo == Indice.FONTE_CUSTOM) jsonPath else "",
            sufixo = if (sufixo.isEmpty()) " R$" else " $sufixo",
            cor = cor,
            casasDecimais = casas,
            valorPadrao = valorPadrao,
            ativo = indiceEditando?.ativo ?: true,
            ordem = indiceEditando?.ordem ?: db.proximaOrdem()
        )

        if (indiceEditando != null) db.updateIndice(indice) else db.insertIndice(indice)

        Toast.makeText(this, R.string.salvo, Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    companion object {
        private const val COR_PADRAO = "#3a6fbf"

        private val CORES_DISPONIVEIS = listOf(
            "#1f4f8a", "#2f6fd0", "#3a6fbf", "#00838f",
            "#00897b", "#2e7d32", "#6d4c41", "#f9a825",
            "#f57c00", "#e64a19", "#c62828", "#ad1457",
            "#8e24aa", "#5e35b1", "#3949ab", "#546e7a",
            "#00a6a6", "#7cb342", "#d81b60", "#43a047",
            "#fb8c00", "#8d6e63", "#424242", "#795548"
        )
    }
}
