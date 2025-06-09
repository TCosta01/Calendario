package com.example.calendario

// Modelo de dados
data class Reserva(
    val dia: Int,
    val mes: Int,
    val ano: Int,
    val nome: String,
    val numero: String,
    val endereco: String,
    val descricao: String,
    val valor: Double
)