import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.calendario.R
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvMonthYear: TextView
    private lateinit var gridDaysHeader: GridView
    private lateinit var gridCalendar: GridView
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton

    private val currentDate = Calendar.getInstance()
    private val dayLabels = arrayOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvMonthYear = findViewById(R.id.tvMonthYear)
        gridDaysHeader = findViewById(R.id.gridDaysHeader)
        gridCalendar = findViewById(R.id.gridCalendar)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)

        // Configurar cabeçalho dos dias
        gridDaysHeader.adapter = object : BaseAdapter() {
            override fun getCount(): Int = dayLabels.size
            override fun getItem(position: Int): Any = dayLabels[position]
            override fun getItemId(position: Int): Long = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val textView = if (convertView == null) {
                    layoutInflater.inflate(R.layout.item_day, parent, false) as TextView
                } else {
                    convertView as TextView
                }
                textView.text = dayLabels[position]
                textView.setTextColor(resources.getColor(android.R.color.black))
                return textView
            }
        }

        // Configurar botões de navegação
        btnPrevious.setOnClickListener {
            currentDate.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        btnNext.setOnClickListener {
            currentDate.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        updateCalendar()
    }

    private fun updateCalendar() {
        // Atualizar título (mês/ano)
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = monthFormat.format(currentDate.time)

        // Obter dias do mês
        val daysInMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfMonth = Calendar.getInstance().apply {
            time = currentDate.time
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val startingDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)

        // Ajustar para começar na segunda-feira (Calendar.MONDAY = 2)
        val offset = if (startingDayOfWeek == Calendar.SUNDAY) 6 else startingDayOfWeek - 2

        // Criar lista de dias (incluindo dias vazios no início se necessário)
        val daysList = mutableListOf<String>()

        // Adicionar dias do mês anterior (se necessário)
        val prevMonth = Calendar.getInstance().apply {
            time = currentDate.time
            add(Calendar.MONTH, -1)
        }
        val daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in offset downTo 1) {
            daysList.add((daysInPrevMonth - i + 1).toString())
        }

        // Adicionar dias do mês atual
        for (i in 1..daysInMonth) {
            daysList.add(i.toString())
        }

        // Adicionar dias do próximo mês (se necessário)
        val totalCells = if (daysList.size <= 35) 35 else 42
        val nextMonthDays = totalCells - daysList.size
        for (i in 1..nextMonthDays) {
            daysList.add(i.toString())
        }

        // Configurar adapter do calendário
        gridCalendar.adapter = object : BaseAdapter() {
            override fun getCount(): Int = daysList.size
            override fun getItem(position: Int): Any = daysList[position]
            override fun getItemId(position: Int): Long = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val textView = if (convertView == null) {
                    layoutInflater.inflate(R.layout.item_day, parent, false) as TextView
                } else {
                    convertView as TextView
                }

                val day = daysList[position]
                textView.text = day

                // Estilizar diferente os dias que não são do mês atual
                if (position < offset || position >= offset + daysInMonth) {
                    textView.setTextColor(resources.getColor(android.R.color.darker_gray))
                } else {
                    textView.setTextColor(resources.getColor(android.R.color.black))

                    // Destacar o dia atual
                    if (currentDate.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                        currentDate.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                        day.toInt() == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                        textView.setBackgroundResource(R.drawable.circle_background)
                        textView.setTextColor(resources.getColor(android.R.color.white))
                    } else {
                        textView.background = null
                    }
                }

                return textView
            }
        }
    }
}