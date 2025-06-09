package com.example.calendario

data class Reserva(
    val id: Int = 0,
    val dia: Int,
    val mes: Int,
    val ano: Int,
    val nome: String,
    val numero: String,
    val endereco: String,
    val descricao: String,
    val valor: Double
)
