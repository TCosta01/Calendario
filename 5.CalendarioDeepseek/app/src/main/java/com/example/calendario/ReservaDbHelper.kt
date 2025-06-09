package com.example.calendario

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ReservaDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Reservas.db"

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${ReservaContract.ReservaEntry.TABLE_NAME} (" +
                    "${ReservaContract.ReservaEntry.COLUMN_DIA} INTEGER," +
                    "${ReservaContract.ReservaEntry.COLUMN_MES} INTEGER," +
                    "${ReservaContract.ReservaEntry.COLUMN_ANO} INTEGER," +
                    "${ReservaContract.ReservaEntry.COLUMN_NOME} TEXT," +
                    "${ReservaContract.ReservaEntry.COLUMN_NUMERO} TEXT," +
                    "${ReservaContract.ReservaEntry.COLUMN_ENDERECO} TEXT," +
                    "${ReservaContract.ReservaEntry.COLUMN_DESCRICAO} TEXT," +
                    "${ReservaContract.ReservaEntry.COLUMN_VALOR} REAL," +
                    "PRIMARY KEY (${ReservaContract.ReservaEntry.COLUMN_DIA}, " +
                    "${ReservaContract.ReservaEntry.COLUMN_MES}, " +
                    "${ReservaContract.ReservaEntry.COLUMN_ANO}))"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${ReservaContract.ReservaEntry.TABLE_NAME}")
        onCreate(db)
    }

    fun inserirReserva(reserva: Reserva): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(ReservaContract.ReservaEntry.COLUMN_DIA, reserva.dia)
            put(ReservaContract.ReservaEntry.COLUMN_MES, reserva.mes)
            put(ReservaContract.ReservaEntry.COLUMN_ANO, reserva.ano)
            put(ReservaContract.ReservaEntry.COLUMN_NOME, reserva.nome)
            put(ReservaContract.ReservaEntry.COLUMN_NUMERO, reserva.numero)
            put(ReservaContract.ReservaEntry.COLUMN_ENDERECO, reserva.endereco)
            put(ReservaContract.ReservaEntry.COLUMN_DESCRICAO, reserva.descricao)
            put(ReservaContract.ReservaEntry.COLUMN_VALOR, reserva.valor)
        }

        return db.insert(ReservaContract.ReservaEntry.TABLE_NAME, null, values) != -1L
    }

    fun atualizarReserva(dataAntiga: Triple<Int, Int, Int>, reserva: Reserva): Boolean {
        val db = writableDatabase

        // Primeiro deleta a reserva antiga
        deletarReserva(dataAntiga.first, dataAntiga.second, dataAntiga.third)

        // Depois insere a nova reserva
        return inserirReserva(reserva)
    }

    fun deletarReserva(dia: Int, mes: Int, ano: Int): Boolean {
        val db = writableDatabase
        val selection = "${ReservaContract.ReservaEntry.COLUMN_DIA} = ? AND " +
                "${ReservaContract.ReservaEntry.COLUMN_MES} = ? AND " +
                "${ReservaContract.ReservaEntry.COLUMN_ANO} = ?"
        val selectionArgs = arrayOf(dia.toString(), mes.toString(), ano.toString())

        return db.delete(ReservaContract.ReservaEntry.TABLE_NAME, selection, selectionArgs) > 0
    }

    fun buscarReserva(dia: Int, mes: Int, ano: Int): Reserva? {
        val db = readableDatabase
        val projection = arrayOf(
            ReservaContract.ReservaEntry.COLUMN_NOME,
            ReservaContract.ReservaEntry.COLUMN_NUMERO,
            ReservaContract.ReservaEntry.COLUMN_ENDERECO,
            ReservaContract.ReservaEntry.COLUMN_DESCRICAO,
            ReservaContract.ReservaEntry.COLUMN_VALOR
        )

        val selection = "${ReservaContract.ReservaEntry.COLUMN_DIA} = ? AND " +
                "${ReservaContract.ReservaEntry.COLUMN_MES} = ? AND " +
                "${ReservaContract.ReservaEntry.COLUMN_ANO} = ?"
        val selectionArgs = arrayOf(dia.toString(), mes.toString(), ano.toString())

        val cursor = db.query(
            ReservaContract.ReservaEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            Reserva(
                dia,
                mes,
                ano,
                cursor.getString(cursor.getColumnIndexOrThrow(ReservaContract.ReservaEntry.COLUMN_NOME)),
                cursor.getString(cursor.getColumnIndexOrThrow(ReservaContract.ReservaEntry.COLUMN_NUMERO)),
                cursor.getString(cursor.getColumnIndexOrThrow(ReservaContract.ReservaEntry.COLUMN_ENDERECO)),
                cursor.getString(cursor.getColumnIndexOrThrow(ReservaContract.ReservaEntry.COLUMN_DESCRICAO)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(ReservaContract.ReservaEntry.COLUMN_VALOR))
            )
        } else {
            null
        }
    }

    fun buscarDatasReservadas(): List<Triple<Int, Int, Int>> {
        val db = readableDatabase
        val projection = arrayOf(
            ReservaContract.ReservaEntry.COLUMN_DIA,
            ReservaContract.ReservaEntry.COLUMN_MES,
            ReservaContract.ReservaEntry.COLUMN_ANO
        )

        val cursor = db.query(
            ReservaContract.ReservaEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        val datas = mutableListOf<Triple<Int, Int, Int>>()
        while (cursor.moveToNext()) {
            val dia = cursor.getInt(cursor.getColumnIndexOrThrow(ReservaContract.ReservaEntry.COLUMN_DIA))
            val mes = cursor.getInt(cursor.getColumnIndexOrThrow(ReservaContract.ReservaEntry.COLUMN_MES))
            val ano = cursor.getInt(cursor.getColumnIndexOrThrow(ReservaContract.ReservaEntry.COLUMN_ANO))
            datas.add(Triple(dia, mes, ano))
        }
        cursor.close()

        return datas
    }
}


