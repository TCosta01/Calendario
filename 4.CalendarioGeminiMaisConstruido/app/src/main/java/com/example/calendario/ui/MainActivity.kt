// src/main/java/com/example/reservascalendario/ui/MainActivity.kt
package com.example.calendario.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.reservascalendario.data.DatabaseHelper
import com.example.reservascalendario.databinding.ActivityMainBinding
import com.example.reservascalendario.ui.CalendarViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val calendarViewModel: CalendarViewModel by viewModels()
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var databaseHelper: DatabaseHelper // Instância do DatabaseHelper

    // Launcher para iniciar CadastroActivity e obter resultado
    private val cadastroActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Se a operação na CadastroActivity foi bem-sucedida (inserir/atualizar/excluir/mover)
            // Recarregamos as datas reservadas para atualizar a UI do calendário
            calendarViewModel.loadReservedDates()
            calendarViewModel.clearSelectedDates() // Limpa qualquer seleção após a operação
            Toast.makeText(this, "Operação de reserva concluída!", Toast.LENGTH_SHORT).show()
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            // Se o usuário cancelou a operação na CadastroActivity
            calendarViewModel.clearSelectedDates() // Limpa qualquer seleção
            Toast.makeText(this, "Operação de reserva cancelada.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this) // Inicializa o DatabaseHelper

        setupCalendarRecyclerView()
        setupListeners()
        observeViewModel()

        // Carrega as datas reservadas assim que a Activity é criada
        calendarViewModel.loadReservedDates()
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
            if (selectedDates.isNullOrEmpty()) {
                Toast.makeText(this, "Por favor, selecione ao menos um dia.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Converter o Set<LocalDate> para List<String> para passar via Intent
            val selectedDatesStrings = ArrayList(selectedDates.map { it.toString() })

            // Verificar se ALGUMA das datas selecionadas já está reservada no banco
            val hasAnyReservedDates = selectedDates.any { date ->
                databaseHelper.getReservaByDate(date.dayOfMonth, date.monthValue, date.year) != null
            }

            // Verificar se SOMENTE UMA data reservada foi selecionada
            val isSingleReservedDateSelected = selectedDates.size == 1 && hasAnyReservedDates &&
                    databaseHelper.getReservaByDate(selectedDates.first().dayOfMonth, selectedDates.first().monthValue, selectedDates.first().year) != null


            val intent = Intent(this, CadastroActivity::class.java).apply {
                putStringArrayListExtra("selected_dates", selectedDatesStrings)
                // Passa true se alguma das datas selecionadas já está reservada
                putExtra("has_any_reserved_dates", hasAnyReservedDates)
                // Passa true se APENAS UMA data reservada foi selecionada
                putExtra("is_single_reserved_date_selected", isSingleReservedDateSelected)
            }
            cadastroActivityResultLauncher.launch(intent)
        }
    }

    private fun observeViewModel() {
        calendarViewModel.currentMonth.observe(this) {
            binding.monthYearText.text = calendarViewModel.getMonthYearText()
        }

        calendarViewModel.calendarDays.observe(this) { days ->
            calendarAdapter.updateDays(days)
        }

        calendarViewModel.reservedDates.observe(this) {
            // Quando as datas reservadas são carregadas ou atualizadas,
            // forçamos a regeneração dos dias do calendário para que o adapter
            // possa aplicar os novos estados 'isReserved' na UI.
            calendarViewModel.generateCalendarDays() // Chama o método interno do ViewModel
        }
    }

    // Você pode querer chamar loadReservedDates() em onResume() também,
    // para garantir que o calendário esteja atualizado se o usuário voltar para esta tela
    // sem passar por cadastroActivityResultLauncher.
    override fun onResume() {
        super.onResume()
        calendarViewModel.loadReservedDates()
    }
}
