// src/main/java/com/example/calendario/DatabaseHelper.kt
package com.example.calendario

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.UUID // Adicionar import para UUID

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ReservaApp.db"
        // MUDANÇA AQUI: Incrementamos a versão do banco de dados para disparar onUpgrade
        private const val DATABASE_VERSION = 2 // Era 1, agora é 2
        const val TABLE_RESERVAS = "Reservas"
        const val COLUMN_ID = "_id" // Adicionando uma coluna ID para facilitar updates/deletes
        const val COLUMN_DIA = "dia"
        const val COLUMN_MES = "mes"
        const val COLUMN_ANO = "ano"
        const val COLUMN_NOME = "nome"
        const val COLUMN_NUMERO = "numero"
        const val COLUMN_ENDERECO = "endereco"
        const val COLUMN_DESCRICAO = "descricao"
        const val COLUMN_VALOR = "valor" // Continua sendo a coluna
        const val COLUMN_BOOKING_GROUP_ID = "booking_group_id" // Adicione esta coluna para agrupar reservas
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_RESERVAS_TABLE = ("CREATE TABLE " + TABLE_RESERVAS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," // Adicionando COLUMN_ID
                + COLUMN_DIA + " INTEGER,"
                + COLUMN_MES + " INTEGER,"
                + COLUMN_ANO + " INTEGER,"
                + COLUMN_NOME + " TEXT,"
                + COLUMN_NUMERO + " TEXT,"
                + COLUMN_ENDERECO + " TEXT,"
                + COLUMN_DESCRICAO + " TEXT,"
                + COLUMN_VALOR + " TEXT," // MUDANÇA AQUI: Agora é TEXT para String
                + COLUMN_BOOKING_GROUP_ID + " TEXT" // Adicionando COLUMN_BOOKING_GROUP_ID
                + ")")
        db.execSQL(CREATE_RESERVAS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Exemplo de como lidar com upgrade do banco de dados
        if (oldVersion < 2) {
            // Se a versão antiga for 1 e a nova for 2, adicione a nova coluna
            // Primeiro, verifique se a coluna já existe antes de tentar adicioná-la
            // Isso evita erros se onUpgrade for chamado mais de uma vez ou em cenários específicos
            if (!columnExists(db, TABLE_RESERVAS, COLUMN_BOOKING_GROUP_ID)) {
                db.execSQL("ALTER TABLE $TABLE_RESERVAS ADD COLUMN $COLUMN_BOOKING_GROUP_ID TEXT DEFAULT '';")
            }
            if (!columnExists(db, TABLE_RESERVAS, COLUMN_ID)) {
                // Para adicionar PRIMARY KEY AUTOINCREMENT a uma tabela existente,
                // geralmente você precisará criar uma nova tabela, copiar os dados
                // e depois renomear. Para simplicidade, vamos adicionar como uma coluna normal.
                // Se você realmente precisa de um ID auto incrementável e primário para as reservas existentes,
                // a abordagem seria mais complexa (renomear tabela antiga, criar nova, copiar dados).
                // Para este caso, apenas adicionar a coluna é mais direto.
                db.execSQL("ALTER TABLE $TABLE_RESERVAS ADD COLUMN $COLUMN_ID INTEGER DEFAULT 0;")
            }
            // Se a coluna valor era REAL e agora precisa ser TEXT, isso é mais complexo
            // pois exigiriria migração de dados. Por enquanto, a alteração é feita na criação.
            // Para um caso de uso real, você precisaria de um script de migração.
        }
        // Se houver mais upgrades no futuro, adicione outros `if` statements aqui
    }

    // Helper para verificar se a coluna existe
    private fun columnExists(db: SQLiteDatabase, tableName: String, columnName: String): Boolean {
        var cursor = db.rawQuery("PRAGMA table_info($tableName)", null)
        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndexOrThrow("name"))
                if (columnName.equals(name, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
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
            put(COLUMN_VALOR, booking.valor) // Valor como String
            put(COLUMN_BOOKING_GROUP_ID, booking.bookingGroupId) // Adiciona o bookingGroupId
        }
        val success = db.insert(TABLE_RESERVAS, null, contentValues)
        db.close()
        return success
    }

    // Método para inserir várias reservas de um grupo
    fun insertBookingGroup(bookings: List<Booking>): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            val bookingGroupId = UUID.randomUUID().toString() // Gera um UUID para o grupo
            for (booking in bookings) {
                val contentValues = ContentValues().apply {
                    put(COLUMN_DIA, booking.dia)
                    put(COLUMN_MES, booking.mes)
                    put(COLUMN_ANO, booking.ano)
                    put(COLUMN_NOME, booking.nome)
                    put(COLUMN_NUMERO, booking.numero)
                    put(COLUMN_ENDERECO, booking.endereco)
                    put(COLUMN_DESCRICAO, booking.descricao)
                    put(COLUMN_VALOR, booking.valor)
                    put(COLUMN_BOOKING_GROUP_ID, bookingGroupId) // Associa ao mesmo grupo
                }
                val success = db.insert(TABLE_RESERVAS, null, contentValues)
                if (success == -1L) {
                    db.endTransaction()
                    return false // Falha na inserção
                }
            }
            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error inserting booking group: ${e.message}")
            return false
        } finally {
            db.endTransaction()
        }
    }

    // Altere a assinatura para aceitar um `Booking` completo para o `oldBooking`
    // isso permitirá usar o `bookingGroupId` para localizar e atualizar
    fun updateBooking(oldBooking: Booking, newBooking: Booking): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_DIA, newBooking.dia)
            put(COLUMN_MES, newBooking.mes)
            put(COLUMN_ANO, newBooking.ano)
            put(COLUMN_NOME, newBooking.nome)
            put(COLUMN_NUMERO, newBooking.numero)
            put(COLUMN_ENDERECO, newBooking.endereco)
            put(COLUMN_DESCRICAO, newBooking.descricao)
            put(COLUMN_VALOR, newBooking.valor) // Valor como String
            put(COLUMN_BOOKING_GROUP_ID, newBooking.bookingGroupId) // Atualiza o bookingGroupId
        }

        // Para atualizar, precisamos de um ID único ou uma combinação única de colunas.
        // Se você tiver um COLUMN_ID, use-o. Se não, use a combinação de dia, mes, ano e bookingGroupId.
        // A melhor prática é usar o COLUMN_ID.
        val rowsAffected = db.update(
            TABLE_RESERVAS,
            contentValues,
            "$COLUMN_DIA = ? AND $COLUMN_MES = ? AND $COLUMN_ANO = ? AND $COLUMN_BOOKING_GROUP_ID = ?",
            arrayOf(
                oldBooking.dia.toString(),
                oldBooking.mes.toString(),
                oldBooking.ano.toString(),
                oldBooking.bookingGroupId
            )
        )
        db.close()
        return rowsAffected
    }

    // Método para deletar uma única reserva (ainda usando dia, mes, ano)
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

    // Método para deletar todas as reservas de um grupo
    fun deleteBookingGroup(bookingGroupId: String): Int {
        val db = this.writableDatabase
        val rowsAffected = db.delete(TABLE_RESERVAS, "$COLUMN_BOOKING_GROUP_ID = ?", arrayOf(bookingGroupId))
        db.close()
        return rowsAffected
    }

    // Método para excluir reservas para datas específicas (se o grupo for alterado para menos datas)
    fun deleteSpecificDatesFromBookingGroup(bookingGroupId: String, datesToDelete: List<Triple<Int, Int, Int>>): Int {
        val db = this.writableDatabase
        var rowsAffected = 0
        db.beginTransaction()
        try {
            for (date in datesToDelete) {
                val whereClause = "$COLUMN_BOOKING_GROUP_ID = ? AND $COLUMN_DIA = ? AND $COLUMN_MES = ? AND $COLUMN_ANO = ?"
                val whereArgs = arrayOf(bookingGroupId, date.first.toString(), date.second.toString(), date.third.toString())
                rowsAffected += db.delete(TABLE_RESERVAS, whereClause, whereArgs)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error deleting specific dates: ${e.message}")
        } finally {
            db.endTransaction()
        }
        return rowsAffected
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
                    val valor = it.getString(it.getColumnIndexOrThrow(COLUMN_VALOR)) // MUDANÇA AQUI: getString
                    val bookingGroupId = it.getString(it.getColumnIndexOrThrow(COLUMN_BOOKING_GROUP_ID)) // NEW: Get bookingGroupId

                    val booking = Booking(dia, mes, ano, nome, numero, endereco, descricao, valor, bookingGroupId) // NEW: Pass bookingGroupId
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
                val valor = it.getString(it.getColumnIndexOrThrow(COLUMN_VALOR)) // MUDANÇA AQUI: getString
                val bookingGroupId = it.getString(it.getColumnIndexOrThrow(COLUMN_BOOKING_GROUP_ID)) // NEW: Get bookingGroupId

                booking = Booking(dia, mes, ano, nome, numero, endereco, descricao, valor, bookingGroupId) // NEW: Pass bookingGroupId
            }
            it.close()
        }
        db.close()
        return booking
    }

    fun getBookingsByGroup(bookingGroupId: String): List<Booking> {
        val bookingList = mutableListOf<Booking>()
        val selectQuery = "SELECT * FROM $TABLE_RESERVAS WHERE $COLUMN_BOOKING_GROUP_ID = ?"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(bookingGroupId))

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
                    val valor = it.getString(it.getColumnIndexOrThrow(COLUMN_VALOR))
                    val groupId = it.getString(it.getColumnIndexOrThrow(COLUMN_BOOKING_GROUP_ID))

                    val booking = Booking(dia, mes, ano, nome, numero, endereco, descricao, valor, groupId)
                    bookingList.add(booking)
                } while (it.moveToNext())
            }
            it.close()
        }
        db.close()
        return bookingList
    }

    fun updateBookingGroupDetails(bookingGroupId: String, values: ContentValues): Int {
        val db = this.writableDatabase
        val rowsAffected = db.update(TABLE_RESERVAS, values, "$COLUMN_BOOKING_GROUP_ID = ?", arrayOf(bookingGroupId))
        db.close()
        return rowsAffected
    }

}