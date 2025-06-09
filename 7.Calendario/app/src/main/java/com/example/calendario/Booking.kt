// src/main/java/com/yourpackage/yourapp/Booking.kt
package com.example.calendario // Substitua pelo seu package

import java.io.Serializable

data class Booking(
    val dia: Int,
    val mes: Int,
    val ano: Int,
    var nome: String,
    var numero: String,
    var endereco: String,
    var descricao: String,
    var valor: Double
) : Serializable // Necessário para passar objetos entre Activities