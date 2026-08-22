package com.antig.agendAS

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var monthYearText: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var calendarGrid: RecyclerView
    private lateinit var storage: TaskStorage

    private var currentDate = LocalDate.now()
    private var currentMonth = currentDate.monthValue - 1
    private var currentYear = currentDate.year

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage = TaskStorage(this)

        monthYearText = findViewById(R.id.monthYearText)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        calendarGrid = findViewById(R.id.calendarGrid)

        calendarGrid.layoutManager = GridLayoutManager(this, 7)

        btnPrevMonth.setOnClickListener {
            currentMonth--
            if (currentMonth < 0) {
                currentMonth = 11
                currentYear--
            }
            renderCalendar()
        }

        btnNextMonth.setOnClickListener {
            currentMonth++
            if (currentMonth > 11) {
                currentMonth = 0
                currentYear++
            }
            renderCalendar()
        }

        renderCalendar()
    }

    fun refreshCalendar() {
        renderCalendar()
    }

    private fun renderCalendar() {
        val yearMonth = YearMonth.of(currentYear, currentMonth + 1)
        val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7
        val daysInMonth = yearMonth.lengthOfMonth()
        val today = LocalDate.now()

        val monthName = yearMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
        monthYearText.text = "$monthName $currentYear"

        val items = mutableListOf<CalendarDayItem>()

        for (i in 0 until firstDayOfWeek) {
            items.add(CalendarDayItem(isBlank = true))
        }

        for (day in 1..daysInMonth) {
            val dateKey = "$currentYear-${currentMonth + 1}-$day"
            items.add(
                CalendarDayItem(
                    day = day,
                    isToday = day == today.dayOfMonth && currentMonth == today.monthValue - 1 && currentYear == today.year,
                    hasTask = storage.hasTasks(dateKey)
                )
            )
        }

        val remaining = (7 - (items.size % 7)) % 7
        for (i in 0 until remaining) {
            items.add(CalendarDayItem(isBlank = true))
        }

        val adapter = CalendarAdapter(items) { day ->
            val dateKey = "$currentYear-${currentMonth + 1}-$day"
            val dialog = TaskDialogFragment.newInstance(dateKey)
            dialog.show(supportFragmentManager, "TaskDialog")
        }

        calendarGrid.adapter = adapter
    }
}
