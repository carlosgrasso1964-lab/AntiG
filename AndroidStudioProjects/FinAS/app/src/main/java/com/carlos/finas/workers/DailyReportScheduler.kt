package com.carlos.finas.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Agendador do DailyReportWorker.
 * Use `DailyReportScheduler.schedule(context)` no MyApplication.onCreate() ou na MainActivity.
 *
 * Restrições:
 * - PeriodicWorkRequest mínimo = 15 min, mas WorkManager respeita janelas de Doze/Standby.
 * - Roda SOMENTE com internet (NetworkType.CONNECTED).
 * - Janela flexível de 6h (executa em qualquer momento dentro do período de 24h).
 */
object DailyReportScheduler {

    fun schedule(context: Context, reserva: Double = 500.00) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putDouble(DailyReportWorker.KEY_RESERVA, reserva)
            .build()

        val request = PeriodicWorkRequestBuilder<DailyReportWorker>(
            24, TimeUnit.HOURS,
            6, TimeUnit.HOURS  // janela flexível
        )
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyReportWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DailyReportWorker.WORK_NAME)
    }
}