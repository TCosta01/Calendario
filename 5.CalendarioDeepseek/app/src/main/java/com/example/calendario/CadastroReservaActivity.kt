package com.example.calendario

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// CadastroReservaActivity.kt
class CadastroReservaActivity : AppCompatActivity() {
    private lateinit var dbHelper: ReservaDbHelper
    private lateinit var tvDatas: TextView
    private lateinit var etNome: EditText
    private lateinit var etNumero: EditText
    private lateinit var etEndereco: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etValor: EditText
    private lateinit var btnInserir: Button
    private lateinit var btnAtualizar: Button
    private lateinit var btnExcluir: Button

    private var datasSelecionadas = emptyArray<Triple<Int, Int, Int>>()
    private var isNovaReserva = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_reserva)

        dbHelper = ReservaDbHelper(this)

        // Inicializar views
        tvDatas = findViewById(R.id.tvDatas)
        etNome = findViewById(R.id.etNome)
        etNumero = findViewById(R.id.etNumero)
        etEndereco = findViewById(R.id.etEndereco)
        etDescricao = findViewById(R.id.etDescricao)
        etValor = findViewById(R.id.etValor)
        btnInserir = findViewById(R.id.btnInserir)
        btnAtualizar = findViewById(R.id.btnAtualizar)
        btnExcluir = findViewById(R.id.btnExcluir)

        // Obter dados da intent
        isNovaReserva = intent.getBooleanExtra("novaReserva", true)
        datasSelecionadas = intent.getSerializableExtra("datas") as Array<Triple<Int, Int, Int>>

        // Configurar interface baseada no tipo de operação
        if (isNovaReserva) {
            btnInserir.visibility = View.VISIBLE
            btnAtualizar.visibility = View.GONE
            btnExcluir.visibility = View.GONE
        } else {
            btnInserir.visibility = View.GONE
            btnAtualizar.visibility = View.VISIBLE
            btnExcluir.visibility = View.VISIBLE

            // Preencher campos com dados existentes
            etNome.setText(intent.getStringExtra("nome"))
            etNumero.setText(intent.getStringExtra("numero"))
            etEndereco.setText(intent.getStringExtra("endereco"))
            etDescricao.setText(intent.getStringExtra("descricao"))
            etValor.setText(intent.getDoubleExtra("valor", 0.0).toString())
        }

        // Exibir datas selecionadas
        val datasFormatadas = datasSelecionadas.joinToString("\n") { (dia, mes, ano) ->
            "$dia/${mes + 1}/$ano"
        }
        tvDatas.text = "Datas selecionadas:\n$datasFormatadas"

        // Configurar listeners dos botões
        btnInserir.setOnClickListener { inserirReserva() }
        btnAtualizar.setOnClickListener { atualizarReserva() }
        btnExcluir.setOnClickListener { excluirReserva() }
    }

    private fun inserirReserva() {
        if (validarCampos()) {
            val reserva = criarReservaAPartirDosCampos()

            // Inserir para cada data selecionada
            var sucesso = true
            for ((dia, mes, ano) in datasSelecionadas) {
                val reservaParaData = reserva.copy(dia = dia, mes = mes, ano = ano)
                if (!dbHelper.inserirReserva(reservaParaData)) {
                    sucesso = false
                    break
                }
            }

            if (sucesso) {
                Toast.makeText(this, "Reserva inserida com sucesso!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Erro ao inserir reserva", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun atualizarReserva() {
        if (validarCampos()) {
            val reservaAntiga = datasSelecionadas[0] // Só podemos editar uma reserva por vez
            val novaReserva = criarReservaAPartirDosCampos()

            // Atualizar a reserva (remove a antiga e insere a nova)
            val sucesso = dbHelper.atualizarReserva(
                reservaAntiga,
                novaReserva.copy(dia = reservaAntiga.first, mes = reservaAntiga.second, ano = reservaAntiga.third)
            )

            if (sucesso) {
                Toast.makeText(this, "Reserva atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Erro ao atualizar reserva", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun excluirReserva() {
        // Excluir cada data selecionada
        var sucesso = true
        for ((dia, mes, ano) in datasSelecionadas) {
            if (!dbHelper.deletarReserva(dia, mes, ano)) {
                sucesso = false
                break
            }
        }

        if (sucesso) {
            Toast.makeText(this, "Reserva excluída com sucesso!", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Erro ao excluir reserva", Toast.LENGTH_SHORT).show()
        }
    }

    private fun criarReservaAPartirDosCampos(): Reserva {
        return Reserva(
            dia = 0, // Será definido para cada data
            mes = 0,  // Será definido para cada data
            ano = 0,  // Será definido para cada data
            nome = etNome.text.toString(),
            numero = etNumero.text.toString(),
            endereco = etEndereco.text.toString(),
            descricao = etDescricao.text.toString(),
            valor = etValor.text.toString().toDoubleOrNull() ?: 0.0
        )
    }

    private fun validarCampos(): Boolean {
        if (etNome.text.isBlank()) {
            etNome.error = "Nome é obrigatório"
            return false
        }

        if (etNumero.text.isBlank()) {
            etNumero.error = "Número é obrigatório"
            return false
        }

        if (etEndereco.text.isBlank()) {
            etEndereco.error = "Endereço é obrigatório"
            return false
        }

        if (etValor.text.isBlank()) {
            etValor.error = "Valor é obrigatório"
            return false
        }

        return true
    }
}