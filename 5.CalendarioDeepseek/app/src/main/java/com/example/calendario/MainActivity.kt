import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.calendario.CadastroReservaActivity
import com.example.calendario.R
import com.example.calendario.ReservaDbHelper
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.util.*
import com.example.calendario.EventDay

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: ReservaDbHelper
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var btnDone: Button
    private val selectedDates = mutableListOf<CalendarDay>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = ReservaDbHelper(this)
        calendarView = findViewById(R.id.calendarView)
        btnDone = findViewById(R.id.btnDone)

        // Configurar o calendário
        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_MULTIPLE
        calendarView.setOnDateChangedListener { _, date, selected ->
            if (selected) {
                selectedDates.add(date)
            } else {
                selectedDates.remove(date)
            }
        }

        // Carregar datas reservadas
        carregarDatasReservadas()

        btnDone.setOnClickListener {
            if (selectedDates.isNotEmpty()) {
                verificarERedirecionarParaCadastro()
            } else {
                Toast.makeText(this, "Selecione pelo menos uma data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun carregarDatasReservadas() {
        val datasReservadas = dbHelper.buscarDatasReservadas()
        val eventos = datasReservadas.map {
            CalendarDay.from(it.third, it.second - 1, it.first)
        }.map {
            EventDay(it, Color.BLUE)
        }

        calendarView.setEvents(eventos)
    }

    private fun verificarERedirecionarParaCadastro() {
        // Verificar se todas as datas selecionadas estão disponíveis
        val todasDisponiveis = selectedDates.all { date ->
            dbHelper.buscarReserva(date.day, date.month + 1, date.year) == null
        }

        // Verificar se todas as datas selecionadas estão reservadas (para edição)
        val todasReservadas = selectedDates.all { date ->
            dbHelper.buscarReserva(date.day, date.month + 1, date.year) != null
        }

        when {
            todasDisponiveis -> {
                // Nova reserva
                val intent = Intent(this, CadastroReservaActivity::class.java).apply {
                    putExtra("novaReserva", true)
                    putExtra("datas", selectedDates.map {
                        Triple(it.day, it.month + 1, it.year)
                    }.toTypedArray())
                }
                startActivityForResult(intent, REQUEST_CODE_CADASTRO)
            }
            todasReservadas && selectedDates.size == 1 -> {
                // Editar reserva existente
                val reserva = dbHelper.buscarReserva(
                    selectedDates[0].day,
                    selectedDates[0].month + 1,
                    selectedDates[0].year
                )

                reserva?.let {
                    val intent = Intent(this, CadastroReservaActivity::class.java).apply {
                        putExtra("novaReserva", false)
                        putExtra("datas", arrayOf(
                            Triple(selectedDates[0].day, selectedDates[0].month + 1, selectedDates[0].year)
                        ))
                        putExtra("nome", it.nome)
                        putExtra("numero", it.numero)
                        putExtra("endereco", it.endereco)
                        putExtra("descricao", it.descricao)
                        putExtra("valor", it.valor)
                    }
                    startActivityForResult(intent, REQUEST_CODE_CADASTRO)
                } ?: run {
                    Toast.makeText(
                        this,
                        "Erro ao carregar reserva",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                Toast.makeText(
                    this,
                    "Você selecionou uma mistura de datas reservadas e disponíveis. " +
                            "Por favor, selecione apenas datas disponíveis para nova reserva " +
                            "ou apenas uma data reservada para edição.",
                    Toast.LENGTH_LONG
                ).show()
                selectedDates.clear()
                calendarView.clearSelection()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CADASTRO && resultCode == RESULT_OK) {
            selectedDates.clear()
            calendarView.clearSelection()
            carregarDatasReservadas()
        }
    }

    companion object {
        const val REQUEST_CODE_CADASTRO = 1
    }
}