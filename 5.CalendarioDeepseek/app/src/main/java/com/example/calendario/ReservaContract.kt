package com.example.calendario

import android.provider.BaseColumns


object ReservaContract {
    object ReservaEntry : BaseColumns {
        const val TABLE_NAME = "Reservas"
        const val COLUMN_DIA = "dia"
        const val COLUMN_MES = "mes"
        const val COLUMN_ANO = "ano"
        const val COLUMN_NOME = "nome"
        const val COLUMN_NUMERO = "numero"
        const val COLUMN_ENDERECO = "endereco"
        const val COLUMN_DESCRICAO = "descricao"
        const val COLUMN_VALOR = "valor"
    }
}

