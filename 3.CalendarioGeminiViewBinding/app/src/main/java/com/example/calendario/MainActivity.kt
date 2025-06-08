package com.example.calendario

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.calendario.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val calendarViewModel: CalendarViewModel by viewModels()
    private lateinit var calendarAdapter: CalendarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCalendarRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupCalendarRecyclerView() {
        calendarAdapter = CalendarAdapter(emptyList()) { calendarDay ->
            calendarDay.date?.let { date ->
                calendarViewModel.toggleDaySelection(date)
            }
        }
        binding.calendarRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 7) // 7 dias por semana
            adapter = calendarAdapter
        }
    }

    private fun setupListeners() {
        binding.previousMonthButton.setOnClickListener {
            calendarViewModel.goToPreviousMonth()
        }

        binding.nextMonthButton.setOnClickListener {
            calendarViewModel.goToNextMonth()
        }

        binding.doneButton.setOnClickListener {
            val selectedDates = calendarViewModel.selectedDates.value
            if (!selectedDates.isNullOrEmpty()) {
                val message = "Dias selecionados: ${selectedDates.joinToString { it.toString() }}"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                // Aqui você pode fazer algo com os dias selecionados, como enviá-los para outra Activity
            } else {
                Toast.makeText(this, "Nenhum dia selecionado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        calendarViewModel.currentMonth.observe(this) {
            binding.monthYearText.text = calendarViewModel.getMonthYearText()
        }

        calendarViewModel.calendarDays.observe(this) { days ->
            calendarAdapter.updateDays(days)
        }

        calendarViewModel.selectedDates.observe(this) { selectedDates ->
            // Para garantir que a UI reflita as seleções, force uma atualização dos dias
            // Isso pode ser otimizado para atualizar apenas os itens afetados se houver muitos dias
            calendarViewModel.calendarDays.value?.forEach { calendarDay ->
                calendarDay.isSelected = selectedDates.contains(calendarDay.date)
            }
            calendarViewModel.calendarDays.value?.let { calendarAdapter.updateDays(it) }
        }
    }
}