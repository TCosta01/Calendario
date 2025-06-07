package com.example.your_app_name // Mude para o nome do seu pacote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.calendario.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var tvMonthYear: TextView
    private lateinit var btnPreviousMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var rvCalendarDays: RecyclerView
    private lateinit var btnDone: Button

    private val calendar = Calendar.getInstance()
    private lateinit var calendarAdapter: CalendarAdapter
    private val selectedDates = mutableSetOf<Date>() // Para seleção múltipla

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvMonthYear = findViewById(R.id.tv_month_year)
        btnPreviousMonth = findViewById(R.id.btn_previous_month)
        btnNextMonth = findViewById(R.id.btn_next_month)
        rvCalendarDays = findViewById(R.id.rv_calendar_days)
        btnDone = findViewById(R.id.btn_done)

        setupCalendar()
        updateCalendarDisplay()

        btnPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendarDisplay()
        }

        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendarDisplay()
        }

        btnDone.setOnClickListener {
            if (selectedDates.isNotEmpty()) {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                val datesString = selectedDates.sortedBy { it.time }.joinToString(", ") { formatter.format(it) }
                Toast.makeText(this, "Datas Selecionadas: $datesString", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Nenhuma data selecionada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter(object : CalendarAdapter.OnDayClickListener {
            override fun onDayClick(date: Date?) {
                date?.let {
                    if (selectedDates.contains(it)) {
                        selectedDates.remove(it)
                    } else {
                        selectedDates.add(it)
                    }
                    calendarAdapter.notifyDataSetChanged() // Atualiza a view para mostrar a seleção
                }
            }
        }, selectedDates) // Passa selectedDates para o adapter

        rvCalendarDays.adapter = calendarAdapter
        // O layout manager já está definido no XML: app:layoutManager="androidx.recyclerview.widget.GridLayoutManager"
    }

    private fun updateCalendarDisplay() {
        val daysInMonth = ArrayList<Date?>()
        val currentMonthCalendar = calendar.clone() as Calendar
        currentMonthCalendar.set(Calendar.DAY_OF_MONTH, 1) // Define para o primeiro dia do mês

        // Obtém o dia da semana para o primeiro dia do mês (Segunda = 2, Domingo = 1)
        // Ajuste para o dia de início desejado (Segunda-feira como 1º dia da semana)
        val firstDayOfWeek = currentMonthCalendar.get(Calendar.DAY_OF_WEEK)
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2 // Ajuste para iniciar na Segunda-feira

        // Adiciona dias vazios no início para alinhamento
        for (i in 0 until offset) {
            daysInMonth.add(null)
        }

        // Adiciona os dias do mês atual
        val maxDays = currentMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDays) {
            val dayCalendar = currentMonthCalendar.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_MONTH, i)
            daysInMonth.add(dayCalendar.time)
        }

        // Adiciona dias vazios no final para completar a grade (opcional, mas bom para layout consistente)
        val totalCells = daysInMonth.size
        val remainingCells = 7 - (totalCells % 7)
        if (remainingCells > 0 && remainingCells < 7) {
            for (i in 0 until remainingCells) {
                daysInMonth.add(null)
            }
        }

        calendarAdapter.updateDays(newDays = daysInMonth) // Nomeando o parâmetro para clareza

        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
        tvMonthYear.text = monthYearFormat.format(calendar.time).capitalize(Locale("pt", "BR"))
    }
}

// --------------------- CalendarAdapter ---------------------

class CalendarAdapter(
    private val onDayClickListener: OnDayClickListener,
    private val selectedDates: MutableSet<Date> // Passa o conjunto de datas selecionadas
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var days = listOf<Date?>() // Usa Date anulável para células vazias

    interface OnDayClickListener {
        fun onDayClick(date: Date?)
    }

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDayNumber: TextView = itemView.findViewById(R.id.tv_day_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]

        if (day != null) {
            val dayOfMonth = SimpleDateFormat("d", Locale.getDefault()).format(day).toInt()
            holder.tvDayNumber.text = dayOfMonth.toString()
            holder.tvDayNumber.visibility = View.VISIBLE

            // Verifica se este dia está selecionado
            holder.tvDayNumber.isSelected = selectedDates.contains(day)

            holder.itemView.setOnClickListener {
                onDayClickListener.onDayClick(day)
            }

            // Opcional: Estilo para dias do mês atual vs. dias do mês anterior/próximo
            // Por enquanto, todos os dias exibidos são do mês atual sendo visualizado.
            // Se você estender para mostrar dias do mês anterior/próximo, adicionaria mais lógica aqui.

        } else {
            holder.tvDayNumber.text = ""
            holder.tvDayNumber.visibility = View.INVISIBLE // Oculta células vazias
            holder.itemView.setOnClickListener(null) // Desabilita cliques em células vazias
        }
    }

    override fun getItemCount(): Int = days.size

    fun updateDays(newDays: List<Date?>) {
        days = newDays
        notifyDataSetChanged()
    }
}