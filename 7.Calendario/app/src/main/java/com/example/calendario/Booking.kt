// src/main/java/com/example/calendario/Booking.kt
package com.example.calendario

import java.io.Serializable
import java.util.UUID // Importar para gerar IDs únicos

data class Booking(
    val dia: Int,
    val mes: Int,
    val ano: Int,
    var nome: String,
    var numero: String,
    var endereco: String,
    var descricao: String,
    var valor: Double,
    val bookingGroupId: String = UUID.randomUUID().toString() // Gerar um ID único por padrão
) : Serializable