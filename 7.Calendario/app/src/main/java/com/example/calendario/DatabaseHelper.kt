// src/main/java/com/example/calendario/DatabaseHelper.kt
package com.example.calendario

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ReservaApp.db"
        private const val DATABASE_VERSION = 2 // Incrementar a versão do banco de dados
        const val TABLE_RESERVAS = "Reservas"
        const val COLUMN_DIA = "dia"
        const val COLUMN_MES = "mes"
        const val COLUMN_ANO = "ano"
        const val COLUMN_NOME = "nome"
        const val COLUMN_NUMERO = "numero"
        const val COLUMN_ENDERECO = "endereco"
        const val COLUMN_DESCRICAO = "descricao"
        const val COLUMN_VALOR = "valor"
        const val COLUMN_BOOKING_GROUP_ID = "booking_group_id" // Nova coluna
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_RESERVAS_TABLE = ("CREATE TABLE " + TABLE_RESERVAS + "("
                + COLUMN_DIA + " INTEGER,"
                + COLUMN_MES + " INTEGER,"
                + COLUMN_ANO + " INTEGER,"
                + COLUMN_NOME + " TEXT,"
                + COLUMN_NUMERO + " TEXT,"
                + COLUMN_ENDERECO + " TEXT,"
                + COLUMN_DESCRICAO + " TEXT,"
                + COLUMN_VALOR + " REAL,"
                + COLUMN_BOOKING_GROUP_ID + " TEXT," // Adicionando a nova coluna
                + "PRIMARY KEY (" + COLUMN_DIA + ", " + COLUMN_MES + ", " + COLUMN_ANO + "))")
        db.execSQL(CREATE_RESERVAS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop all tables and recreate them to handle schema changes
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVAS")
        onCreate(db)
    }

    fun insertBooking(booking: Booking): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DIA, booking.dia)
            put(COLUMN_MES, booking.mes)
            put(COLUMN_ANO, booking.ano)
            put(COLUMN_NOME, booking.nome)
            put(COLUMN_NUMERO, booking.numero)
            put(COLUMN_ENDERECO, booking.endereco)
            put(COLUMN_DESCRICAO, booking.descricao)
            put(COLUMN_VALOR, booking.valor)
            put(COLUMN_BOOKING_GROUP_ID, booking.bookingGroupId) // Salva o Group ID
        }
        val id = db.insert(TABLE_RESERVAS, null, values)
        db.close()
        return id
    }

    fun getAllBookings(): List<Booking> {
        val bookingList = mutableListOf<Booking>()
        val selectQuery = "SELECT * FROM $TABLE_RESERVAS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        cursor?.let {
            if (it.moveToFirst()) {
                do {
                    val dia = it.getInt(it.getColumnIndexOrThrow(COLUMN_DIA))
                    val mes = it.getInt(it.getColumnIndexOrThrow(COLUMN_MES))
                    val ano = it.getInt(it.getColumnIndexOrThrow(COLUMN_ANO))
                    val nome = it.getString(it.getColumnIndexOrThrow(COLUMN_NOME))
                    val numero = it.getString(it.getColumnIndexOrThrow(COLUMN_NUMERO))
                    val endereco = it.getString(it.getColumnIndexOrThrow(COLUMN_ENDERECO))
                    val descricao = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRICAO))
                    val valor = it.getDouble(it.getColumnIndexOrThrow(COLUMN_VALOR))
                    val bookingGroupId = it.getString(it.getColumnIndexOrThrow(COLUMN_BOOKING_GROUP_ID))
                    val booking = Booking(dia, mes, ano, nome, numero, endereco, descricao, valor, bookingGroupId)
                    bookingList.add(booking)
                } while (it.moveToNext())
            }
            it.close()
        }
        db.close()
        return bookingList
    }

    fun getBooking(day: Int, month: Int, year: Int): Booking? {
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_RESERVAS WHERE $COLUMN_DIA = ? AND $COLUMN_MES = ? AND $COLUMN_ANO = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(day.toString(), month.toString(), year.toString()))

        var booking: Booking? = null
        cursor?.let {
            if (it.moveToFirst()) {
                val dia = it.getInt(it.getColumnIndexOrThrow(COLUMN_DIA))
                val mes = it.getInt(it.getColumnIndexOrThrow(COLUMN_MES))
                val ano = it.getInt(it.getColumnIndexOrThrow(COLUMN_ANO))
                val nome = it.getString(it.getColumnIndexOrThrow(COLUMN_NOME))
                val numero = it.getString(it.getColumnIndexOrThrow(COLUMN_NUMERO))
                val endereco = it.getString(it.getColumnIndexOrThrow(COLUMN_ENDERECO))
                val descricao = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRICAO))
                val valor = it.getDouble(it.getColumnIndexOrThrow(COLUMN_VALOR))
                val bookingGroupId = it.getString(it.getColumnIndexOrThrow(COLUMN_BOOKING_GROUP_ID))
                booking = Booking(dia, mes, ano, nome, numero, endereco, descricao, valor, bookingGroupId)
            }
            it.close()
        }
        db.close()
        return booking
    }

    // Novo método para obter todas as reservas de um grupo
    fun getBookingsByGroupId(groupId: String): List<Booking> {
        val bookingList = mutableListOf<Booking>()
        val selectQuery = "SELECT * FROM $TABLE_RESERVAS WHERE $COLUMN_BOOKING_GROUP_ID = ?"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(groupId))

        cursor?.let {
            if (it.moveToFirst()) {
                do {
                    val dia = it.getInt(it.getColumnIndexOrThrow(COLUMN_DIA))
                    val mes = it.getInt(it.getColumnIndexOrThrow(COLUMN_MES))
                    val ano = it.getInt(it.getColumnIndexOrThrow(COLUMN_ANO))
                    val nome = it.getString(it.getColumnIndexOrThrow(COLUMN_NOME))
                    val numero = it.getString(it.getColumnIndexOrThrow(COLUMN_NUMERO))
                    val endereco = it.getString(it.getColumnIndexOrThrow(COLUMN_ENDERECO))
                    val descricao = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRICAO))
                    val valor = it.getDouble(it.getColumnIndexOrThrow(COLUMN_VALOR))
                    val bookingGroupId = it.getString(it.getColumnIndexOrThrow(COLUMN_BOOKING_GROUP_ID))
                    val booking = Booking(dia, mes, ano, nome, numero, endereco, descricao, valor, bookingGroupId)
                    bookingList.add(booking)
                } while (it.moveToNext())
            }
            it.close()
        }
        db.close()
        return bookingList
    }

    // Método para atualizar todas as reservas de um grupo
    fun updateBookingsInGroup(groupId: String, newName: String, newNumber: String, newAddress: String,
                              newDescription: String, newValue: Double): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOME, newName)
            put(COLUMN_NUMERO, newNumber)
            put(COLUMN_ENDERECO, newAddress)
            put(COLUMN_DESCRICAO, newDescription)
            put(COLUMN_VALOR, newValue)
        }
        val rowsAffected = db.update(TABLE_RESERVAS, values, "$COLUMN_BOOKING_GROUP_ID = ?", arrayOf(groupId))
        db.close()
        return rowsAffected
    }

    fun deleteBooking(day: Int, month: Int, year: Int): Int {
        val db = this.writableDatabase
        val rowsAffected = db.delete(
            TABLE_RESERVAS,
            "$COLUMN_DIA = ? AND $COLUMN_MES = ? AND $COLUMN_ANO = ?",
            arrayOf(day.toString(), month.toString(), year.toString())
        )
        db.close()
        return rowsAffected
    }

    // Método para excluir todas as reservas de um grupo
    fun deleteBookingsByGroupId(groupId: String): Int {
        val db = this.writableDatabase
        val rowsAffected = db.delete(TABLE_RESERVAS, "$COLUMN_BOOKING_GROUP_ID = ?", arrayOf(groupId))
        db.close()
        return rowsAffected
    }
}