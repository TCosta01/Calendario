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

class MainActivity : AppCompatActivity(), OnDateSelectedListener {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var selectedDatesTextView: TextView
    private lateinit var doneButton: Button
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var currentMonthYearTextView: TextView // Novo TextView para Mês/Ano

    // Armazena as datas que o usuário CLICOU para uma NOVA RESERVA
    private val currentSelectedDates = mutableSetOf<Triple<Int, Int, Int>>() // Triple(dia, mes, ano)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView = findViewById(R.id.calendarView)
        selectedDatesTextView = findViewById(R.id.selectedDatesTextView)
        doneButton = findViewById(R.id.doneButton)
        databaseHelper = DatabaseHelper(this)
        currentMonthYearTextView = findViewById(R.id.currentMonthYearTextView) // Inicializa o TextView

        calendarView.setOnDateChangedListener(this)

        // Configura o clique no TextView para abrir o seletor de mês/ano
        currentMonthYearTextView.setOnClickListener {
            showMonthYearPickerDialog()
        }

        // Configura o listener para mudanças de página (mês) no calendário
        calendarView.setOnMonthChangedListener { widget, date ->
            updateMonthYearDisplay(date)
            markBookedDates() // Redesenha os decoradores quando o mês muda
        }

        // Inicializa a exibição do mês/ano e marca as datas
        updateMonthYearDisplay(calendarView.currentDate)
        markBookedDates()


        doneButton.setOnClickListener {
            handleDoneButtonClick()
        }
    }

    override fun onResume() {
        super.onResume()
        // Isso garante que o calendário seja atualizado toda vez que a MainActivity volta ao foco
        markBookedDates()
        currentSelectedDates.clear() // Limpa as seleções ao retornar, para evitar confusão
        updateSelectedDatesText()
    }

    override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
        val selectedDay = date.day
        val selectedMonth = date.month + 1 // MaterialCalendarView usa 0-11 para meses
        val selectedYear = date.year

        val dateTriple = Triple(selectedDay, selectedMonth, selectedYear)

        if (selected) {
            currentSelectedDates.add(dateTriple)
            // Se o modo de seleção for SINGLE no XML, ele naturalmente desmarca a data anterior.
            // Se for MULTIPLE, você precisaria adicionar e remover manualmente.
        } else {
            currentSelectedDates.remove(dateTriple)
        }
        updateSelectedDatesText()
    }

    private fun updateMonthYearDisplay(date: CalendarDay) {
        val calendar = Calendar.getInstance()
        calendar.set(date.year, date.month, date.day) // month já é 0-indexed aqui
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
        currentMonthYearTextView.text = dateFormat.format(calendar.time).capitalize()
    }


    private fun markBookedDates() {
        calendarView.removeDecorators() // Remove decoradores antigos
        val bookedDates = databaseHelper.getAllBookings()
        val calendarDays = bookedDates.map { CalendarDay.from(it.ano, it.mes - 1, it.dia) }

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

    private fun showMonthYearPickerDialog() {
        val currentYear = calendarView.currentDate.year
        val currentMonth = calendarView.currentDate.month // 0-indexed

        val years = (currentYear - 5..currentYear + 5).toList() // Exemplo: 5 anos para trás e 5 para frente
        val yearNames = years.map { it.toString() }.toTypedArray()

        val monthNames = arrayOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )

        val yearBuilder = AlertDialog.Builder(this)
        yearBuilder.setTitle("Selecione o Ano")
        yearBuilder.setItems(yearNames) { dialog, which ->
            val selectedYear = years[which]
            dialog.dismiss()
            showMonthPickerDialog(selectedYear) // Passa o ano selecionado para o próximo dialog
        }
        yearBuilder.create().show()
    }

    private fun showMonthPickerDialog(year: Int) {
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

        // Primeiro, verifica se as datas selecionadas para a NOVA reserva já possuem reservas
        val existingBookingsForSelectedDates = currentSelectedDates.mapNotNull { (day, month, year) ->
            databaseHelper.getBooking(day, month, year)
        }

        if (existingBookingsForSelectedDates.isNotEmpty()) {
            // Se alguma das datas selecionadas já tem uma reserva,
            // considera que o usuário quer editar uma reserva existente
            val firstExistingBooking = existingBookingsForSelectedDates.first()
            val intent = Intent(this, BookingActivity::class.java).apply {
                putExtra("isEditMode", true)
                // Passa o bookingGroupId para buscar todas as datas desse grupo
                putExtra("bookingGroupId", firstExistingBooking.bookingGroupId)
            }
            startActivity(intent)

        } else {
            // Se nenhuma das datas selecionadas já tem uma reserva,
            // considera que o usuário quer criar uma nova reserva para essas datas
            val intent = Intent(this, BookingActivity::class.java).apply {
                putExtra("selectedDatesForNewBooking", ArrayList(currentSelectedDates)) // Passa as datas para uma NOVA reserva
                putExtra("isEditMode", false) // Indica que é uma nova reserva
            }
            startActivity(intent)
        }
    }
}