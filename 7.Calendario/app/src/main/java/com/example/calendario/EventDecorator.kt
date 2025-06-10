package com.example.calendario

// src/main/java/com/yourpackage/yourapp/EventDecorator.kt


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class EventDecorator(private val color: Int, private val dates: Collection<CalendarDay>) : DayViewDecorator {

    private val highlightDrawable = ColorDrawable(color)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        // Aplica um fundo colorido. Para um círculo, você precisaria de um drawable XML de shape redondo.
        view.setBackgroundDrawable(highlightDrawable)
        // Opcionalmente, mudar a cor do texto para melhor contraste
        // view.setTextColor(Color.WHITE)
    }
}