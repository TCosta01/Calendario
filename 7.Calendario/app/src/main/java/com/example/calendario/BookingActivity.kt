// src/main/java/com/example/calendario/BookingActivity.kt
package com.example.calendario

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.util.*
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
    private var existingBooking: Booking? = null // Pode ser nulo se for um novo booking
    private var bookingGroupId: String? = null // Novo: para identificar o grupo de reservas

    // NEW: Lista de datas selecionadas (para nova reserva) ou datas do grupo (para edição)
    private lateinit var currentDatesInGroup: ArrayList<Triple<Int, Int, Int>>

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

        isEditMode = intent.getBooleanExtra("isEditMode", false)

        if (isEditMode) {
            bookingGroupId = intent.getStringExtra("bookingGroupId")
            if (bookingGroupId != null) {
                // Se estiver no modo de edição, carregue todas as reservas associadas a esse grupo
                val bookingsInGroup = databaseHelper.getBookingsByGroup(bookingGroupId!!)
                if (bookingsInGroup.isNotEmpty()) {
                    existingBooking = bookingsInGroup.first() // Pegue o primeiro para preencher os campos
                    currentDatesInGroup = ArrayList(bookingsInGroup.map { Triple(it.dia, it.mes, it.ano) })

                    // Preencher campos com dados da primeira reserva do grupo
                    nameEditText.setText(existingBooking?.nome)
                    numberEditText.setText(existingBooking?.numero)
                    addressEditText.setText(existingBooking?.endereco)
                    descriptionEditText.setText(existingBooking?.descricao)
                    valueEditText.setText(existingBooking?.valor) // Valor como String

                    actionButton.text = "Atualizar Reserva"
                    deleteButton.visibility = Button.VISIBLE // Mostra o botão de excluir
                } else {
                    // Erro: bookingGroupId fornecido, mas nenhuma reserva encontrada
                    Toast.makeText(this, "Erro: Grupo de reserva não encontrado.", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }
            } else {
                // Erro: Modo de edição, mas sem bookingGroupId
                Toast.makeText(this, "Erro: ID do grupo de reserva ausente no modo de edição.", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        } else {
            // Modo de nova reserva
            currentDatesInGroup = intent.getSerializableExtra("selectedDatesForNewBooking") as? ArrayList<Triple<Int, Int, Int>> ?: ArrayList()
            actionButton.text = "Inserir Reserva"
            deleteButton.visibility = Button.GONE // Esconde o botão de excluir
        }

        // Exibe as datas selecionadas
        updateBookingDatesTextView()

        actionButton.setOnClickListener {
            handleActionButtonClick()
        }

        deleteButton.setOnClickListener {
            deleteBookingGroup() // Exclui todo o grupo
        }
    }

    private fun updateBookingDatesTextView() {
        if (currentDatesInGroup.isEmpty()) {
            bookingDatesTextView.text = "Datas da Reserva: Nenhuma"
        } else {
            val datesText = currentDatesInGroup.sortedWith(compareBy({ it.third }, { it.second }, { it.first }))
                .joinToString(", ") { "${it.first}/${it.second}/${it.third}" }
            bookingDatesTextView.text = "Datas da Reserva: $datesText"
        }
    }

    private fun handleActionButtonClick() {
        val name = nameEditText.text.toString().trim()
        val number = numberEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val valueString = valueEditText.text.toString().trim() // Valor como String

        if (name.isEmpty() || number.isEmpty() || address.isEmpty() || description.isEmpty() || valueString.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentDatesInGroup.isEmpty()) {
            Toast.makeText(this, "Nenhuma data selecionada para reserva.", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode && existingBooking != null && bookingGroupId != null) {
            // Lógica de atualização
            updateBookingGroup(name, number, address, description, valueString)
        } else {
            // Lógica de inserção
            insertNewBookingGroup(name, number, address, description, valueString)
        }
    }

    private fun insertNewBookingGroup(name: String, number: String, address: String, description: String, value: String) {
        val bookingsToInsert = currentDatesInGroup.map { (day, month, year) ->
            // Ao criar um novo grupo, o bookingGroupId é gerado dentro do insertBookingGroup
            Booking(day, month, year, name, number, address, description, value, "") // BookingGroupId será preenchido no DB
        }

        val success = databaseHelper.insertBookingGroup(bookingsToInsert)
        if (success) {
            Toast.makeText(this, "Reservas inseridas com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Erro ao inserir reservas.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBookingGroup(name: String, number: String, address: String, description: String, value: String) {
        val currentGroupId = bookingGroupId ?: return // Não deve ser nulo em modo de edição

        // Obtém as reservas atuais no grupo
        val existingBookingsInGroup = databaseHelper.getBookingsByGroup(currentGroupId)
        val existingDates = existingBookingsInGroup.map { Triple(it.dia, it.mes, it.ano) }.toSet()
        val newDates = currentDatesInGroup.toSet()

        // Datas a serem adicionadas (novas datas selecionadas que não existiam no grupo)
        val datesToAdd = newDates.filter { it !in existingDates }
        // Datas a serem removidas (datas existentes no grupo que não estão mais selecionadas)
        val datesToRemove = existingDates.filter { it !in newDates }

        // 1. Excluir as reservas que não estão mais selecionadas
        if (datesToRemove.isNotEmpty()) {
            databaseHelper.deleteSpecificDatesFromBookingGroup(currentGroupId, datesToRemove)
        }

        // 2. Inserir as novas datas selecionadas
        if (datesToAdd.isNotEmpty()) {
            val bookingsToInsert = datesToAdd.map { (day, month, year) ->
                Booking(day, month, year, name, number, address, description, value, currentGroupId)
            }
            databaseHelper.insertBookingGroup(bookingsToInsert) // O insertBookingGroup irá reusar o bookingGroupId
        }

        // 3. Atualizar os detalhes (nome, número, etc.) para as reservas restantes no grupo
        // Percorrer todas as reservas que deveriam existir (as novas datas) e atualizar
        // Se o número de datas não mudou, ou se datas foram adicionadas e removidas,
        // é mais fácil fazer um update geral dos campos comuns (nome, numero, etc.)
        // para todas as reservas que ainda pertencem ao grupo.
        val contentValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NOME, name)
            put(DatabaseHelper.COLUMN_NUMERO, number)
            put(DatabaseHelper.COLUMN_ENDERECO, address)
            put(DatabaseHelper.COLUMN_DESCRICAO, description)
            put(DatabaseHelper.COLUMN_VALOR, value)
        }

        val rowsAffected = databaseHelper.updateBookingGroupDetails(currentGroupId, contentValues)

        if (rowsAffected > 0 || datesToAdd.isNotEmpty() || datesToRemove.isNotEmpty()) {
            Toast.makeText(this, "Reservas atualizadas com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Nenhuma alteração detectada ou erro ao atualizar.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteBookingGroup() {
        val currentGroupId = bookingGroupId
        if (currentGroupId != null) {
            val rowsAffected = databaseHelper.deleteBookingGroup(currentGroupId)
            if (rowsAffected > 0) {
                Toast.makeText(this, "Reservas do grupo excluídas com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Erro ao excluir reservas do grupo.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Erro: Não foi possível identificar o grupo para exclusão.", Toast.LENGTH_SHORT).show()
        }
    }
}