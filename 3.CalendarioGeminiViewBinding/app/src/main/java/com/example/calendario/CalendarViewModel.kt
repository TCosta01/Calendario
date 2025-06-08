package com.example.calendario

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CalendarViewModel : ViewModel() {

    private val _currentMonth = MutableLiveData<YearMonth>()
    val currentMonth: LiveData<YearMonth> = _currentMonth

    private val _calendarDays = MutableLiveData<List<CalendarDay>>()
    val calendarDays: LiveData<List<CalendarDay>> = _calendarDays

    private val _selectedDates = MutableLiveData<MutableSet<LocalDate>>()
    val selectedDates: LiveData<MutableSet<LocalDate>> = _selectedDates

    init {
        _currentMonth.value = YearMonth.now()
        _selectedDates.value = mutableSetOf()
        generateCalendarDays()
    }

    fun goToNextMonth() {
        _currentMonth.value = _currentMonth.value?.plusMonths(1)
        generateCalendarDays()
    }

    fun goToPreviousMonth() {
        _currentMonth.value = _currentMonth.value?.minusMonths(1)
        generateCalendarDays()
    }

    fun getMonthYearText(): String {
        return _currentMonth.value?.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR")))
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() } ?: ""
    }

    fun toggleDaySelection(date: LocalDate) {
        val currentSelected = _selectedDates.value ?: mutableSetOf()
        if (currentSelected.contains(date)) {
            currentSelected.remove(date)
        } else {
            currentSelected.add(date)
        }
        _selectedDates.value = currentSelected
        generateCalendarDays() // Regenerar para atualizar o estado de seleção na UI
    }

    private fun generateCalendarDays() {
        val month = _currentMonth.value ?: return
        val daysInMonth = month.lengthOfMonth()
        val firstDayOfMonth = month.atDay(1)
        val lastDayOfMonth = month.atEndOfMonth()

        val days = mutableListOf<CalendarDay>()

        // Adicionar dias do mês anterior para preencher a primeira semana
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek
        val daysToAddBefore = if (firstDayOfWeek == DayOfWeek.MONDAY) 0 else firstDayOfWeek.value - 1 // Mês começa na segunda, não adiciona dias
        for (i in daysToAddBefore downTo 1) {
            days.add(CalendarDay(firstDayOfMonth.minusDays(i.toLong()), false))
        }

        // Adicionar dias do mês atual
        for (i in 1..daysInMonth) {
            val date = month.atDay(i)
            val isSelected = _selectedDates.value?.contains(date) ?: false
            days.add(CalendarDay(date, true, isSelected))
        }

        // Adicionar dias do próximo mês para preencher a última semana
        val totalCells = 42 // 6 semanas * 7 dias = 42 células (pode ser 35 ou 42 dependendo do mês)
        val daysToAddAfter = totalCells - days.size
        for (i in 1..daysToAddAfter) {
            days.add(CalendarDay(lastDayOfMonth.plusDays(i.toLong()), false))
        }

        _calendarDays.value = days
    }
}