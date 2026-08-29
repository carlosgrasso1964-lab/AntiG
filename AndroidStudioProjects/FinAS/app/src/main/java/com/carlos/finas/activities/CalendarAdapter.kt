package com.carlos.finas.activities

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.databinding.ItemCalendarDayBinding
import java.util.Calendar

class CalendarAdapter(
    private val days: List<CalendarDay>,
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    data class CalendarDay(
        val dayOfMonth: Int,
        val dateKey: String, // yyyy-MM-dd
        val isCurrentMonth: Boolean,
        val isToday: Boolean,
        val hasTask: Boolean
    )

    inner class DayViewHolder(val binding: ItemCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayViewHolder(binding)
    }

    override fun getItemCount(): Int = days.size

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        with(holder.binding) {
            dayText.text = day.dayOfMonth.toString()

            val ctx = root.context
            val isDark = (ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val textColorNormal = if (isDark) ctx.getColor(com.carlos.finas.R.color.text_primary) else ctx.getColor(com.carlos.finas.R.color.day_text)
            val textColorDisabled = ctx.getColor(com.carlos.finas.R.color.text_secondary)
            val textColorToday = ctx.getColor(com.carlos.finas.R.color.primary)
            val bgNormal = com.carlos.finas.R.drawable.day_cell_bg
            val bgToday = com.carlos.finas.R.drawable.today_bg
            val bgTask = com.carlos.finas.R.drawable.task_bg

            when {
                day.isToday -> {
                    root.setBackgroundResource(bgToday)
                    dayText.setTextColor(textColorToday)
                }
                !day.isCurrentMonth -> {
                    root.setBackgroundResource(bgNormal)
                    dayText.setTextColor(textColorDisabled)
                }
                day.hasTask -> {
                    root.setBackgroundResource(bgTask)
                    dayText.setTextColor(ctx.getColor(com.carlos.finas.R.color.white))
                }
                else -> {
                    root.setBackgroundResource(bgNormal)
                    dayText.setTextColor(textColorNormal)
                }
            }

            taskIndicator.visibility = if (day.hasTask) View.VISIBLE else View.GONE

            root.setOnClickListener { onDayClick(day) }
        }
    }

    companion object {
        fun buildMonth(
            year: Int,
            month: Int, // 0-based
            tasksByDate: Set<String>
        ): List<CalendarDay> {
            val cal = Calendar.getInstance()
            cal.set(year, month, 1)
            val firstDow = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            val today = Calendar.getInstance()
            val todayKey = "%04d-%02d-%02d".format(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.DAY_OF_MONTH)
            )

            val list = mutableListOf<CalendarDay>()

            // dias do mês anterior (preenchimento)
            val prevCal = Calendar.getInstance()
            prevCal.set(year, month - 1, 1)
            val prevDays = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val prevYear = prevCal.get(Calendar.YEAR)
            val prevMonth = prevCal.get(Calendar.MONTH) + 1
            for (i in (firstDow - 1) downTo 1) {
                val dKey = "%04d-%02d-%02d".format(prevYear, prevMonth, prevDays - i + 1)
                list.add(CalendarDay(prevDays - i + 1, dKey, false, false, tasksByDate.contains(dKey)))
            }

            // dias do mês atual
            for (d in 1..daysInMonth) {
                val dKey = "%04d-%02d-%02d".format(year, month + 1, d)
                list.add(
                    CalendarDay(
                        dayOfMonth = d,
                        dateKey = dKey,
                        isCurrentMonth = true,
                        isToday = dKey == todayKey,
                        hasTask = tasksByDate.contains(dKey)
                    )
                )
            }

            // dias do próximo mês (até completar múltiplo de 7)
            val nextCal = Calendar.getInstance()
            nextCal.set(year, month + 1, 1)
            val nextYear = nextCal.get(Calendar.YEAR)
            val nextMonth = nextCal.get(Calendar.MONTH) + 1
            var nextDay = 1
            while (list.size % 7 != 0 || list.size < 35) {
                val dKey = "%04d-%02d-%02d".format(nextYear, nextMonth, nextDay)
                list.add(CalendarDay(nextDay, dKey, false, false, tasksByDate.contains(dKey)))
                nextDay++
                if (list.size >= 42) break
            }

            return list
        }
    }
}
