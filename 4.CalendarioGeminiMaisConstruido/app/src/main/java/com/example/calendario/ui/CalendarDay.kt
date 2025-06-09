// src/main/java/com/example/reservascalendario/ui/CalendarDay.kt
package com.example.calendario.ui

import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate?, // Pode ser nulo para preencher espaços vazios no início/fim do mês
    val isCurrentMonth: Boolean,
    var isSelected: Boolean = false,
    var isReserved: Boolean = false // Indica se a data tem uma reserva
)
