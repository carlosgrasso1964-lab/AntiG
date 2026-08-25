package com.carlos.finas.activities

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.R
import com.carlos.finas.databinding.ActivityPainelDiarioBinding
import com.carlos.finas.repositories.PainelDiarioRepository
import com.carlos.finas.services.TelegramService
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PainelDiarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPainelDiarioBinding
    private lateinit var dbHelper: DatabaseHelper
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateFormatDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val currencyFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))

    private lateinit var entradasAdapter: MovimentoAdapter
    private lateinit var saidasAdapter: MovimentoAdapter
    private var valorReserva: Double = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPainelDiarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        dbHelper.ensureViewsExist()

        setupRecyclerViews()
        setupListeners()
        mostrarDialogReserva()
    }

    private fun mostrarDialogReserva() {
        val editText = EditText(this)
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        editText.hint = "Digite o valor de reserva"
        editText.setText("0.00")

        AlertDialog.Builder(this)
            .setTitle("Valor de Reserva")
            .setMessage("Digite o valor que deseja manter como reserva de segurança:")
            .setView(editText)
            .setPositiveButton("Continuar") { _, _ ->
                val valorStr = editText.text.toString()
                valorReserva = valorStr.toDoubleOrNull() ?: 0.00
                carregarDados()
            }
            .setNegativeButton("Cancelar") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun setupRecyclerViews() {
        entradasAdapter = MovimentoAdapter()
        saidasAdapter = MovimentoAdapter()

        binding.recyclerEntradas.layoutManager = LinearLayoutManager(this)
        binding.recyclerEntradas.adapter = entradasAdapter

        binding.recyclerSaidas.layoutManager = LinearLayoutManager(this)
        binding.recyclerSaidas.adapter = saidasAdapter
    }

    private fun setupListeners() {
        binding.btnAtualizar.setOnClickListener {
            mostrarDialogReserva()
        }

        binding.btnEnviarTelegram.setOnClickListener {
            enviarRelatorioTelegram()
        }

        binding.btnFechar.setOnClickListener {
            finish()
        }
    }

    private fun enviarRelatorioTelegram() {
        binding.btnEnviarTelegram.isEnabled = false
        binding.btnEnviarTelegram.text = "Enviando..."

        Thread {
            try {
                val texto = PainelDiarioRepository.gerarEFormatar(this, valorReserva)
                val sucesso = TelegramService.sendMessage(this, texto, "HTML")

                runOnUiThread {
                    binding.btnEnviarTelegram.isEnabled = true
                    binding.btnEnviarTelegram.text = "Enviar Telegram"
                    Toast.makeText(
                        this,
                        if (sucesso) "✅ Relatório enviado ao Telegram!" else "❌ Falha ao enviar — verifique token/chat_id",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.btnEnviarTelegram.isEnabled = true
                    binding.btnEnviarTelegram.text = "Enviar Telegram"
                    Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("PainelDiarioActivity", "Erro ao enviar Telegram", e)
                }
            }
        }.start()
    }

    private fun carregarDados() {
        try {
            val relatorio = com.carlos.finas.repositories.PainelDiarioRepository.gerarRelatorio(this, valorReserva)

            binding.txtTitulo.text = getString(R.string.painel_diario) + " - " + dateFormatDisplay.format(Date())

            binding.txtSaldoInicial.text = currencyFormat.format(relatorio.saldoInicial)
            binding.txtTotalEntradas.text = currencyFormat.format(relatorio.totalEntradas())
            binding.txtTotalSaidas.text = currencyFormat.format(relatorio.totalSaidas())
            binding.txtSaldoPoupanca.text = currencyFormat.format(relatorio.saldoPoupanca)

            binding.txtSaldoFinal.text = currencyFormat.format(relatorio.saldoFinalDia)
            if (relatorio.saldoFinalDia < 0) {
                binding.txtSaldoFinal.setTextColor(Color.RED)
            } else {
                binding.txtSaldoFinal.setTextColor(Color.parseColor("#4CAF50"))
            }

            binding.txtSugestaoAplicar.text = currencyFormat.format(relatorio.sugestaoAplicar)
            binding.txtSugestaoSegura.text = currencyFormat.format(relatorio.sugestaoSeguraAplicar)

            if (relatorio.sugestaoSeguraAplicar > 0) {
                binding.txtSugestaoSegura.setTextColor(Color.parseColor("#4CAF50"))
                binding.txtSugestaoAplicar.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                binding.txtSugestaoSegura.setTextColor(Color.RED)
                binding.txtSugestaoAplicar.setTextColor(Color.RED)
            }

            binding.txtMenorSaldoFuturo.text = currencyFormat.format(relatorio.menorSaldoProjetado)
            binding.txtDataMenorSaldo.text = relatorio.dataMenorSaldo

            binding.txtAlerta.text = if (relatorio.alerta.isNotEmpty()) relatorio.alerta else "Nenhum"

            if (relatorio.alerta.contains("INSUFICIENTE") || relatorio.alerta.contains("queda")) {
                binding.txtAlerta.setTextColor(Color.RED)
            } else if (relatorio.alerta.contains("Aplicar")) {
                binding.txtAlerta.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                binding.txtAlerta.setTextColor(Color.GRAY)
            }

            entradasAdapter.submitList(relatorio.entradas.map {
                MovimentoAdapter.MovimentoItem(it.descricao, it.valor)
            })

            saidasAdapter.submitList(relatorio.saidas.map {
                MovimentoAdapter.MovimentoItem(it.descricao, it.valor)
            })

            Log.d("PainelDiarioActivity", "Dados carregados com sucesso")

        } catch (e: Exception) {
            Log.e("PainelDiarioActivity", "Erro ao carregar dados: ${e.message}")
            Toast.makeText(this, "Erro ao carregar dados: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    class MovimentoAdapter : RecyclerView.Adapter<MovimentoAdapter.ViewHolder>() {

        data class MovimentoItem(val descricao: String, val valor: Double)
        private var items: List<MovimentoItem> = emptyList()

        fun submitList(newItems: List<MovimentoItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val text1: TextView = itemView.findViewById(android.R.id.text1)
            private val text2: TextView = itemView.findViewById(android.R.id.text2)

            fun bind(item: MovimentoItem) {
                text1.text = item.descricao
                text2.text = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR"))).format(item.valor)
                text2.setTextColor(if (item.valor >= 0) Color.parseColor("#4CAF50") else Color.RED)
            }
        }
    }
}