package com.carlos.indextracker.activities

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlos.indextracker.R
import com.carlos.indextracker.database.BackupDados
import com.carlos.indextracker.database.IndexTrackerDbHelper
import com.carlos.indextracker.database.Indice
import com.carlos.indextracker.databinding.ActivityMainBinding
import com.carlos.indextracker.databinding.DialogGerenciarBinding
import com.carlos.indextracker.databinding.ItemGraficoBinding
import com.carlos.indextracker.databinding.ItemIndiceBinding
import com.carlos.indextracker.network.CotacaoService
import com.carlos.indextracker.ui.Formato
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: IndexTrackerDbHelper
    private var indices: List<Indice> = emptyList()

    private val abrirFormulario = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) carregar()
    }

    private val criarBackup = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) exportarBackup(uri) }

    private val abrirBackup = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) prepararImportacao(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = IndexTrackerDbHelper(this)

        binding.btnAdd.setOnClickListener { editarIndice(null) }
        binding.btnManage.setOnClickListener { abrirGerenciamento() }
        binding.btnRefresh.setOnClickListener { atualizarCotas() }
        binding.btnBackup.setOnClickListener {
            criarBackup.launch("indextracker-backup-${LocalDate.now()}.json")
        }
        binding.btnRestore.setOnClickListener {
            abrirBackup.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
        }
        binding.btnReset.setOnClickListener { confirmarReinicio() }

        carregar()
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) carregar()
    }

    // ---------- Carregamento e renderização ----------

    private fun carregar() {
        indices = db.listIndices(onlyActive = true)
        binding.tvEmpty.isVisible = indices.isEmpty()

        renderizarCards()
        renderizarGraficos()

        val ultima = db.ultimaData()
        binding.tvLastUpdate.text = if (ultima == null) {
            getString(R.string.sem_registros)
        } else {
            getString(R.string.ultima_atualizacao, Formato.dataBR(ultima))
        }
        binding.tvStatus.text = if (ultima == null) {
            getString(R.string.sem_registros)
        } else {
            getString(R.string.status_atualizado)
        }
    }

    private fun renderizarCards() {
        binding.gridQuotes.removeAllViews()

        indices.forEachIndexed { posicao, indice ->
            val item = ItemIndiceBinding.inflate(layoutInflater, binding.gridQuotes, false)
            val historico = db.getCotacoes(indice.id, limite = 2)
            val valorAtual = historico.lastOrNull()?.valor ?: indice.valorPadrao
            val valorAnterior = if (historico.size >= 2) historico[historico.size - 2].valor else valorAtual
            val variacao = if (valorAnterior != 0.0) {
                (valorAtual - valorAnterior) / valorAnterior * 100
            } else {
                0.0
            }

            item.tvNome.text = indice.nome
            item.tvBadge.text = getString(
                if (indice.manual) R.string.badge_manual else R.string.badge_api
            )
            if (indice.manual) {
                item.tvBadge.setTextColor(ContextCompatColor(R.color.manual_badge_text))
                item.tvBadge.background = GradientDrawable().apply {
                    cornerRadius = resources.displayMetrics.density * 6
                    setColor(ContextCompatColor(R.color.manual_badge_bg))
                }
            }

            item.tvValor.text = "${Formato.valor(valorAtual, indice.casasDecimais)}${indice.sufixo}"
            item.tvVariacao.text = Formato.percentual(variacao)
            item.tvVariacao.setTextColor(
                when {
                    variacao > 0 -> ContextCompatColor(R.color.positive)
                    variacao < 0 -> ContextCompatColor(R.color.negative)
                    else -> ContextCompatColor(R.color.neutral)
                }
            )

            item.manualRow.isVisible = indice.manual
            item.btnManualSalvar.setOnClickListener { salvarValorManual(indice, item) }
            item.root.setOnLongClickListener {
                editarIndice(indice)
                true
            }

            val lp = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(posicao % 2, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED)
                val m = (4 * resources.displayMetrics.density).toInt()
                setMargins(m, m, m, m)
            }
            binding.gridQuotes.addView(item.root, lp)
        }
    }

    private fun renderizarGraficos() {
        binding.chartsContainer.removeAllViews()

        if (indices.isEmpty()) {
            val aviso = android.widget.TextView(this).apply {
                text = getString(R.string.sem_dados_grafico)
                gravity = Gravity.CENTER
                setTextColor(ContextCompatColor(R.color.text_secondary))
                setPadding(0, 32, 0, 32)
                textSize = 13f
            }
            binding.chartsContainer.addView(aviso)
            return
        }

        indices.forEach { indice ->
            val item = ItemGraficoBinding.inflate(layoutInflater, binding.chartsContainer, false)
            item.dotCor.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Formato.parseCor(indice.cor))
            }
            item.tvNomeGrafico.text = indice.nome
            item.tvSufixoGrafico.text = indice.sufixo.trim()

            val cotacoes = db.getCotacoes(indice.id, limite = 30)
            item.chart.configurar(
                datas = cotacoes.map { it.data },
                valores = cotacoes.map { it.valor },
                cor = Formato.parseCor(indice.cor),
                casasDecimais = indice.casasDecimais
            )

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val m = (5 * resources.displayMetrics.density).toInt()
            lp.setMargins(0, m, 0, m)
            binding.chartsContainer.addView(item.root, lp)
        }
    }

    // ---------- Ações ----------

    private fun atualizarCotas() {
        if (indices.isEmpty()) {
            Toast.makeText(this, R.string.sem_indices, Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRefresh.isEnabled = false
        binding.tvStatus.text = getString(R.string.status_buscando)

        lifecycleScope.launch {
            val hoje = LocalDate.now().toString()
            val falhas = mutableListOf<String>()

            for (indice in indices) {
                val valor = if (indice.manual) {
                    db.getUltimoValor(indice.id) ?: indice.valorPadrao
                } else {
                    val buscado = CotacaoService.buscar(indice)
                    if (buscado == null) falhas += indice.nome
                    buscado ?: db.getUltimoValor(indice.id) ?: indice.valorPadrao
                }
                db.upsertCotacao(indice.id, hoje, valor)
            }

            binding.btnRefresh.isEnabled = true
            carregar()
            binding.tvStatus.text = if (falhas.isNotEmpty()) {
                getString(R.string.status_falha, falhas.joinToString(", "))
            } else {
                getString(R.string.status_atualizado) + " (" + Formato.dataBR(hoje) + ")"
            }
        }
    }

    // ---------- Backup ----------

    private fun exportarBackup(uri: Uri) {
        lifecycleScope.launch {
            val sucesso = withContext(Dispatchers.IO) {
                runCatching {
                    val dados = BackupDados(
                        indices = db.listIndices(),
                        cotacoes = db.getTodasCotacoes()
                    )
                    val saida = contentResolver.openOutputStream(uri)
                        ?: error("Não foi possível abrir o arquivo para escrita")
                    saida.bufferedWriter().use { it.write(Gson().toJson(dados)) }
                }.isSuccess
            }
            Toast.makeText(
                this@MainActivity,
                if (sucesso) R.string.backup_salvo else R.string.backup_erro,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun prepararImportacao(uri: Uri) {
        lifecycleScope.launch {
            binding.tvStatus.text = getString(R.string.restaurando)
            val dados = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                        Gson().fromJson(it, BackupDados::class.java)
                    }
                }.getOrNull()
            }

            if (dados == null || dados.indices.isEmpty()) {
                binding.tvStatus.text = getString(R.string.backup_invalido)
                Toast.makeText(this@MainActivity, R.string.backup_invalido, Toast.LENGTH_LONG).show()
                return@launch
            }

            AlertDialog.Builder(this@MainActivity)
                .setTitle(R.string.restaurar_titulo)
                .setMessage(
                    getString(R.string.confirmar_restaurar, dados.indices.size, dados.cotacoes.size)
                )
                .setNegativeButton(R.string.cancelar) { _, _ -> carregar() }
                .setPositiveButton(R.string.restaurar) { _, _ -> restaurarBackup(dados) }
                .show()
        }
    }

    private fun restaurarBackup(dados: BackupDados) {
        lifecycleScope.launch {
            val sucesso = withContext(Dispatchers.IO) {
                runCatching { db.substituirTudo(dados.indices, dados.cotacoes) }.isSuccess
            }
            carregar()
            if (sucesso) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.backup_restaurado, dados.indices.size),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(this@MainActivity, R.string.backup_erro, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun salvarValorManual(indice: Indice, item: ItemIndiceBinding) {
        val valor = Formato.parseNumero(item.etManualValor.text.toString())
        if (valor == null || valor < 0) {
            Toast.makeText(this, R.string.valor_invalido, Toast.LENGTH_SHORT).show()
            return
        }
        db.upsertCotacao(indice.id, LocalDate.now().toString(), valor)
        item.etManualValor.setText("")
        carregar()
        Toast.makeText(
            this, getString(R.string.status_manual, indice.nome), Toast.LENGTH_SHORT
        ).show()
    }

    private fun editarIndice(indice: Indice?) {
        val intent = Intent(this, IndexFormActivity::class.java)
        if (indice != null) intent.putExtra(EXTRA_INDICE, Gson().toJson(indice))
        abrirFormulario.launch(intent)
    }

    private fun abrirGerenciamento() {
        val dialogBinding = DialogGerenciarBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.gerenciar_titulo)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.adicionar, null)
            .create()

        lateinit var adapter: IndiceAdapter
        fun atualizar() {
            val lista = db.listIndices()
            adapter.submitList(lista)
            dialogBinding.tvVazio.isVisible = lista.isEmpty()
            dialogBinding.recyclerGerenciar.isVisible = lista.isNotEmpty()
        }

        adapter = IndiceAdapter(
            onEditar = { indice ->
                dialog.dismiss()
                editarIndice(indice)
            },
            onExcluir = { indice -> confirmarExclusao(indice) { dialog.dismiss(); atualizar(); carregar() } }
        )

        dialogBinding.recyclerGerenciar.layoutManager = LinearLayoutManager(this)
        dialogBinding.recyclerGerenciar.adapter = adapter
        atualizar()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                dialog.dismiss()
                editarIndice(null)
            }
        }
        dialog.show()
    }

    private fun confirmarExclusao(indice: Indice, depois: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirmar_excluir, indice.nome))
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.excluir_icone) { _, _ ->
                db.deleteIndice(indice.id)
                carregar()
                Toast.makeText(this, R.string.excluido, Toast.LENGTH_SHORT).show()
                depois()
            }
            .show()
    }

    private fun confirmarReinicio() {
        AlertDialog.Builder(this)
            .setMessage(R.string.confirmar_reiniciar)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.reiniciar_historico) { _, _ ->
                db.reiniciarHistorico()
                carregar()
                binding.tvStatus.text = getString(R.string.status_historico_limpo)
            }
            .show()
    }

    private fun ContextCompatColor(recurso: Int): Int =
        androidx.core.content.ContextCompat.getColor(this, recurso)

    companion object {
        const val EXTRA_INDICE = "indice_json"
    }
}
