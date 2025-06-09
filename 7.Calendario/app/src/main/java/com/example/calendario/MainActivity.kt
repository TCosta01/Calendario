// src/main/java/com/yourpackage/yourapp/MainActivity.kt
package com.example.calendario // Substitua pelo seu package

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var selectedDatesTextView: TextView
    private lateinit var doneButton: Button
    private lateinit var databaseHelper: DatabaseHelper

    // Usaremos um Set para armazenar as datas selecionadas para permitir múltiplas seleções
    private val selectedDates = mutableSetOf<Triple<Int, Int, Int>>() // Triple(dia, mes, ano)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView = findViewById(R.id.calendarView)
        selectedDatesTextView = findViewById(R.id.selectedDatesTextView)
        doneButton = findViewById(R.id.doneButton)
        databaseHelper = DatabaseHelper(this)

        // Marcar as datas reservadas ao iniciar o app
        markBookedDates()

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // O CalendarView padrão não suporta seleção de múltiplas datas diretamente.
            // Para isso, você precisaria de um calendário customizado ou uma biblioteca.
            // Para este exemplo, vou simular a seleção de uma única data por vez
            // ou permitir que o usuário adicione/remova datas manualmente do set.
            val selectedDate = Triple(dayOfMonth, month + 1, year) // month is 0-indexed

            if (selectedDates.contains(selectedDate)) {
                selectedDates.remove(selectedDate)
            } else {
                selectedDates.add(selectedDate)
            }
            updateSelectedDatesText()
        }

        doneButton.setOnClickListener {
            handleDoneButtonClick()
        }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza o calendário sempre que a MainActivity for retomada (após cadastro/edição/exclusão)
        markBookedDates()
        selectedDates.clear() // Limpa as datas selecionadas para uma nova interação
        updateSelectedDatesText()
    }

    private fun markBookedDates() {
        val bookedDates = databaseHelper.getAllBookings()
        // No CalendarView nativo, não há uma forma direta de "sombrear" datas.
        // Você teria que sobrescrever o draw do CalendarView ou, como mencionado,
        // usar uma biblioteca de calendário mais robusta para essa funcionalidade.
        // Para este exemplo, a funcionalidade de verificação de datas reservadas
        // será para redirecionar o usuário corretamente.
        // Se estiver usando uma biblioteca como MaterialCalendarView, aqui você chamaria:
        // calendarView.addDecorator(EventDecorator(bookedDates.map { EventDay(it.dia, it.mes, it.ano) }))
        // Para este exemplo, vamos apenas logar ou ter uma lista interna.
        val bookedDateStrings = bookedDates.map { "${it.dia}/${it.mes}/${it.ano}" }
        println("Datas reservadas: $bookedDateStrings")
        // Se você usar uma biblioteca, é aqui que você aplicaria os decoradores visuais.
    }

    private fun updateSelectedDatesText() {
        if (selectedDates.isEmpty()) {
            selectedDatesTextView.text = "Datas selecionadas: Nenhuma"
        } else {
            val datesText = selectedDates.sortedWith(compareBy({ it.third }, { it.second }, { it.first }))
                .joinToString(", ") { "${it.first}/${it.second}/${it.third}" }
            selectedDatesTextView.text = "Datas selecionadas: $datesText"
        }
    }

    private fun handleDoneButtonClick() {
        if (selectedDates.isEmpty()) {
            Toast.makeText(this, "Por favor, selecione uma data.", Toast.LENGTH_SHORT).show()
            return
        }

        // Para simplificar, vamos considerar a primeira data selecionada para a verificação.
        // Se múltiplas datas forem selecionadas, você precisaria de uma lógica mais complexa
        // para decidir se todas estão reservadas ou se é uma nova reserva.
        // Para o requisito, a edição de múltiplas datas é feita na tela de cadastro/edição.
        val firstSelectedDate = selectedDates.first()
        val day = firstSelectedDate.first
        val month = firstSelectedDate.second
        val year = firstSelectedDate.third

        val existingBooking = databaseHelper.getBooking(day, month, year)

        val intent = Intent(this, BookingActivity::class.java).apply {
            // Passa as datas selecionadas para a próxima Activity
            putExtra("selectedDates", ArrayList(selectedDates))

            if (existingBooking != null) {
                // Datas já reservadas, passamos os dados existentes para edição
                putExtra("isEditMode", true)
                putExtra("bookingData", existingBooking)
            } else {
                // Datas não reservadas, modo de cadastro
                putExtra("isEditMode", false)
            }
        }
        startActivity(intent)
    }
}