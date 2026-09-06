package com.carlos.finas.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Agendador do Painel Diário via AlarmManager.
 *
 * Dispara em horário fixo (padrão 06:00) e re-agenda o próximo dia ao disparar
 * (padrão reativo, mais confiável que o WorkManager periódico para horário exato).
 *
 * Uso: DailyReportScheduler.schedule(context) no login (MainActivity).
 *
 * Requer a permissão SCHEDULE_EXACT_ALARM no AndroidManifest para alarmes exatos
 * (targetSdk 31+). Em Android 14+, a permissão é solicitada via intent do sistema.
 */
object DailyReportScheduler {

    private const val TAG = "DailyReportScheduler"
    const val EXTRA_RESERVA = "valor_reserva"

    // Horário fixo de envio (24h).
    const val HOUR = 6
    const val MINUTE = 0

    private const val REQUEST_CODE = 1001
    const val ACTION_SEND = "com.carlos.finas.ACTION_SEND_DAILY_REPORT"

    /**
     * Agenda o próximo disparo para [HOUR]:[MINUTE] (horário local).
     * Se já passou do horário hoje, agenda para amanhã; senão para hoje.
     *
     * Também cancela o agendamento periódico antigo via WorkManager (migração),
     * para não gerar mensagens duplicadas no dispositivo.
     */
    fun schedule(context: Context, reserva: Double = DailyReportWorker.DEFAULT_RESERVA) {
        cancelLegacyWorkManager(context)
        scheduleNext(context, reserva)
    }

    /**
     * Cancela o WorkManager periódico antigo (versão anterior do agendador),
     * evitando mensagens duplicadas durante a migração.
     */
    private fun cancelLegacyWorkManager(context: Context) {
        try {
            val workManager = androidx.work.WorkManager.getInstance(context)
            workManager.cancelUniqueWork(DailyReportWorker.WORK_NAME)
        } catch (_: Exception) {
            // Se o WorkManager não estiver disponível/disparado, ignora.
        }
    }

    fun scheduleNext(context: Context, reserva: Double = DailyReportWorker.DEFAULT_RESERVA) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, reserva)

        val triggerAt = nextTriggerMillis()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+: verifica se temos permissão de alarme exato.
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                // Sem permissão: cai para alarme inexato (pode atrasar um pouco, mas não quebra).
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 - 11: setExactAndAllowWhileIdle funciona (Doze).
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Cancel precisa do mesmo requestCode/intent (reserva padrão basta p/ o requestCode)
        val pendingIntent = buildPendingIntent(context, DailyReportWorker.DEFAULT_RESERVA)
        alarmManager.cancel(pendingIntent)
    }

    private fun buildPendingIntent(context: Context, reserva: Double): PendingIntent {
        val intent = Intent(context, DailyReportAlarmReceiver::class.java).apply {
            action = ACTION_SEND
            putExtra(EXTRA_RESERVA, reserva)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Próximo horário 06:00 (hoje se ainda não passou, senão amanhã).
     */
    private fun nextTriggerMillis(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, HOUR)
            set(Calendar.MINUTE, MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}
