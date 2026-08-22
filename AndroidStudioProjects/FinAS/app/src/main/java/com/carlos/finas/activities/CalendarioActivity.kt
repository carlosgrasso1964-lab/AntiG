package com.carlos.finas.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carlos.finas.databinding.ActivityCalendarioBinding
import java.util.*

class CalendarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarioBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Definir data inicial
        updateMonthYear()

        // Botões de navegação
        binding.btnPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            binding.calendarView.date = calendar.timeInMillis
            updateMonthYear()
        }

        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            binding.calendarView.date = calendar.timeInMillis
            updateMonthYear()
        }

        // Voltar
        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun updateMonthYear() {
        val monthYear = android.text.format.DateFormat.format("MMMM yyyy", calendar)
        binding.tvMonthYear.text = monthYear.toString()
    }
}