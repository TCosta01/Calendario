// src/main/java/com/yourpackage/yourapp/BookingActivity.kt
package com.example.calendario // Substitua pelo seu package

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
    private var existingBooking: Booking? = null
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

        isEditMode = intent.getBooleanExtra("isEditMode", false)
        selectedDates = intent.getSerializableExtra("selectedDates") as ArrayList<Triple<Int, Int, Int>>

        updateBookingDatesText()

        if (isEditMode) {
            existingBooking = intent.getSerializableExtra("bookingData") as? Booking
            existingBooking?.let {
                nameEditText.setText(it.nome)
                numberEditText.setText(it.numero)
                addressEditText.setText(it.endereco)
                descriptionEditText.setText(it.descricao)
                valueEditText.setText(it.valor.toString())
            }
            actionButton.text = "Atualizar Reserva"
            deleteButton.visibility = Button.VISIBLE
        } else {
            actionButton.text = "Inserir Reserva"
            deleteButton.visibility = Button.GONE
        }

        actionButton.setOnClickListener {
            if (isEditMode) {
                updateBooking()
            } else {
                insertBooking()
            }
        }

        deleteButton.setOnClickListener {
            deleteBooking()
        }
    }

    private fun updateBookingDatesText() {
        if (selectedDates.isEmpty()) {
            bookingDatesTextView.text = "Datas da Reserva: Nenhuma"
        } else {
            val datesText = selectedDates.sortedWith(compareBy({ it.third }, { it.second }, { it.first }))
                .joinToString(", ") { "${it.first}/${it.second}/${it.third}" }
            bookingDatesTextView.text = "Datas da Reserva: $datesText"
        }
    }

    private fun insertBooking() {
        val name = nameEditText.text.toString().trim()
        val number = numberEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val valueString = valueEditText.text.toString().trim()

        if (name.isEmpty() || number.isEmpty() || address.isEmpty() || valueString.isEmpty() || selectedDates.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos e selecione uma data.", Toast.LENGTH_SHORT).show()
            return
        }

        val value = valueString.toDoubleOrNull()
        if (value == null) {
            Toast.makeText(this, "Valor inválido.", Toast.LENGTH_SHORT).show()
            return
        }

        // Para cada data selecionada, insere uma nova reserva
        var allInserted = true
        for (date in selectedDates) {
            val booking = Booking(date.first, date.second, date.third, name, number, address, description, value)
            val id = databaseHelper.insertBooking(booking)
            if (id == -1L) {
                allInserted = false
                Toast.makeText(this, "Erro ao inserir reserva para ${date.first}/${date.second}/${date.third}", Toast.LENGTH_LONG).show()
                break
            }
        }

        if (allInserted) {
            Toast.makeText(this, "Reserva(s) inserida(s) com sucesso!", Toast.LENGTH_SHORT).show()
            finish() // Retorna à MainActivity
        }
    }

    private fun updateBooking() {
        val name = nameEditText.text.toString().trim()
        val number = numberEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val valueString = valueEditText.text.toString().trim()

        if (name.isEmpty() || number.isEmpty() || address.isEmpty() || valueString.isEmpty() || selectedDates.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos e selecione uma data.", Toast.LENGTH_SHORT).show()
            return
        }

        val value = valueString.toDoubleOrNull()
        if (value == null) {
            Toast.makeText(this, "Valor inválido.", Toast.LENGTH_SHORT).show()
            return
        }

        existingBooking?.let { oldBooking ->
            // Se as datas foram alteradas, precisamos remover as antigas e inserir as novas.
            // Para simplificar, vou assumir que na atualização, a data selecionada no calendário
            // é a nova data para a reserva, e a reserva original se refere à data antiga.
            // Se o usuário puder selecionar múltiplas datas na tela de edição,
            // a lógica precisaria de um loop para atualizar/inserir cada uma.

            // Para o requisito, a atualização lida com a data antiga e a nova data selecionada no calendário.
            // Assumimos que 'selectedDates' agora contém a(s) nova(s) data(s) para a reserva.
            // Se a data antiga não estiver mais em selectedDates, ela deve ser removida.
            // Se as datas em selectedDates não existirem, elas devem ser adicionadas.

            // Lógica simplificada: exclui a reserva antiga e insere a nova com as novas datas
            val firstSelectedDate = selectedDates.first() // Assumimos que a nova reserva será associada a essa data
            val newBooking = Booking(
                firstSelectedDate.first,
                firstSelectedDate.second,
                firstSelectedDate.third,
                name, number, address, description, value
            )

            val rowsAffected = databaseHelper.updateBooking(oldBooking, newBooking) // Método customizado que exclui e insere
            if (rowsAffected > 0) {
                Toast.makeText(this, "Reserva atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                finish() // Retorna à MainActivity
            } else {
                Toast.makeText(this, "Erro ao atualizar reserva.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteBooking() {
        val firstSelectedDate = selectedDates.first() // Assumimos que estamos excluindo a reserva para essa data
        val rowsAffected = databaseHelper.deleteBooking(firstSelectedDate.first, firstSelectedDate.second, firstSelectedDate.third)
        if (rowsAffected > 0) {
            Toast.makeText(this, "Reserva excluída com sucesso!", Toast.LENGTH_SHORT).show()
            finish() // Retorna à MainActivity
        } else {
            Toast.makeText(this, "Erro ao excluir reserva.", Toast.LENGTH_SHORT).show()
        }
    }
}