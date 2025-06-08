// src/main/java/com/example/reservascalendario/data/Reserva.kt
package com.example.reservascalendario.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

// Adicione @Parcelize para que objetos Reserva possam ser passados entre Activities
@Parcelize
data class Reserva(
    val dia: Int,
    val mes: Int,
    val ano: Int,
    val nome: String,
    val numero: String,
    val endereco: String
) : Parcelable {
    // Adiciona uma propriedade para converter a data em LocalDate para facilitar a comparação
    fun toLocalDate(): LocalDate {
        return LocalDate.of(ano, mes, dia)
    }
}
