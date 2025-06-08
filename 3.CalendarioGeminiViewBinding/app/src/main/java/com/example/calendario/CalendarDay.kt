package com.example.calendario

import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate?, // Pode ser nulo para preencher espaços vazios no início/fim do mês
    val isCurrentMonth: Boolean,
    var isSelected: Boolean = false
)