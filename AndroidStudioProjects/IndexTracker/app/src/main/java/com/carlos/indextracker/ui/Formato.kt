package com.carlos.indextracker.ui

import android.graphics.Color
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formato {

    private val ptBr = Locale("pt", "BR")

    fun valor(valor: Double, casasDecimais: Int): String {
        val f = NumberFormat.getNumberInstance(ptBr)
        f.minimumFractionDigits = 0
        f.maximumFractionDigits = casasDecimais.coerceIn(0, 6)
        return f.format(valor)
    }

    fun percentual(percentual: Double): String {
        val sinal = if (percentual > 0) "+" else ""
        return sinal + String.format(ptBr, "%.2f", percentual) + "%"
    }

    fun dataBR(dataIso: String?): String {
        if (dataIso.isNullOrBlank()) return "--/--/----"
        return runCatching {
            LocalDate.parse(dataIso).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }.getOrDefault(dataIso)
    }

    fun parseNumero(texto: String?): Double? =
        texto?.trim()?.replace(",", ".")?.toDoubleOrNull()

    fun parseCor(hex: String?, padrao: Int = Color.parseColor("#1f4f8a")): Int =
        runCatching { Color.parseColor(hex) }.getOrDefault(padrao)
}
