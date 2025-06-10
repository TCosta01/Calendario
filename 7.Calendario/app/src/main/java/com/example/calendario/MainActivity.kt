// src/main/java/com/yourpackage/yourapp/MainActivity.kt
package com.example.calendario // Substitua pelo seu package

// src/main/java/com/yourpackage/yourapp/MainActivity.kt


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

    // Usaremos um Set para armazenar as datas selecionadas para permitir múltiplas seleções
    // mesmo que a UI do MaterialCalendarView seja configurada para 'single' no XML
    // Isso é para a lógica de "Done" para novas reservas.
    private val currentSelectedDates = mutableSetOf<Triple<Int, Int, Int>>() // Triple(dia, mes, ano)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView = findViewById(R.id.calendarView)
        selectedDatesTextView = findViewById(R.id.selectedDatesTextView)
        doneButton = findViewById(R.id.doneButton)
        databaseHelper = DatabaseHelper(this)

        // Configura o listener para cliques nas datas do calendário
        calendarView.setOnDateChangedListener(this)

        // Inicializa o calendário com as datas reservadas
        markBookedDates()

        doneButton.setOnClickListener {
            handleDoneButtonClick()
        }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza o calendário sempre que a MainActivity for retomada (após cadastro/edição/exclusão)
        markBookedDates()
        currentSelectedDates.clear() // Limpa as datas selecionadas para uma nova interação
        updateSelectedDatesText()
    }

    // Implementação da interface OnDateSelectedListener
    override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
        val selectedDate = Triple(date.day, date.month + 1, date.year) // month is 0-indexed in CalendarDay, so add 1

        if (selected) { // A data foi selecionada (clicada)
            // Lógica para lidar com o clique em uma data
            val existingBooking = databaseHelper.getBooking(selectedDate.first, selectedDate.second, selectedDate.third)

            val intent = Intent(this, BookingActivity::class.java).apply {
                // Sempre passamos a data clicada para a BookingActivity
                putExtra("selectedDates", arrayListOf(selectedDate)) // Passamos como ArrayList<Triple>

                if (existingBooking != null) {
                    // Data já reservada, passamos os dados existentes para edição
                    putExtra("isEditMode", true)
                    putExtra("bookingData", existingBooking)
                } else {
                    // Data não reservada, modo de cadastro
                    putExtra("isEditMode", false)
                }
            }
            startActivity(intent)

            // Após o clique, desmarca a data no calendário para evitar confusão visual com múltiplas seleções
            // Opcional, dependendo de como você quer que o "Done" funcione
            widget.clearSelection()
            currentSelectedDates.clear()
            updateSelectedDatesText()

        } else { // A data foi desmarcada (se a seleção múltipla fosse permitida)
            // Para nosso caso de "single" selectionMode, isso raramente será chamado para desmarcar.
            // A lógica de clique já lida com o redirecionamento.
            currentSelectedDates.remove(selectedDate)
            updateSelectedDatesText()
        }
    }

    private fun markBookedDates() {
        calendarView.removeDecorators() // Remove decoradores anteriores para evitar duplicação
        val bookedDates = databaseHelper.getAllBookings()
        val calendarDays = bookedDates.map { CalendarDay.from(it.ano, it.mes -1, it.dia) } // month is 0-indexed in CalendarDay

        // Adiciona o decorador para as datas reservadas
        calendarView.addDecorator(EventDecorator(Color.parseColor("#FFBB86FC"), calendarDays)) // Use sua cor preferida
    }

    private fun updateSelectedDatesText() {
        // No cenário de clique em datas, o TextView abaixo não será tão relevante para múltiplas seleções
        // pois a lógica de clique já redireciona. Ele é mais para quando o "Done" for acionado.
        if (currentSelectedDates.isEmpty()) {
            selectedDatesTextView.text = "Datas selecionadas: Nenhuma"
        } else {
            val datesText = currentSelectedDates.sortedWith(compareBy({ it.third }, { it.second }, { it.first }))
                .joinToString(", ") { "${it.first}/${it.second}/${it.third}" }
            selectedDatesTextView.text = "Datas selecionadas: $datesText"
        }
    }

    private fun handleDoneButtonClick() {
        // Esta lógica de "Done" agora será para quando o usuário **não clicou em uma data específica**,
        // mas quer iniciar uma nova reserva para uma data que ele talvez tenha "selecionado" manualmente
        // (embora a MaterialCalendarView em modo single desmarque a anterior).
        // Se a sua intenção é que o usuário clique em múltiplos dias e depois aperte Done,
        // você precisaria mudar o `app:mcv_selectionMode` para `multiple` e ajustar a lógica de `onDateSelected`
        // para adicionar/remover datas do `currentSelectedDates`.

        if (currentSelectedDates.isEmpty()) {
            Toast.makeText(this, "Por favor, selecione uma data para reservar.", Toast.LENGTH_SHORT).show()
            return
        }

        // Assume que o botão Done é para iniciar um NOVO cadastro
        // Mesmo que a MaterialCalendarView esteja em modo single, se o usuário clicou e depois clicou Done,
        // vamos pegar a última data clicada ou a primeira se houver.
        val firstSelectedDate = currentSelectedDates.first()
        val existingBooking = databaseHelper.getBooking(firstSelectedDate.first, firstSelectedDate.second, firstSelectedDate.third)

        val intent = Intent(this, BookingActivity::class.java).apply {
            putExtra("selectedDates", ArrayList(currentSelectedDates)) // Passa todas as datas que foram "selecionadas"
            if (existingBooking != null) {
                // Se a data já existe, o "Done" redireciona para edição dessa data
                putExtra("isEditMode", true)
                putExtra("bookingData", existingBooking)
            } else {
                // Se não existe, redireciona para cadastro
                putExtra("isEditMode", false)
            }
        }
        startActivity(intent)
    }
}