package com.antig.agendAS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(
    private val items: List<CalendarDayItem>,
    private val onDayClick: (Int) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val item = items[position]
        if (item.isBlank) {
            holder.itemView.visibility = View.INVISIBLE
            holder.itemView.isClickable = false
            holder.itemView.background = null
            holder.dayNumber.text = ""
        } else {
            holder.itemView.visibility = View.VISIBLE
            holder.itemView.isClickable = true
            holder.dayNumber.text = item.day.toString()
            holder.itemView.background = holder.itemView.context.getDrawable(
                when {
                    item.isToday -> R.drawable.day_cell_today
                    item.hasTask -> R.drawable.day_cell_task
                    else -> R.drawable.day_cell_bg
                }
            )
            holder.itemView.setOnClickListener { onDayClick(item.day) }
        }
    }

    override fun getItemCount(): Int = items.size

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayNumber: TextView = view.findViewById(R.id.dayNumber)
    }
}
