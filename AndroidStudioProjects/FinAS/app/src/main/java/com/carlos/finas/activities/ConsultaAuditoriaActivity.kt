package com.carlos.finas.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.R
import com.carlos.finas.services.AIPlatformDetector
import com.carlos.finas.services.AIService
import com.carlos.finas.databinding.ActivityConsultaAuditoriaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AuditoriaItem(
    val conta: String,
    val previsto: String,
    val realizado: String,
    val diferenca: String,
    val variacao: String
)

class AuditoriaAdapter(
    private var items: List<AuditoriaItem>
) : RecyclerView.Adapter<AuditoriaAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textConta: TextView = view.findViewById(R.id.textConta)
        val textPrevisto: TextView = view.findViewById(R.id.textPrevisto)
        val textRealizado: TextView = view.findViewById(R.id.textRealizado)
        val textDiferenca: TextView = view.findViewById(R.id.textDiferenca)
        val textVariacao: TextView = view.findViewById(R.id.textVariacao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_auditoria, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textConta.text = item.conta
        holder.textPrevisto.text = item.previsto
        holder.textRealizado.text = item.realizado
        holder.textDiferenca.text = item.diferenca
        holder.textVariacao.text = item.variacao
        
        // Cor diferenciada para diferença negativa (aumento de gasto)
        val diferencaValor = item.diferenca.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        holder.textDiferenca.setTextColor(
            if (diferencaValor < 0) 0xFFFF4444.toInt() else 0xFF44AA44.toInt()
        )
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<AuditoriaItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}

class ConsultaAuditoriaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsultaAuditoriaBinding
    private val dateFormatInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateFormatOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val decimalFormat = DecimalFormat("#,##0.00").apply {
        val symbols = DecimalFormatSymbols(Locale("pt", "BR"))
        symbols.groupingSeparator = '.'
        symbols.decimalSeparator = ','
        this.decimalFormatSymbols = symbols
    }

    private val items = mutableListOf<AuditoriaItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsultaAuditoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewAuditoria.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAuditoria.adapter = AuditoriaAdapter(items)

        binding.editTextDataInicial.addTextChangedListener(DateMaskWatcher())
        binding.editTextDataFinal.addTextChangedListener(DateMaskWatcher())

        binding.btnAnalisar.setOnClickListener { analisar() }
        binding.btnLia.setOnClickListener { analisarComLia() }
        binding.btnLimpar.setOnClickListener { limpar() }
        binding.btnVoltar.setOnClickListener { finish() }

        // Preencher ano atual
        val ano = Calendar.getInstance().get(Calendar.YEAR)
        binding.editTextDataInicial.setText("01/01/$ano")
        binding.editTextDataFinal.setText("31/12/$ano")
    }

    private inner class DateMaskWatcher : TextWatcher {
        private var isUpdating = false
        private val mask = "##/##/####"

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (isUpdating || s.isNullOrEmpty()) return
            isUpdating = true
            val str = s.toString().replace("[^0-9]".toRegex(), "")
            val formatted = StringBuilder()
            var i = 0
            for (m in mask) {
                if (m != '#' && i < str.length) {
                    formatted.append(m)
                    continue
                }
                if (i < str.length) {
                    formatted.append(str[i])
                    i++
                }
            }
            s.replace(0, s.length, formatted.toString())
            isUpdating = false
        }
    }

    private fun formatarDataParaBanco(dataBr: String): String {
        return try {
            val partes = dataBr.split("/")
            "${partes[2]}-${partes[1]}-${partes[0]}"
        } catch (e: Exception) {
            ""
        }
    }

    private fun analisar() {
        val dataIni = binding.editTextDataInicial.text.toString().trim()
        val dataFim = binding.editTextDataFinal.text.toString().trim()

        if (dataIni.length < 10 || dataFim.length < 10) {
            Toast.makeText(this, "Preencha as datas corretamente.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dataIniSQL = formatarDataParaBanco(dataIni)
                val dataFimSQL = formatarDataParaBanco(dataFim)

                val campoData = if (binding.radioVcto.isChecked) "dtVcto" else "dtApr"
                val filtroStatus = when {
                    binding.radioConf.isChecked -> " AND Prev = 'V' "
                    binding.radioPrev.isChecked -> " AND Prev = 'F' "
                    else -> ""
                }

                val db = DatabaseHelper(this@ConsultaAuditoriaActivity)
                val anoAtual = Calendar.getInstance().get(Calendar.YEAR)

                val mesInicio = dataIniSQL.substring(5, 7).toInt()
                val mesFim = dataFimSQL.substring(5, 7).toInt()

                val sql = """
                    SELECT 
                        p.conta AS conta,
                        ROUND(SUM(p.valor_planejado), 2) AS previsto,
                        ROUND(COALESCE(m.TotalRealizado, 0), 2) AS realizado,
                        ROUND((COALESCE(m.TotalRealizado, 0) - SUM(p.valor_planejado)), 2) AS diferenca,
                        CASE WHEN SUM(p.valor_planejado) = 0 THEN 0 
                             ELSE ROUND(((COALESCE(m.TotalRealizado, 0) / SUM(p.valor_planejado)) - 1) * 100, 2) 
                        END AS variacao
                    FROM tb_plano_diretor p
                    LEFT JOIN (
                        SELECT nome_C, SUM(Valor) as TotalRealizado
                        FROM viewmovrd
                        WHERE Prev = 'V'
                          AND $campoData BETWEEN ? AND ?
                          $filtroStatus
                        GROUP BY nome_C
                    ) m ON p.conta = m.nome_C
                    WHERE p.ano_referencia = ?
                      AND p.mes BETWEEN ? AND ?
                      AND p.conta NOT LIKE 'Receitas%'
                      AND p.conta NOT LIKE 'Despesas%'
                      AND p.conta NOT LIKE 'Saldo%'
                      AND p.conta NOT LIKE 'Resultado%'
                    GROUP BY p.conta
                    ORDER BY ABS(COALESCE(m.TotalRealizado, 0) - SUM(p.valor_planejado)) DESC
                """.trimIndent()

                val resultados = mutableListOf<AuditoriaItem>()

                db.readableDatabase.rawQuery(sql, arrayOf(
                    dataIniSQL, dataFimSQL,
                    anoAtual.toString(),
                    mesInicio.toString(), mesFim.toString()
                )).use { cursor ->
                    while (cursor.moveToNext()) {
                        val conta = cursor.getString(0) ?: ""
                        val previsto = cursor.getDouble(1)
                        val realizado = cursor.getDouble(2)
                        val diferenca = cursor.getDouble(3)
                        val variacao = cursor.getDouble(4)

                        resultados.add(
                            AuditoriaItem(
                                conta = conta,
                                previsto = decimalFormat.format(previsto),
                                realizado = decimalFormat.format(realizado),
                                diferenca = decimalFormat.format(diferenca),
                                variacao = "${decimalFormat.format(variacao)}%"
                            )
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    items.clear()
                    items.addAll(resultados)
                    (binding.recyclerViewAuditoria.adapter as AuditoriaAdapter).notifyDataSetChanged()

                    if (resultados.isEmpty()) {
                        Toast.makeText(this@ConsultaAuditoriaActivity, 
                            "Nenhum dado encontrado. Salve os previstos primeiro!", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ConsultaAuditoriaActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

private fun analisarComLia() {
        if (items.isEmpty()) {
            Toast.makeText(this, "Execute a análise primeiro!", Toast.LENGTH_SHORT).show()
            return
        }

        val dataIni = binding.editTextDataInicial.text.toString()
        val dataFim = binding.editTextDataFinal.text.toString()

        val db = DatabaseHelper(this@ConsultaAuditoriaActivity)
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        val inflacaoAnual = db.getInflacaoPremissa(anoAtual)

        val mesInicio = dataIni.substring(3, 5).toInt()
        val mesFim = dataFim.substring(3, 5).toInt()
        val qtdMeses = (mesFim - mesInicio) + 1
        val inflacaoPeriodo = (inflacaoAnual / 12) * qtdMeses

        val prompt = buildString {
            append("Lia, atue como minha analista financeira pessoal. ")
            append("Analise meus desvios financeiros no período de ")
            append(dataIni).append(" a ").append(dataFim).append(". ")
            append("Atenção: valores negativos são despesas. Se o desvio for negativo e o valor realizado for maior que o previsto, isso é um aumento de gasto. ")
            append("Minha premissa de inflação ANUAL é de ").append(String.format("%.2f", inflacaoAnual)).append("%, ")
            append("o que equivale a uma inflação de ").append(String.format("%.2f", inflacaoPeriodo))
            append("% para este período de ").append(qtdMeses).append(" mês(es).\n\n")
            append("Lia, quando o desvio da Aposentadoria for negativo, chame isso de 'Achatamento' e analise o impacto disso no meu consumo de itens básicos como Energia e Gás.\n\n")
            append("Dados da Auditoria:\n")

            for (item in items) {
                append("- ").append(item.conta)
                    .append(": Prev ").append(item.previsto)
                    .append(" | Real ").append(item.realizado)
                    .append(" | Desvio: ").append(item.variacao).append("\n")
            }

            append("\nCom base na inflação de ").append(String.format("%.2f", inflacaoPeriodo))
                .append("%, identifique onde meu poder de compra está sendo mais 'achatado'.")
        }

        val progressDialog = AlertDialog.Builder(this)
            .setTitle("Enviando para análise...")
            .setMessage("Aguarde, processando com IA local...")
            .setCancelable(false)
            .create()
        progressDialog.show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val aiService = AIService.create(this@ConsultaAuditoriaActivity)
                val config = AIPlatformDetector.detectPlatform(this@ConsultaAuditoriaActivity)

                val result = aiService.generateText(prompt, timeoutMs = 180000)

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()

                    result.fold(
                        onSuccess = { response ->
                            val textArea = android.widget.TextView(this@ConsultaAuditoriaActivity).apply {
                                text = response
                                setPadding(32, 32, 32, 32)
                                textSize = 14f
                            }

                            val scrollView = android.widget.ScrollView(this@ConsultaAuditoriaActivity).apply {
                                addView(textArea)
                            }

                            AlertDialog.Builder(this@ConsultaAuditoriaActivity)
                                .setTitle("Parecer da Lia (${config.platform.name})")
                                .setView(scrollView)
                                .setPositiveButton("Fechar", null)
                                .show()
                        },
                        onFailure = { error ->
                            val models = AIPlatformDetector.SUGGESTED_MODELS.joinToString("\n") { "• ${it.second}" }
                            val message = if (error.message?.contains("ML Kit") == true) {
                                "ML Kit não configurado.\n\n" +
                                "Para usar no celular, instale o Ollama no Termux:\n" +
                                "1. pkg install ollama\n" +
                                "2. ollama pull gemma2:2b\n" +
                                "3. ollama serve\n\n" +
                                "Modelos alternativos disponíveis:\n$models"
                            } else {
                                "Erro: ${error.message}"
                            }
                            
                            AlertDialog.Builder(this@ConsultaAuditoriaActivity)
                                .setTitle("Erro")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    AlertDialog.Builder(this@ConsultaAuditoriaActivity)
                        .setTitle("Erro")
                        .setMessage("Lia está exausta (Erro): ${e.message}")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
}
    }

    private fun limpar() {
        binding.editTextDataInicial.text?.clear()
        binding.editTextDataFinal.text?.clear()
        items.clear()
        (binding.recyclerViewAuditoria.adapter as AuditoriaAdapter).notifyDataSetChanged()
    }
}