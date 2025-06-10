package com.example.calendario

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
        view.setBackgroundDrawable(highlightDrawable)
        // Opcionalmente, você pode mudar a cor do texto ou aplicar outro estilo aqui
        // view.setTextColor(Color.WHITE)
    }
}