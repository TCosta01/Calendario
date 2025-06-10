// src/main/java/com/example/calendario/MainActivity.kt
package com.example.calendario

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.example.calendario.EventDecorator

class MainActivity : AppCompatActivity(), OnDateSelectedListener {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var selectedDatesTextView: TextView
    private lateinit var doneButton: Button
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var currentMonthYearTextView: TextView // Novo TextView para Mês/Ano

    // Armazena as datas que o usuário CLICOU para uma NOVA RESERVA
    private val currentSelectedDates = mutableSetOf<Triple<Int, Int, Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView = findViewById(R.id.calendarView)
        selectedDatesTextView = findViewById(R.id.selectedDatesTextView)
        doneButton = findViewById(R.id.doneButton)
        currentMonthYearTextView = findViewById(R.id.currentMonthYearTextView) // Inicializar o novo TextView
        databaseHelper = DatabaseHelper(this)

        calendarView.setOnDateChangedListener(this)

        // Listener para mudanças de mês/ano no calendário
        calendarView.setOnMonthChangedListener { widget, date ->
            updateMonthYearDisplay(date)
        }

        currentMonthYearTextView.setOnClickListener {
            showMonthSelectionDialog()
        }

        markBookedDates() // Marca as datas já reservadas do banco de dados
        updateMonthYearDisplay(calendarView.currentDate) // Atualiza o display inicial

        doneButton.setOnClickListener {
            handleDoneButtonClick()
        }
    }

    override fun onResume() {
        super.onResume()
        // Isso é importante para re-renderizar as reservas se algo mudar em BookingActivity
        markBookedDates()
        // Limpa as seleções temporárias ao retornar
        currentSelectedDates.clear()
        updateSelectedDatesText()
        // Força a atualização dos decoradores para remover qualquer seleção anterior
        calendarView.clearSelection() // Limpa a seleção interna da MaterialCalendarView
        calendarView.invalidateDecorators() // Redesenha os decoradores
    }

    override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
        val selectedDateTriple = Triple(date.day, date.month + 1, date.year) // Mês é 0-based no CalendarDay

        // Primeiro, verifique se a data clicada JÁ TEM uma reserva
        val existingBookingForDay = databaseHelper.getBooking(selectedDateTriple.first, selectedDateTriple.second, selectedDateTriple.third)

        if (existingBookingForDay != null) {
            // Se a data clicada já está reservada, vá para a BookingActivity para EDITAR/VISUALIZAR o grupo
            val intent = Intent(this, BookingActivity::class.java).apply {
                putExtra("bookingGroupId", existingBookingForDay.bookingGroupId) // Passa o ID do grupo
                putExtra("isEditMode", true) // Indica que estamos em modo de edição
            }
            startActivity(intent)
            // Não adicione esta data ao currentSelectedDates, pois estamos visualizando/editando uma reserva existente
            return
        }

        // Se a data NÃO TEM reserva, então ela faz parte de uma NOVA seleção
        if (currentSelectedDates.contains(selectedDateTriple)) {
            currentSelectedDates.remove(selectedDateTriple)
        } else {
            currentSelectedDates.add(selectedDateTriple)
        }

        // Limpa a seleção interna da MaterialCalendarView para não interferir
        widget.clearSelection()
        widget.invalidateDecorators() // Isso remove todos os decoradores existentes e permite re-aplicá-los

        // Re-aplica o decorador para as datas no currentSelectedDates (as datas que o usuário clicou para uma NOVA reserva)
        val selectedCalendarDays = currentSelectedDates.map { CalendarDay.from(it.third, it.second - 1, it.first) }
        // Use uma cor diferente para datas selecionadas (não reservadas) se quiser.
        widget.addDecorator(EventDecorator(Color.parseColor("#80FFBB86"), selectedCalendarDays)) // Cor mais clara para seleção temporária

        // Adiciona novamente os decoradores para as datas JÁ RESERVADAS (para não perdê-los)
        markBookedDates()

        updateSelectedDatesText() // Atualiza o texto das datas selecionadas
    }

    private fun markBookedDates() {
        calendarView.removeDecorators() // Remove todos os decoradores existentes para evitar duplicação

        val bookedDates = databaseHelper.getAllBookings()
        val calendarDays = bookedDates.map { CalendarDay.from(it.ano, it.mes - 1, it.dia) }

        // Aplica o decorador para as datas que já estão reservadas
        calendarView.addDecorator(EventDecorator(Color.parseColor("#FFBB86FC"), calendarDays)) // Cor original para reservas
    }

    private fun updateSelectedDatesText() {
        if (currentSelectedDates.isEmpty()) {
            selectedDatesTextView.text = "Datas selecionadas: Nenhuma"
        } else {
            val datesText = currentSelectedDates.sortedWith(compareBy({ it.third }, { it.second }, { it.first }))
                .joinToString(", ") { "${String.format("%02d", it.first)}/${String.format("%02d", it.second)}/${it.third}" }
            selectedDatesTextView.text = "Datas selecionadas (Nova Reserva): $datesText"
        }
    }

    private fun updateMonthYearDisplay(calendarDay: CalendarDay) {
        val monthNames = arrayOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
        val monthName = monthNames[calendarDay.month] // calendarDay.month é 0-based
        currentMonthYearTextView.text = "$monthName ${calendarDay.year}"
    }

    private fun showMonthSelectionDialog() {
        val year = calendarView.currentDate.year
        val monthNames = arrayOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione o Mês ($year)")

        builder.setItems(monthNames) { dialog, which ->
            // `which` é o índice do mês selecionado (0 para Janeiro, 11 para Dezembro)
            val selectedMonth = which

            // Atualiza o calendário para o mês selecionado no ano atual
            val newCalendarDay = CalendarDay.from(year, selectedMonth, 1) // Define para o dia 1 do mês selecionado
            calendarView.setCurrentDate(newCalendarDay, true) // O segundo parâmetro `animated` pode ser true ou false
            updateMonthYearDisplay(newCalendarDay) // Atualiza o TextView do mês/ano
            markBookedDates() // Redesenha os decoradores para o novo mês
            currentSelectedDates.clear() // Limpa seleções temporárias ao mudar de mês
            updateSelectedDatesText()
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun handleDoneButtonClick() {
        if (currentSelectedDates.isEmpty()) {
            Toast.makeText(this, "Por favor, selecione uma data para reservar.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, BookingActivity::class.java).apply {
            putExtra("selectedDatesForNewBooking", ArrayList(currentSelectedDates)) // Passa as datas para uma NOVA reserva
            putExtra("isEditMode", false) // Indica que é uma nova reserva
        }
        startActivity(intent)
    }
}