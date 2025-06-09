package com.example.calendario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var dbHelper: ReservaDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView = findViewById(R.id.calendarView)
        dbHelper = ReservaDbHelper(this)

        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_MULTIPLE

        // Destaque de dias com reservas
        lifecycleScope.launch {
            val reservas = withContext(Dispatchers.IO) { dbHelper.getTodasReservas() }
            val datas = reservas.map {
                CalendarDay.from(it.ano, it.mes - 1, it.dia)
            }.toSet()
            calendarView.addDecorator(ReservaDecorator(datas, ContextCompat.getColor(this@MainActivity, R.color.purple_500)))
        }

        val doneButton: Button = findViewById(R.id.btnDone)
        doneButton.setOnClickListener {
            val datasSelecionadas = calendarView.selectedDates
            if (datasSelecionadas.isEmpty()) return@setOnClickListener

            val intent = Intent(this, ReservaActivity::class.java)
            val dias = datasSelecionadas.map { it.day }.toIntArray()
            val meses = datasSelecionadas.map { it.month + 1 }.toIntArray()
            val anos = datasSelecionadas.map { it.year }.toIntArray()

            intent.putExtra("dias", dias)
            intent.putExtra("meses", meses)
            intent.putExtra("anos", anos)
            startActivity(intent)
        }
    }
}