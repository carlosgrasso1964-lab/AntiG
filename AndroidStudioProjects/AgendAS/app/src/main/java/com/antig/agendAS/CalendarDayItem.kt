package com.antig.agendAS

data class CalendarDayItem(
    val day: Int = 0,
    val isBlank: Boolean = false,
    val isToday: Boolean = false,
    val hasTask: Boolean = false
)
