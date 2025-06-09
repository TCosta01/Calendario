// src/main/java/com/example/reservascalendario/ui/CalendarViewModel.kt
package com.example.reservascalendario.ui

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.calendario.ui.CalendarDay
import com.example.calendario.data.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val databaseHelper = DatabaseHelper(application)

    private val _currentMonth = MutableLiveData<YearMonth>()
    val currentMonth: LiveData<YearMonth> = _currentMonth

    private val _calendarDays = MutableLiveData<List<CalendarDay>>()
    val calendarDays: LiveData<List<CalendarDay>> = _calendarDays

    private val _selectedDates = MutableLiveData<MutableSet<LocalDate>>()
    val selectedDates: LiveData<MutableSet<LocalDate>> = _selectedDates

    // Novo: LiveData para as datas que estão reservadas no banco de dados
    private val _reservedDates = MutableLiveData<Set<LocalDate>>()
    val reservedDates: LiveData<Set<LocalDate>> = _reservedDates

    init {
        _currentMonth.value = YearMonth.now()
        _selectedDates.value = mutableSetOf()
        loadReservedDates() // Carregar datas reservadas ao iniciar
    }

    // Carrega as datas reservadas do banco de dados em uma corrotina
    fun loadReservedDates() {
        viewModelScope.launch(Dispatchers.IO) {
            val dates = databaseHelper.getAllReservedDates()
            _reservedDates.postValue(dates)
            generateCalendarDays() // Regenerar dias após carregar as reservas
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun goToNextMonth() {
        _currentMonth.value = _currentMonth.value?.plusMonths(1)
        generateCalendarDays()
    }

    @SuppressLint("NewApi")
    fun goToPreviousMonth() {
        _currentMonth.value = _currentMonth.value?.minusMonths(1)
        generateCalendarDays()
    }

    fun getMonthYearText(): String {
        return _currentMonth.value?.format(DateTimeFormatter.ofPattern("MMMM peruse", Locale("pt", "BR")))
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

    // Limpa a seleção de dias (útil após uma reserva ser feita ou cancelada)
    fun clearSelectedDates() {
        _selectedDates.value = mutableSetOf()
        generateCalendarDays()
    }

    // Gera os dias do calendário, incluindo o estado de reserva
    fun generateCalendarDays() { // Mudado para public para ser chamado explicitamente em MainActivity
        val month = _currentMonth.value ?: return
        val daysInMonth = month.lengthOfMonth()
        val firstDayOfMonth = month.atDay(1)
        val lastDayOfMonth = month.atEndOfMonth()
        val currentReservedDates = _reservedDates.value ?: emptySet() // Obter as datas reservadas

        val days = mutableListOf<CalendarDay>()

        // Adicionar dias do mês anterior para preencher a primeira semana
        // O calendário começa na SEGUNDA-FEIRA, ajustamos para isso
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek
        val daysToAddBefore = if (firstDayOfWeek == DayOfWeek.MONDAY) 0 else firstDayOfWeek.value - DayOfWeek.MONDAY.value
        if (daysToAddBefore < 0) { // Se o primeiro dia for domingo, ele será 7 (ou 1 se for segunda)
            // Ajuste para garantir que a segunda-feira seja o primeiro dia da semana
            // DayOfWeek.MONDAY.value é 1. DayOfWeek.SUNDAY.value é 7.
            // Se firstDayOfWeek é domingo (7), daysToAddBefore seria 7-1=6.
            // Se firstDayOfWeek é segunda (1), daysToAddBefore seria 1-1=0.
            // A lógica atual já funciona bem com o valor int de DayOfWeek.
        }

        for (i in daysToAddBefore downTo 1) {
            days.add(CalendarDay(firstDayOfMonth.minusDays(i.toLong()), false))
        }

        // Adicionar dias do mês atual
        for (i in 1..daysInMonth) {
            val date = month.atDay(i)
            val isSelected = _selectedDates.value?.contains(date) ?: false
            val isReserved = currentReservedDates.contains(date) // Verifica se a data está reservada
            days.add(CalendarDay(date, true, isSelected, isReserved))
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
