package com.example.calendario

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReservaActivity : AppCompatActivity() {
    private lateinit var dbHelper: ReservaDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reserva)

        dbHelper = ReservaDbHelper(this)

        val dias = intent.getIntArrayExtra("dias") ?: intArrayOf()
        val meses = intent.getIntArrayExtra("meses") ?: intArrayOf()
        val anos = intent.getIntArrayExtra("anos") ?: intArrayOf()

        val edtNome = findViewById<EditText>(R.id.edtNome)
        val edtNumero = findViewById<EditText>(R.id.edtNumero)
        val edtEndereco = findViewById<EditText>(R.id.edtEndereco)
        val edtDescricao = findViewById<EditText>(R.id.edtDescricao)
        val edtValor = findViewById<EditText>(R.id.edtValor)
        val txtData = findViewById<TextView>(R.id.txtData)

        val textoDatas = dias.indices.joinToString(", ") {
            "${dias[it]}/${meses[it]}/${anos[it]}"
        }
        txtData.text = textoDatas

        findViewById<Button>(R.id.btnInserir).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                val nome = edtNome.text.toString()
                val numero = edtNumero.text.toString()
                val endereco = edtEndereco.text.toString()
                val descricao = edtDescricao.text.toString()
                val valor = edtValor.text.toString().toDouble()

                lifecycleScope.launch(Dispatchers.IO) {
                    dias.indices.forEach {
                        val reserva = Reserva(
                            dia = dias[it],
                            mes = meses[it],
                            ano = anos[it],
                            nome = nome,
                            numero = numero,
                            endereco = endereco,
                            descricao = descricao,
                            valor = valor
                        )
                        dbHelper.inserirReserva(reserva)
                    }
                    finish()
                }
            }
        }
    }
}
