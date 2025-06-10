// src/main/java/com/example/calendario/BookingActivity.kt
package com.example.calendario

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlin.collections.ArrayList

class BookingActivity : AppCompatActivity() {

    private lateinit var bookingDatesTextView: TextView
    private lateinit var nameEditText: TextInputEditText
    private lateinit var numberEditText: TextInputEditText
    private lateinit var addressEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var valueEditText: TextInputEditText
    private lateinit var actionButton: Button
    private lateinit var deleteButton: Button

    private lateinit var databaseHelper: DatabaseHelper
    private var isEditMode: Boolean = false // Reavaliar o uso exato com múltiplas datas
    private var existingBooking: Booking? = null // Pode ser menos relevante com múltiplas datas
    private lateinit var selectedDates: ArrayList<Triple<Int, Int, Int>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        bookingDatesTextView = findViewById(R.id.bookingDatesTextView)
        nameEditText = findViewById(R.id.nameEditText)
        numberEditText = findViewById(R.id.numberEditText)
        addressEditText = findViewById(R.id.addressEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        valueEditText = findViewById(R.id.valueEditText)
        actionButton = findViewById(R.id.actionButton)
        deleteButton = findViewById(R.id.deleteButton)

        databaseHelper = DatabaseHelper(this)

        selectedDates = intent.getSerializableExtra("selectedDates") as? ArrayList<Triple<Int, Int, Int>>
            ?: arrayListOf()

        isEditMode = intent.getBooleanExtra("isEditMode", false) // Mantido, mas sua lógica é adaptada

        // Exibir todas as datas selecionadas
        val datesText = selectedDates.sortedWith(compareBy({ it.third }, { it.second }, { it.first }))
            .joinToString(", ") { "${it.first}/${it.second}/${it.third}" }
        bookingDatesTextView.text = "Datas da Reserva: $datesText"

        // Lógica de preenchimento (se for uma "edição" ou nova inserção)
        // Se houver apenas uma data selecionada e ela já tiver uma reserva, preenche os campos.
        // Caso contrário, assume que é uma nova inserção para as datas.
        if (selectedDates.size == 1) {
            val singleSelectedDate = selectedDates.first()
            existingBooking = databaseHelper.getBooking(singleSelectedDate.first, singleSelectedDate.second, singleSelectedDate.third)
            existingBooking?.let {
                nameEditText.setText(it.nome)
                numberEditText.setText(it.numero)
                addressEditText.setText(it.endereco)
                descriptionEditText.setText(it.descricao)
                valueEditText.setText(it.valor.toString())
                actionButton.text = "Atualizar Reserva"
                deleteButton.visibility = View.VISIBLE
                isEditMode = true // Confirma o modo de edição para uma única data
            } ?: run {
                actionButton.text = "Inserir Reservas"
                deleteButton.visibility = View.GONE
                isEditMode = false // É uma nova reserva, mesmo que seja apenas uma data
            }
        } else {
            // Múltiplas datas selecionadas ou nenhuma data existente: sempre para inserção/substituição
            actionButton.text = "Inserir Reservas"
            deleteButton.visibility = View.GONE
            isEditMode = false // Desabilita o modo de edição tradicional
        }


        actionButton.setOnClickListener {
            // Lógica para salvar/atualizar múltiplas reservas
            insertOrUpdateBookings()
        }

        deleteButton.setOnClickListener {
            // Lógica para excluir múltiplas reservas
            if (selectedDates.isNotEmpty()) {
                deleteMultipleBookings()
            } else {
                Toast.makeText(this, "Nenhuma data selecionada para excluir.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun insertOrUpdateBookings() {
        val name = nameEditText.text.toString()
        val number = numberEditText.text.toString()
        val address = addressEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val value = valueEditText.text.toString().toDoubleOrNull()

        if (name.isBlank() || value == null) {
            Toast.makeText(this, "Por favor, preencha nome e valor.", Toast.LENGTH_SHORT).show()
            return
        }

        var allBookingsProcessed = true
        selectedDates.forEach { (dia, mes, ano) ->
            val newBooking = Booking(dia, mes, ano, name, number, address, description, value)

            // Lógica de "UPSERT" (Update ou Insert):
            // Tenta obter a reserva existente para esta data.
            val existing = databaseHelper.getBooking(dia, mes, ano)
            if (existing != null) {
                // Se existe, atualiza. (A sua função updateBooking faz um delete+insert).
                val rowsAffected = databaseHelper.updateBooking(existing, newBooking)
                if (rowsAffected == 0) {
                    allBookingsProcessed = false
                    // Opcional: Toast para a data específica que falhou a atualização
                }
            } else {
                // Se não existe, insere.
                val newRowId = databaseHelper.insertBooking(newBooking)
                if (newRowId == -1L) {
                    allBookingsProcessed = false
                    // Opcional: Toast para a data específica que falhou a inserção
                }
            }
        }

        if (allBookingsProcessed) {
            Toast.makeText(this, "Reservas processadas com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Ocorreu um erro ao processar algumas reservas.", Toast.LENGTH_LONG).show()
        }
    }


    private fun deleteMultipleBookings() {
        var allBookingsDeleted = true
        selectedDates.forEach { (dia, mes, ano) ->
            val rowsAffected = databaseHelper.deleteBooking(dia, mes, ano)
            if (rowsAffected == 0) {
                allBookingsDeleted = false
                // Opcional: mostrar Toast para a data específica que falhou a exclusão
            }
        }

        if (allBookingsDeleted) {
            Toast.makeText(this, "Reservas excluídas com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Ocorreu um erro ao excluir algumas reservas.", Toast.LENGTH_LONG).show()
        }
    }
}