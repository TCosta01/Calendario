// src/main/java/com/example/calendario/MainActivity.kt
package com.example.calendario

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), OnDateSelectedListener {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var selectedDatesTextView: TextView
    private lateinit var doneButton: Button
    private lateinit var databaseHelper: DatabaseHelper

    private val currentSelectedDates = mutableSetOf<Triple<Int, Int, Int>>() // Armazena as datas selecionadas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView = findViewById(R.id.calendarView)
        selectedDatesTextView = findViewById(R.id.selectedDatesTextView)
        doneButton = findViewById(R.id.doneButton)
        databaseHelper = DatabaseHelper(this)

        calendarView.setOnDateChangedListener(this)

        markBookedDates() // Marca as datas já reservadas do banco de dados

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

        // Alterna o estado da data no nosso conjunto de seleções
        if (currentSelectedDates.contains(selectedDateTriple)) {
            currentSelectedDates.remove(selectedDateTriple)
        } else {
            currentSelectedDates.add(selectedDateTriple)
        }

        // Limpa a seleção interna da MaterialCalendarView para não interferir
        // e force a redesenho para aplicar nossos decoradores personalizados
        widget.clearSelection()
        widget.invalidateDecorators() // Isso remove todos os decoradores existentes e permite re-aplicá-los

        // Re-aplica o decorador para as datas no currentSelectedDates (as datas que o usuário clicou)
        val selectedCalendarDays = currentSelectedDates.map { CalendarDay.from(it.third, it.second - 1, it.first) }
        // Use uma cor diferente para datas selecionadas (não reservadas) se quiser.
        // Aqui, estou usando a mesma cor, mas você pode definir outra constante.
        widget.addDecorator(EventDecorator(Color.parseColor("#FFBB86FC"), selectedCalendarDays))

        // Adiciona novamente os decoradores para as datas JÁ RESERVADAS (para não perdê-los)
        markBookedDates()

        updateSelectedDatesText() // Atualiza o texto das datas selecionadas
    }

    private fun markBookedDates() {
        calendarView.removeDecorators() // Remove todos os decoradores existentes para evitar duplicação

        val bookedDates = databaseHelper.getAllBookings()
        val calendarDays = bookedDates.map { CalendarDay.from(it.ano, it.mes - 1, it.dia) }

        // Aplica o decorador para as datas que já estão reservadas
        calendarView.addDecorator(EventDecorator(Color.parseColor("#FFBB86FC"), calendarDays))
    }

    private fun updateSelectedDatesText() {
        if (currentSelectedDates.isEmpty()) {
            selectedDatesTextView.text = "Datas selecionadas: Nenhuma"
        } else {
            val datesText = currentSelectedDates.sortedWith(compareBy({ it.third }, { it.second }, { it.first }))
                .joinToString(", ") { "${it.first}/${it.second}/${it.third}" }
            selectedDatesTextView.text = "Datas selecionadas: $datesText"
        }
    }

    private fun handleDoneButtonClick() {
        if (currentSelectedDates.isEmpty()) {
            Toast.makeText(this, "Por favor, selecione uma data para reservar.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, BookingActivity::class.java).apply {
            // Passa todas as datas selecionadas para a BookingActivity
            putExtra("selectedDates", ArrayList(currentSelectedDates))
            // isEditMode agora é tratado dentro da BookingActivity com base nas datas.
            putExtra("isEditMode", false) // Assume que é uma tentativa de nova reserva para as datas selecionadas
        }
        startActivity(intent)
    }
}