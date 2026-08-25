package com.carlos.finas.repositories

import android.content.Context
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.DatabaseHelper.RelatorioPainel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repositório para geração do relatório do Painel Diário.
 * Isola a chamada ao SQLite do restante do app (Activity e Worker usam esta camada).
 *
 * Formata o relatório em texto pronto para envio (Telegram/WhatsApp/etc).
 */
object PainelDiarioRepository {

    private val dateFormatInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateFormatDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val currencyFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))

    /**
     * Gera o relatório bruto (estrutura RelatorioPainel) consultando o SQLite.
     */
    fun gerarRelatorio(context: Context, reserva: Double = 500.00): RelatorioPainel {
        val db = DatabaseHelper(context)
        db.ensureViewsExist()
        val dataHoje = dateFormatInput.format(Date())
        return db.gerarRelatorioDiario(dataHoje, reserva)
    }

    /**
     * Formata o relatório em texto Telegram-friendly (HTML).
     * Estrutura: cabeçalho → saldo → entradas → saídas → projeção → alerta.
     */
    fun formatarParaTelegram(relatorio: RelatorioPainel): String {
        val sb = StringBuilder()

        sb.append("<b>📊 Painel Diário FinAS</b>\n")
        sb.append("Data: ${dateFormatDisplay.format(Date())}\n\n")

        sb.append("<b>💰 Saldos</b>\n")
        sb.append("• Saldo Inicial: ${currencyFormat.format(relatorio.saldoInicial)}\n")
        sb.append("• Entradas pendentes: ${currencyFormat.format(relatorio.totalEntradas())}\n")
        sb.append("• Saídas pendentes: ${currencyFormat.format(relatorio.totalSaidas())}\n")
        val saldoFinalCor = if (relatorio.saldoFinalDia < 0) "🔴" else "🟢"
        sb.append("• Saldo Final do Dia: $saldoFinalCor ${currencyFormat.format(relatorio.saldoFinalDia)}\n\n")

        if (relatorio.entradas.isNotEmpty()) {
            sb.append("<b>📥 Entradas (${relatorio.entradas.size})</b>\n")
            relatorio.entradas.take(10).forEach { item ->
                // Escapar caracteres que quebram o HTML do Telegram
                val descSafe = item.descricao.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                sb.append("• $descSafe: ${currencyFormat.format(item.valor)}\n")
            }
            if (relatorio.entradas.size > 10) {
                sb.append("• ... e mais ${relatorio.entradas.size - 10}\n")
            }
            sb.append("\n")
        }

        if (relatorio.saidas.isNotEmpty()) {
            sb.append("<b>📤 Saídas (${relatorio.saidas.size})</b>\n")
            relatorio.saidas.take(10).forEach { item ->
                // Escapar caracteres que quebram o HTML do Telegram
                val descSafe = item.descricao.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                sb.append("• $descSafe: ${currencyFormat.format(item.valor)}\n")
            }
            if (relatorio.saidas.size > 10) {
                sb.append("• ... e mais ${relatorio.saidas.size - 10}\n")
            }
            sb.append("\n")
        }

        sb.append("<b>🔮 Projeção Futura</b>\n")
        sb.append("• Menor saldo projetado: ${currencyFormat.format(relatorio.menorSaldoProjetado)}\n")
        sb.append("• Data do menor saldo: ${relatorio.dataMenorSaldo}\n")
        sb.append("• Poupança atual: ${currencyFormat.format(relatorio.saldoPoupanca)}\n\n")

        if (relatorio.alerta.isNotEmpty()) {
            sb.append("<b>⚠️ Alerta</b>\n")
            val alertaSafe = relatorio.alerta.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            sb.append(alertaSafe)
            sb.append("\n")
        }

        return sb.toString()
    }

    /**
     * Convenience: gera e formata em uma chamada só.
     */
    fun gerarEFormatar(context: Context, reserva: Double = 500.00): String {
        val relatorio = gerarRelatorio(context, reserva)
        return formatarParaTelegram(relatorio)
    }
}