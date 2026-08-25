package com.carlos.finas.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.carlos.finas.repositories.PainelDiarioRepository
import com.carlos.finas.services.TelegramService

/**
 * Worker periódico (a cada 24h) que gera o Painel Diário e envia via Telegram.
 *
 * Agendamento: ver DailyReportScheduler.schedule(context).
 */
class DailyReportWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val TAG = "DailyReportWorker"
        const val WORK_NAME = "daily_report_worker"
        const val KEY_RESERVA = "valor_reserva"
        const val DEFAULT_RESERVA = 500.00
    }

    override suspend fun doWork(): Result {
        return try {
            val reserva = inputData.getDouble(KEY_RESERVA, DEFAULT_RESERVA)
            Log.d(TAG, "Iniciando geração do relatório diário (reserva=$reserva)")

            val texto = PainelDiarioRepository.gerarEFormatar(applicationContext, reserva)
            if (texto.isBlank()) {
                Log.w(TAG, "Relatório vazio — nada a enviar")
                return Result.success()
            }

            val enviado = TelegramService.sendMessage(applicationContext, texto, parseMode = "HTML")
            if (enviado) {
                Log.d(TAG, "Relatório diário enviado ao Telegram com sucesso")
                Result.success()
            } else {
                Log.w(TAG, "Falha ao enviar relatório — WorkManager tentará novamente")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao executar worker: ${e.message}", e)
            Result.retry()
        }
    }
}