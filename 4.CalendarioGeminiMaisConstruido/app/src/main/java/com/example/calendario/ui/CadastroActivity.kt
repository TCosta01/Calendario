// src/main/java/com/example/reservascalendario/ui/CadastroActivity.kt
package com.example.reservascalendario.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.calendario.data.DatabaseHelper
import com.example.calendario.databinding.ActivityCadastroBinding
import com.example.reservascalendario.data.DatabaseHelper
import com.example.reservascalendario.data.Reserva
import com.example.reservascalendario.databinding.ActivityCadastroBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var selectedDates: List<LocalDate> // Datas originalmente selecionadas na MainActivity
    private var isSingleReservedDateSelected: Boolean = false // Indica se apenas uma data reservada foi selecionada

    // Para a funcionalidade de alteração de data única
    private var originalDateForUpdate: LocalDate? = null
    private var newDateForUpdate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        // Obter as datas selecionadas da Intent
        val selectedDatesStrings = intent.getStringArrayListExtra("selected_dates")
        if (selectedDatesStrings.isNullOrEmpty()) {
            Toast.makeText(this, "Nenhuma data selecionada para cadastro.", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish() // Fecha a Activity se não houver datas
            return
        }
        selectedDates = selectedDatesStrings.map { LocalDate.parse(it) }

        // Obter o tipo de fluxo (se é uma única data reservada para alteração)
        isSingleReservedDateSelected = intent.getBooleanExtra("is_single_reserved_date_selected", false)

        // Se for um fluxo de única data reservada, definimos a data original e a nova data para update
        if (isSingleReservedDateSelected) {
            originalDateForUpdate = selectedDates.first()
            newDateForUpdate = originalDateForUpdate // Inicialmente a nova data é a mesma que a original
        }

        displaySelectedDates()
        setupUIBasedOnFlow()
        setupListeners()
    }

    private fun displaySelectedDates() {
        // Formatar as datas selecionadas para exibição
        val formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
        val datesText = selectedDates.joinToString(separator = ", ") { date ->
            "${date.dayOfMonth} de ${date.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))} de ${date.year}"
        }
        binding.selectedDatesTextView.text = "Datas Selecionadas: $datesText"
    }

    private fun setupUIBasedOnFlow() {
        if (isSingleReservedDateSelected) {
            // Fluxo de ÚNICA DATA RESERVADA (Atualizar/Excluir, e permitir Alterar Data)
            binding.buttonInserir.visibility = View.GONE
            binding.buttonAtualizar.visibility = View.VISIBLE
            binding.buttonExcluir.visibility = View.VISIBLE
            binding.changeDateContainer.visibility = View.VISIBLE

            // Preencher campos com dados da reserva existente
            val existingReserva = databaseHelper.getReservaByDate(
                originalDateForUpdate!!.dayOfMonth,
                originalDateForUpdate!!.monthValue,
                originalDateForUpdate!!.year
            )
            existingReserva?.let {
                binding.editTextNome.setText(it.nome)
                binding.editTextNumero.setText(it.numero)
                binding.editTextEndereco.setText(it.endereco)
                binding.editTextDescricao.setText(it.descricao)
                binding.editTextValor.setText(String.format(Locale.getDefault(), "%.2f", it.valor))
            }
            binding.currentSelectedDateForUpdate.text = "Data atual da reserva: ${originalDateForUpdate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"

        } else if (intent.getBooleanExtra("has_any_reserved_dates", false)) {
            // Fluxo de MÚLTIPLAS DATAS (algumas já reservadas) - Apenas atualizar/excluir detalhes, sem mudar datas
            binding.buttonInserir.visibility = View.GONE // Não insere novas aqui, apenas atualiza/exclui
            binding.buttonAtualizar.visibility = View.VISIBLE
            binding.buttonExcluir.visibility = View.VISIBLE
            binding.changeDateContainer.visibility = View.GONE // Não mostra opção de mudar data

            // Tenta preencher campos com dados da primeira data reservada selecionada, se houver
            val firstReservedDate = selectedDates.firstOrNull { date ->
                databaseHelper.getReservaByDate(date.dayOfMonth, date.monthValue, date.year) != null
            }
            firstReservedDate?.let { date ->
                val existingReserva = databaseHelper.getReservaByDate(date.dayOfMonth, date.monthValue, date.year)
                existingReserva?.let {
                    binding.editTextNome.setText(it.nome)
                    binding.editTextNumero.setText(it.numero)
                    binding.editTextEndereco.setText(it.endereco)
                    binding.editTextDescricao.setText(it.descricao)
                    binding.editTextValor.setText(String.format(Locale.getDefault(), "%.2f", it.valor))
                }
            }

        } else {
            // Fluxo de datas NÃO SALVAS (Inserir)
            binding.buttonInserir.visibility = View.VISIBLE
            binding.buttonAtualizar.visibility = View.GONE
            binding.buttonExcluir.visibility = View.GONE
            binding.changeDateContainer.visibility = View.GONE
            // Campos de texto já estão vazios por padrão
        }
    }

    private fun setupListeners() {
        binding.buttonInserir.setOnClickListener {
            insertReservas()
        }

        binding.buttonAtualizar.setOnClickListener {
            if (isSingleReservedDateSelected) {
                updateSingleReservaWithDateChange() // Lógica para alterar data única
            } else {
                updateMultipleReservas() // Lógica para atualizar detalhes de múltiplas reservas
            }
        }

        binding.buttonExcluir.setOnClickListener {
            deleteReservas()
        }

        binding.changeDateButton.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance().apply {
            newDateForUpdate?.let {
                set(it.year, it.monthValue - 1, it.dayOfMonth) // Month is 0-indexed in Calendar
            } ?: run {
                // If newDateForUpdate is null, use current selected date
                originalDateForUpdate?.let {
                    set(it.year, it.monthValue - 1, it.dayOfMonth)
                } ?: run {
                    // Fallback to current date if all else fails
                    set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)
                }
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val newPickedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                // Verifica se a nova data é diferente da original e não está já reservada
                if (newPickedDate == originalDateForUpdate) {
                    Toast.makeText(this, "A data selecionada é a mesma da reserva atual.", Toast.LENGTH_SHORT).show()
                } else if (databaseHelper.getReservaByDate(newPickedDate.dayOfMonth, newPickedDate.monthValue, newPickedDate.year) != null) {
                    Toast.makeText(this, "A data ${newPickedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} já está reservada. Por favor, escolha outra data.", Toast.LENGTH_LONG).show()
                } else {
                    newDateForUpdate = newPickedDate
                    binding.currentSelectedDateForUpdate.text = "Nova data: ${newDateForUpdate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                    Toast.makeText(this, "Data para atualização definida para: ${newDateForUpdate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", Toast.LENGTH_SHORT).show()
                }
            },
            year, month, day
        )
        datePickerDialog.show()
    }


    private fun insertReservas() {
        val nome = binding.editTextNome.text.toString().trim()
        val numero = binding.editTextNumero.text.toString().trim()
        val endereco = binding.editTextEndereco.text.toString().trim()
        val descricao = binding.editTextDescricao.text.toString().trim()
        val valorString = binding.editTextValor.text.toString().trim()

        if (nome.isEmpty() || numero.isEmpty() || endereco.isEmpty() || valorString.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha nome, número, endereço e valor.", Toast.LENGTH_SHORT).show()
            return
        }

        val valor = valorString.toDoubleOrNull()
        if (valor == null) {
            Toast.makeText(this, "Por favor, insira um valor numérico válido.", Toast.LENGTH_SHORT).show()
            return
        }

        // Inserir cada data selecionada como uma nova reserva
        var successCount = 0
        for (date in selectedDates) {
            val reserva = Reserva(date.dayOfMonth, date.monthValue, date.year, nome, numero, endereco, descricao, valor)
            val id = databaseHelper.insertReserva(reserva)
            if (id != -1L) {
                successCount++
            }
        }

        if (successCount == selectedDates.size) {
            Toast.makeText(this, "Reservas inseridas com sucesso!", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK) // Indica sucesso para MainActivity
            finish()
        } else {
            Toast.makeText(this, "Erro ao inserir algumas reservas. Verifique se a data já está reservada.", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateMultipleReservas() {
        val nome = binding.editTextNome.text.toString().trim()
        val numero = binding.editTextNumero.text.toString().trim()
        val endereco = binding.editTextEndereco.text.toString().trim()
        val descricao = binding.editTextDescricao.text.toString().trim()
        val valorString = binding.editTextValor.text.toString().trim()

        if (nome.isEmpty() || numero.isEmpty() || endereco.isEmpty() || valorString.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha nome, número, endereço e valor.", Toast.LENGTH_SHORT).show()
            return
        }

        val valor = valorString.toDoubleOrNull()
        if (valor == null) {
            Toast.makeText(this, "Por favor, insira um valor numérico válido.", Toast.LENGTH_SHORT).show()
            return
        }

        // Atualizar apenas as datas que já estavam reservadas originalmente
        var successCount = 0
        for (date in selectedDates) {
            if (databaseHelper.getReservaByDate(date.dayOfMonth, date.monthValue, date.year) != null) {
                val reserva = Reserva(date.dayOfMonth, date.monthValue, date.year, nome, numero, endereco, descricao, valor)
                val rowsAffected = databaseHelper.updateReserva(reserva)
                if (rowsAffected > 0) {
                    successCount++
                }
            }
        }

        if (successCount > 0) {
            Toast.makeText(this, "Reservas atualizadas com sucesso!", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Nenhuma reserva existente foi atualizada.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSingleReservaWithDateChange() {
        val nome = binding.editTextNome.text.toString().trim()
        val numero = binding.editTextNumero.text.toString().trim()
        val endereco = binding.editTextEndereco.text.toString().trim()
        val descricao = binding.editTextDescricao.text.toString().trim()
        val valorString = binding.editTextValor.text.toString().trim()

        if (nome.isEmpty() || numero.isEmpty() || endereco.isEmpty() || valorString.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha nome, número, endereço e valor.", Toast.LENGTH_SHORT).show()
            return
        }

        val valor = valorString.toDoubleOrNull()
        if (valor == null) {
            Toast.makeText(this, "Por favor, insira um valor numérico válido.", Toast.LENGTH_SHORT).show()
            return
        }

        val oldDate = originalDateForUpdate
        val newDate = newDateForUpdate

        if (oldDate == null) {
            Toast.makeText(this, "Erro: Data original não encontrada.", Toast.LENGTH_SHORT).show()
            return
        }
        if (newDate == null) {
            Toast.makeText(this, "Erro: Nenhuma nova data selecionada.", Toast.LENGTH_SHORT).show()
            return
        }

        // Se a data não mudou, apenas atualize os detalhes
        if (oldDate == newDate) {
            val reserva = Reserva(oldDate.dayOfMonth, oldDate.monthValue, oldDate.year, nome, numero, endereco, descricao, valor)
            val rowsAffected = databaseHelper.updateReserva(reserva)
            if (rowsAffected > 0) {
                Toast.makeText(this, "Reserva atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Erro ao atualizar reserva. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Se a data mudou, chame moveReserva
            try {
                val updatedReserva = Reserva(0, 0, 0, nome, numero, endereco, descricao, valor) // Data temporária, será substituída em moveReserva
                val success = databaseHelper.moveReserva(oldDate, newDate, updatedReserva)
                if (success) {
                    Toast.makeText(this, "Reserva movida e atualizada para ${newDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}!", Toast.LENGTH_LONG).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "Erro ao mover/atualizar reserva. Tente novamente.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IllegalStateException) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Erro inesperado ao mover/atualizar reserva: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteReservas() {
        // Excluir todas as datas selecionadas
        val rowsAffected = databaseHelper.deleteReservasByDates(selectedDates)

        if (rowsAffected > 0) {
            Toast.makeText(this, "$rowsAffected reservas excluídas com sucesso!", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK) // Indica sucesso para MainActivity
            finish()
        } else {
            Toast.makeText(this, "Nenhuma reserva encontrada para as datas selecionadas.", Toast.LENGTH_SHORT).show()
        }
    }
}
