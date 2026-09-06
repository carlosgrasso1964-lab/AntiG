package com.carlos.finas.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.carlos.finas.repositories.PainelDiarioRepository
import com.carlos.finas.services.TelegramService

/**
 * Receptor de alarme do Painel Diário.
 *
 * Disparado pelo AlarmManager (ver DailyReportScheduler.schedule) no horário
 * configurado (padrão 06:00). Gera o painel, envia via Telegram e re-agenda
 * o próximo disparo para o dia seguinte — alarme reativo mais confiável que o
 * WorkManager periódico, que não respeita horário fixo.
 */
class DailyReportAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DailyReportAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarme disparado: gerando Painel Diário")

        // goAsync dá ~10s para trabalho em background fora da main thread.
        val pendingResult = goAsync()

        // Wakelock temporário: garante que a CPU não volte a dormir durante o
        // envio via rede (Telegram), onde o dispositivo pode estar em Doze.
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FinAS:DailyReport").apply {
            setReferenceCounted(false)
            acquire(30_000L) // 30s máx
        }

        Thread {
            try {
                val reserva = intent.getDoubleExtra(
                    DailyReportScheduler.EXTRA_RESERVA,
                    DailyReportWorker.DEFAULT_RESERVA
                )

                val texto = PainelDiarioRepository.gerarEFormatar(context, reserva)
                if (texto.isBlank()) {
                    Log.w(TAG, "Relatório vazio — nada a enviar")
                } else {
                    val enviado = TelegramService.sendMessage(context, texto, parseMode = "HTML")
                    Log.d(TAG, if (enviado) "Painel enviado ao Telegram" else "Falha ao enviar (será re-agendado amanhã)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao gerar/enviar Painel: ${e.message}", e)
            } finally {
                if (wakeLock.isHeld) wakeLock.release()
                pendingResult.finish()
            }
        }.start()

        // Re-agenda o próximo disparo para o dia seguinte.
        DailyReportScheduler.scheduleNext(context)
    }
}
