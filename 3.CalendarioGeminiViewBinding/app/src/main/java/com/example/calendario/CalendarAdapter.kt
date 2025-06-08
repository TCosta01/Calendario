package com.example.calendario

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.calendario.databinding.CalendarDayItemBinding

class CalendarAdapter(
    private var days: List<CalendarDay>,
    private val onDayClickListener: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    inner class DayViewHolder(private val binding: CalendarDayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(calendarDay: CalendarDay) {
            binding.dayText.text = calendarDay.date?.dayOfMonth?.toString() ?: ""
            binding.dayText.isEnabled = calendarDay.isCurrentMonth // Desabilita dias de outros meses
            binding.dayText.isSelected = calendarDay.isSelected // Define o estado de seleção

            binding.root.setOnClickListener {
                if (calendarDay.date != null && calendarDay.isCurrentMonth) {
                    onDayClickListener(calendarDay)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = CalendarDayItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    fun updateDays(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }

    fun updateSelectedDay(selectedDay: CalendarDay) {
        val index = days.indexOfFirst { it.date == selectedDay.date }
        if (index != -1) {
            days[index].isSelected = selectedDay.isSelected
            notifyItemChanged(index)
        }
    }
}