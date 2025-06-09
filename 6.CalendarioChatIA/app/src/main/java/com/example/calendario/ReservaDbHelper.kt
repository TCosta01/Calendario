package com.example.calendario

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ReservaDbHelper(context: Context) : SQLiteOpenHelper(context, "reserva_database", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE Reservas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dia INTEGER,
                mes INTEGER,
                ano INTEGER,
                nome TEXT,
                numero TEXT,
                endereco TEXT,
                descricao TEXT,
                valor REAL
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Reservas")
        onCreate(db)
    }

    fun getReservaPorData(dia: Int, mes: Int, ano: Int): List<Reserva> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Reservas WHERE dia=? AND mes=? AND ano=?", arrayOf(dia.toString(), mes.toString(), ano.toString()))
        val reservas = mutableListOf<Reserva>()
        while (cursor.moveToNext()) {
            reservas.add(cursorToReserva(cursor))
        }
        cursor.close()
        return reservas
    }

    fun getTodasReservas(): List<Reserva> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Reservas", null)
        val reservas = mutableListOf<Reserva>()
        while (cursor.moveToNext()) {
            reservas.add(cursorToReserva(cursor))
        }
        cursor.close()
        return reservas
    }

    fun inserirReserva(reserva: Reserva) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("dia", reserva.dia)
            put("mes", reserva.mes)
            put("ano", reserva.ano)
            put("nome", reserva.nome)
            put("numero", reserva.numero)
            put("endereco", reserva.endereco)
            put("descricao", reserva.descricao)
            put("valor", reserva.valor)
        }
        db.insert("Reservas", null, values)
    }

    fun atualizarReserva(reserva: Reserva) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("dia", reserva.dia)
            put("mes", reserva.mes)
            put("ano", reserva.ano)
            put("nome", reserva.nome)
            put("numero", reserva.numero)
            put("endereco", reserva.endereco)
            put("descricao", reserva.descricao)
            put("valor", reserva.valor)
        }
        db.update("Reservas", values, "id = ?", arrayOf(reserva.id.toString()))
    }

    fun excluirReserva(reserva: Reserva) {
        val db = writableDatabase
        db.delete("Reservas", "id = ?", arrayOf(reserva.id.toString()))
    }

    private fun cursorToReserva(cursor: Cursor): Reserva {
        return Reserva(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            dia = cursor.getInt(cursor.getColumnIndexOrThrow("dia")),
            mes = cursor.getInt(cursor.getColumnIndexOrThrow("mes")),
            ano = cursor.getInt(cursor.getColumnIndexOrThrow("ano")),
            nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
            numero = cursor.getString(cursor.getColumnIndexOrThrow("numero")),
            endereco = cursor.getString(cursor.getColumnIndexOrThrow("endereco")),
            descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao")),
            valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor"))
        )
    }
}

