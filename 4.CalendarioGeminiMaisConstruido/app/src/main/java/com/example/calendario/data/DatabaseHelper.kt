// src/main/java/com/example/reservascalendario/data/DatabaseHelper.kt
package com.example.calendario.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "reservas_db"
        private const val DATABASE_VERSION = 1 // Mantenha a versão se a lógica de onCreate/onUpgrade for limpa, ou aumente para forçar onUpgrade
        private const val TABLE_RESERVAS = "Reservas"
        private const val COLUMN_DIA = "dia"
        private const val COLUMN_MES = "mes"
        private const val COLUMN_ANO = "ano"
        private const val COLUMN_NOME = "nome"
        private const val COLUMN_NUMERO = "numero"
        private const val COLUMN_ENDERECO = "endereco"
        private const val COLUMN_DESCRICAO = "descricao" // Nova coluna
        private const val COLUMN_VALOR = "valor"         // Nova coluna
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_RESERVAS_TABLE = ("CREATE TABLE $TABLE_RESERVAS ("
                + "$COLUMN_DIA INTEGER,"
                + "$COLUMN_MES INTEGER,"
                + "$COLUMN_ANO INTEGER,"
                + "$COLUMN_NOME TEXT,"
                + "$COLUMN_NUMERO TEXT,"
                + "$COLUMN_ENDERECO TEXT,"
                + "$COLUMN_DESCRICAO TEXT," // Nova coluna
                + "$COLUMN_VALOR REAL,"     // Nova coluna (REAL para Double)
                + "PRIMARY KEY ($COLUMN_DIA, $COLUMN_MES, $COLUMN_ANO))") // Chave primária composta para garantir unicidade da data
        db.execSQL(CREATE_RESERVAS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Se você tiver usuários com versões antigas do app, precisará de uma lógica de migração
        // mais robusta aqui (ex: ALTER TABLE ADD COLUMN).
        // Para este exemplo, estamos simplesmente recriando a tabela, o que apaga os dados existentes.
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVAS")
        onCreate(db)
    }

    // --- Operações CRUD ---

    /**
     * Cria ContentValues a partir de um objeto Reserva.
     * Reutilizado para inserção e atualização.
     */
    private fun getContentValues(reserva: Reserva): ContentValues {
        return ContentValues().apply {
            put(COLUMN_DIA, reserva.dia)
            put(COLUMN_MES, reserva.mes)
            put(COLUMN_ANO, reserva.ano)
            put(COLUMN_NOME, reserva.nome)
            put(COLUMN_NUMERO, reserva.numero)
            put(COLUMN_ENDERECO, reserva.endereco)
            put(COLUMN_DESCRICAO, reserva.descricao)
            put(COLUMN_VALOR, reserva.valor)
        }
    }

    /**
     * Insere uma nova reserva no banco de dados.
     * Retorna o ID da linha recém-inserida ou -1 se ocorrer um erro.
     */
    fun insertReserva(reserva: Reserva): Long {
        val db = this.writableDatabase
        val values = getContentValues(reserva)
        val id = db.insert(TABLE_RESERVAS, null, values)
        db.close()
        return id
    }

    /**
     * Obtém uma reserva específica por data.
     * Retorna um objeto Reserva se encontrado, ou null.
     */
    fun getReservaByDate(dia: Int, mes: Int, ano: Int): Reserva? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_RESERVAS,
            arrayOf(
                COLUMN_DIA, COLUMN_MES, COLUMN_ANO, COLUMN_NOME,
                COLUMN_NUMERO, COLUMN_ENDERECO, COLUMN_DESCRICAO, COLUMN_VALOR
            ),
            "$COLUMN_DIA = ? AND $COLUMN_MES = ? AND $COLUMN_ANO = ?",
            arrayOf(dia.toString(), mes.toString(), ano.toString()),
            null, null, null, null
        )

        var reserva: Reserva? = null
        cursor?.use {
            if (it.moveToFirst()) {
                val foundDia = it.getInt(it.getColumnIndexOrThrow(COLUMN_DIA))
                val foundMes = it.getInt(it.getColumnIndexOrThrow(COLUMN_MES))
                val foundAno = it.getInt(it.getColumnIndexOrThrow(COLUMN_ANO))
                val nome = it.getString(it.getColumnIndexOrThrow(COLUMN_NOME))
                val numero = it.getString(it.getColumnIndexOrThrow(COLUMN_NUMERO))
                val endereco = it.getString(it.getColumnIndexOrThrow(COLUMN_ENDERECO))
                val descricao = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRICAO))
                val valor = it.getDouble(it.getColumnIndexOrThrow(COLUMN_VALOR))
                reserva = Reserva(foundDia, foundMes, foundAno, nome, numero, endereco, descricao, valor)
            }
        }
        db.close()
        return reserva
    }

    /**
     * Obtém todas as datas reservadas no banco de dados.
     * Retorna um conjunto de LocalDate para facilitar a verificação.
     */
    fun getAllReservedDates(): Set<LocalDate> {
        val reservedDates = mutableSetOf<LocalDate>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_DIA, $COLUMN_MES, $COLUMN_ANO FROM $TABLE_RESERVAS", null)

        cursor?.use {
            while (it.moveToNext()) {
                val dia = it.getInt(it.getColumnIndexOrThrow(COLUMN_DIA))
                val mes = it.getInt(it.getColumnIndexOrThrow(COLUMN_MES))
                val ano = it.getInt(it.getColumnIndexOrThrow(COLUMN_ANO))
                reservedDates.add(LocalDate.of(ano, mes, dia))
            }
        }
        db.close()
        return reservedDates
    }

    /**
     * Atualiza uma reserva existente no banco de dados com base na data.
     * Retorna o número de linhas afetadas.
     */
    fun updateReserva(reserva: Reserva): Int {
        val db = this.writableDatabase
        val values = getContentValues(reserva)
        val rowsAffected = db.update(
            TABLE_RESERVAS,
            values,
            "$COLUMN_DIA = ? AND $COLUMN_MES = ? AND $COLUMN_ANO = ?",
            arrayOf(reserva.dia.toString(), reserva.mes.toString(), reserva.ano.toString())
        )
        db.close()
        return rowsAffected
    }

    /**
     * Exclui uma reserva específica por data.
     * Retorna o número de linhas afetadas.
     */
    fun deleteReserva(dia: Int, mes: Int, ano: Int): Int {
        val db = this.writableDatabase
        val rowsAffected = db.delete(
            TABLE_RESERVAS,
            "$COLUMN_DIA = ? AND $COLUMN_MES = ? AND $COLUMN_ANO = ?",
            arrayOf(dia.toString(), mes.toString(), ano.toString())
        )
        db.close()
        return rowsAffected
    }

    /**
     * Exclui múltiplas reservas com base em uma lista de datas.
     * Retorna o número total de linhas afetadas.
     */
    fun deleteReservasByDates(dates: List<LocalDate>): Int {
        val db = this.writableDatabase
        var totalRowsAffected = 0
        db.beginTransaction()
        try {
            for (date in dates) {
                val rowsAffected = db.delete(
                    TABLE_RESERVAS,
                    "$COLUMN_DIA = ? AND $COLUMN_MES = ? AND $COLUMN_ANO = ?",
                    arrayOf(date.dayOfMonth.toString(), date.monthValue.toString(), date.year.toString())
                )
                totalRowsAffected += rowsAffected
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
        return totalRowsAffected
    }

    /**
     * Move uma reserva de uma data antiga para uma nova data,
     * atualizando também seus detalhes.
     * Retorna true se a operação for bem-sucedida, false caso contrário.
     * Lança exceção se a nova data já estiver reservada.
     */
    fun moveReserva(oldDate: LocalDate, newDate: LocalDate, updatedReserva: Reserva): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            // 1. Verificar se a nova data já está ocupada
            if (getReservaByDate(newDate.dayOfMonth, newDate.monthValue, newDate.year) != null) {
                throw IllegalStateException("A nova data ${newDate.dayOfMonth}/${newDate.monthValue}/${newDate.year} já está reservada.")
            }

            // 2. Excluir a reserva antiga
            val deletedRows = db.delete(
                TABLE_RESERVAS,
                "$COLUMN_DIA = ? AND $COLUMN_MES = ? AND $COLUMN_ANO = ?",
                arrayOf(oldDate.dayOfMonth.toString(), oldDate.monthValue.toString(), oldDate.year.toString())
            )

            if (deletedRows > 0) {
                // 3. Inserir a nova reserva com a nova data e os detalhes atualizados
                val newReserva = updatedReserva.copy(
                    dia = newDate.dayOfMonth,
                    mes = newDate.monthValue,
                    ano = newDate.year
                )
                val newId = db.insert(TABLE_RESERVAS, null, getContentValues(newReserva))

                if (newId != -1L) {
                    db.setTransactionSuccessful()
                    return true
                }
            }
            return false // Se não conseguiu deletar ou inserir
        } catch (e: Exception) {
            // Lidar com exceções (ex: nova data já reservada)
            throw e
        } finally {
            db.endTransaction()
            db.close()
        }
    }
}
