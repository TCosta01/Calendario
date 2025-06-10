// src/main/java/com/example/calendario/Booking.kt
package com.example.calendario

import java.io.Serializable

data class Booking(
    val dia: Int,
    val mes: Int,
    val ano: Int,
    var nome: String,
    var numero: String,
    var endereco: String,
    var descricao: String,
    var valor: String, // MUDANÇA AQUI: Agora é String
    var bookingGroupId: String // NOVO: Para agrupar reservas
) : Serializable // Necessário para passar objetos entre Activities