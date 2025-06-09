// src/main/java/com/yourpackage/yourapp/DatabaseHelper.kt
package com.example.calendario // Substitua pelo seu package

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ReservaApp.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_RESERVAS = "Reservas"
        const val COLUMN_DIA = "dia"
        const val COLUMN_MES = "mes"
        const val COLUMN_ANO = "ano"
        const val COLUMN_NOME = "nome"
        const val COLUMN_NUMERO = "numero"
        const val COLUMN_ENDERECO = "endereco"
        const val COLUMN_DESCRICAO = "descricao"
        const val COLUMN_VALOR = "valor"
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
                + "PRIMARY KEY (" + COLUMN_DIA + "," + COLUMN_MES + "," + COLUMN_ANO + "))") // Chave primária composta
        db.execSQL(CREATE_RESERVAS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVAS")
        onCreate(db)
    }

    fun insertBooking(booking: Booking): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_DIA, booking.dia)
            put(COLUMN_MES, booking.mes)
            put(COLUMN_ANO, booking.ano)
            put(COLUMN_NOME, booking.nome)
            put(COLUMN_NUMERO, booking.numero)
            put(COLUMN_ENDERECO, booking.endereco)
            put(COLUMN_DESCRICAO, booking.descricao)
            put(COLUMN_VALOR, booking.valor)
        }
        val success = db.insert(TABLE_RESERVAS, null, contentValues)
        db.close()
        return success
    }

    fun updateBooking(oldBooking: Booking, newBooking: Booking): Int {
        val db = this.writableDatabase

        // Primeiro, excluímos as datas antigas
        val deleteSelection = "$COLUMN_DIA = ? AND $COLUMN_MES = ? AND $COLUMN_ANO = ?"
        val deleteArgs = arrayOf(oldBooking.dia.toString(), oldBooking.mes.toString(), oldBooking.ano.toString())
        db.delete(TABLE_RESERVAS, deleteSelection, deleteArgs)

        // Em seguida, inserimos a nova reserva com as novas datas
        val contentValues = ContentValues().apply {
            put(COLUMN_DIA, newBooking.dia)
            put(COLUMN_MES, newBooking.mes)
            put(COLUMN_ANO, newBooking.ano)
            put(COLUMN_NOME, newBooking.nome)
            put(COLUMN_NUMERO, newBooking.numero)
            put(COLUMN_ENDERECO, newBooking.endereco)
            put(COLUMN_DESCRICAO, newBooking.descricao)
            put(COLUMN_VALOR, newBooking.valor)
        }
        val success = db.insert(TABLE_RESERVAS, null, contentValues).toInt()
        db.close()
        return success
    }

    fun deleteBooking(day: Int, month: Int, year: Int): Int {
        val db = this.writableDatabase
        val selection = "$COLUMN_DIA = ? AND $COLUMN_MES = ? AND $COLUMN_ANO = ?"
        val selectionArgs = arrayOf(day.toString(), month.toString(), year.toString())
        val count = db.delete(TABLE_RESERVAS, selection, selectionArgs)
        db.close()
        return count
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

                    val booking = Booking(dia, mes, ano, nome, numero, endereco, descricao, valor)
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
                booking = Booking(dia, mes, ano, nome, numero, endereco, descricao, valor)
            }
            it.close()
        }
        db.close()
        return booking
    }
}