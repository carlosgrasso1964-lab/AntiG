package com.carlos.finas.activities

import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.databinding.ActivityCalendarioBinding
import java.util.Calendar
import java.util.Locale

class CalendarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarioBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var taskStorage: TaskStorage
    private val calendar = Calendar.getInstance()

    private val dayLabels = arrayOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityCalendarioBinding.inflate(layoutInflater)
            setContentView(binding.root)

            dbHelper = DatabaseHelper(this)
            taskStorage = TaskStorage(dbHelper)

            populateDayOfWeekHeader()

            binding.headerInclude.btnPrevMonth.setOnClickListener {
                calendar.add(Calendar.MONTH, -1)
                refreshCalendar()
            }
            binding.headerInclude.btnNextMonth.setOnClickListener {
                calendar.add(Calendar.MONTH, 1)
                refreshCalendar()
            }
            binding.btnVoltar.setOnClickListener { finish() }

            refreshCalendar()
        }

        fun refreshCalendar() {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            binding.headerInclude.monthYearText.text = String.format(
                Locale("pt", "BR"),
                "%1\$tB %1\$tY", calendar
            ).replaceFirstChar { it.uppercase() }

        val taskDates = taskStorage.getAllDateKeys()
        val days = CalendarAdapter.buildMonth(year, month, taskDates)

        binding.calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)
        binding.calendarRecyclerView.adapter = CalendarAdapter(days) { day ->
            if (day.isCurrentMonth) {
                TaskDialogFragment.newInstance(day.dateKey)
                    .show(supportFragmentManager, "task_dialog")
            }
        }
    }

    private fun populateDayOfWeekHeader() {
        binding.daysOfWeekLayout.removeAllViews()
        val density = resources.displayMetrics.density
        val cellWidth = (resources.displayMetrics.widthPixels / 7f).toInt()
        dayLabels.forEach { label ->
            val tv = TextView(this).apply {
                text = label
                gravity = Gravity.CENTER
                setTextColor(getColor(com.carlos.finas.R.color.text_secondary))
                textSize = 12f
                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellWidth
                    height = (32 * density).toInt()
                }
            }
            binding.daysOfWeekLayout.addView(tv)
        }
    }
}
