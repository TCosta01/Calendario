// src/main/java/com/example/calendario/BookingActivity.kt
package com.example.calendario

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.util.UUID // Importar para gerar IDs únicos para novos grupos
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
    private var isEditMode: Boolean = false
    private var currentBookingGroupId: String? = null // O ID do grupo de reservas que estamos editando/criando
    private var newBookingDates: ArrayList<Triple<Int, Int, Int>> = arrayListOf() // Datas para uma NOVA reserva

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

        // Verifica se estamos em modo de edição (clicou em uma data já reservada)
        currentBookingGroupId = intent.getStringExtra("bookingGroupId")
        isEditMode = currentBookingGroupId != null

        if (isEditMode) {
            // Carrega todas as reservas do grupo para preencher a UI
            currentBookingGroupId?.let { groupId ->
                val groupBookings = databaseHelper.getBookingsByGroupId(groupId)
                if (groupBookings.isNotEmpty()) {
                    // Preenche os campos com os dados da primeira reserva do grupo (assumindo que são todos iguais)
                    val firstBookingInGroup = groupBookings.first()
                    nameEditText.setText(firstBookingInGroup.nome)
                    numberEditText.setText(firstBookingInGroup.numero)
                    addressEditText.setText(firstBookingInGroup.endereco)
                    descriptionEditText.setText(firstBookingInGroup.descricao)
                    valueEditText.setText(firstBookingInGroup.valor.toString())

                    // Exibe todas as datas do grupo no TextView
                    val datesText = groupBookings.map { "${it.dia}/${it.mes}/${it.ano}" }
                        .sortedWith(compareBy({ it.split("/")[2].toInt() }, { it.split("/")[1].toInt() }, { it.split("/")[0].toInt() })) // Ordenar por ano, mes, dia
                        .joinToString(", ")
                    bookingDatesTextView.text = "Datas da Reserva (Grupo): $datesText"

                    actionButton.text = "Atualizar Reservas"
                    deleteButton.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, "Erro: Grupo de reserva não encontrado.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            // Se não é modo de edição, estamos criando uma nova reserva para as datas selecionadas
            newBookingDates = intent.getSerializableExtra("selectedDatesForNewBooking") as? ArrayList<Triple<Int, Int, Int>>
                ?: arrayListOf()

            if (newBookingDates.isEmpty()) {
                Toast.makeText(this, "Nenhuma data selecionada para nova reserva.", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            val datesText = newBookingDates.sortedWith(compareBy({ it.third }, { it.second }, { it.first }))
                .joinToString(", ") { "${it.first}/${it.second}/${it.third}" }
            bookingDatesTextView.text = "Datas da Nova Reserva: $datesText"

            actionButton.text = "Inserir Reservas"
            deleteButton.visibility = View.GONE
        }

        actionButton.setOnClickListener {
            if (isEditMode) {
                updateGroupBookings()
            } else {
                insertNewGroupBookings()
            }
        }

        deleteButton.setOnClickListener {
            if (isEditMode && currentBookingGroupId != null) {
                deleteGroupBookings()
            } else {
                Toast.makeText(this, "Nenhuma reserva para excluir.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun insertNewGroupBookings() {
        val name = nameEditText.text.toString()
        val number = numberEditText.text.toString()
        val address = addressEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val value = valueEditText.text.toString().toDoubleOrNull()

        if (name.isBlank() || value == null) {
            Toast.makeText(this, "Por favor, preencha nome e valor.", Toast.LENGTH_SHORT).show()
            return
        }

        val newGroupId = UUID.randomUUID().toString() // Gerar um novo ID para este grupo de reservas

        var allBookingsProcessed = true
        newBookingDates.forEach { (dia, mes, ano) ->
            val newBooking = Booking(dia, mes, ano, name, number, address, description, value, newGroupId)
            val newRowId = databaseHelper.insertBooking(newBooking)
            if (newRowId == -1L) {
                allBookingsProcessed = false
                Toast.makeText(this, "Erro ao inserir reserva para $dia/$mes/$ano", Toast.LENGTH_SHORT).show()
            }
        }

        if (allBookingsProcessed) {
            Toast.makeText(this, "Reservas inseridas com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Ocorreu um erro ao inserir algumas reservas.", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateGroupBookings() {
        val name = nameEditText.text.toString()
        val number = numberEditText.text.toString()
        val address = addressEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val value = valueEditText.text.toString().toDoubleOrNull()

        if (name.isBlank() || value == null) {
            Toast.makeText(this, "Por favor, preencha nome e valor.", Toast.LENGTH_SHORT).show()
            return
        }

        currentBookingGroupId?.let { groupId ->
            val rowsAffected = databaseHelper.updateBookingsInGroup(groupId, name, number, address, description, value)
            if (rowsAffected > 0) {
                Toast.makeText(this, "$rowsAffected reservas atualizadas com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Nenhuma reserva foi atualizada. Verifique se o ID do grupo está correto.", Toast.LENGTH_LONG).show()
            }
        } ?: Toast.makeText(this, "Erro: ID do grupo de reserva não encontrado para atualização.", Toast.LENGTH_SHORT).show()
    }

    private fun deleteGroupBookings() {
        currentBookingGroupId?.let { groupId ->
            val rowsAffected = databaseHelper.deleteBookingsByGroupId(groupId)
            if (rowsAffected > 0) {
                Toast.makeText(this, "$rowsAffected reservas excluídas com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Nenhuma reserva foi excluída. Verifique se o ID do grupo está correto.", Toast.LENGTH_LONG).show()
            }
        } ?: Toast.makeText(this, "Erro: ID do grupo de reserva não encontrado para exclusão.", Toast.LENGTH_SHORT).show()
    }
}