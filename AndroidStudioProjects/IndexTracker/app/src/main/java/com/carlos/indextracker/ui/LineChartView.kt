package com.carlos.indextracker.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.carlos.indextracker.R
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * Gráfico de linha simples desenhado em Canvas, sem dependências externas.
 */
class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var datas: List<String> = emptyList()
    private var valores: List<Double> = emptyList()
    private var corLinha: Int = Color.parseColor("#1f4f8a")
    private var casasDecimais: Int = 2

    private val densidade = resources.displayMetrics.density
    private val pinturaLinha = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * densidade
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val pinturaArea = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val pinturaGrade = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * densidade
        color = context.getColor(R.color.chart_grid)
    }
    private val pinturaTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.chart_text)
        textSize = 10f * resources.displayMetrics.scaledDensity
    }
    private val pinturaPonto = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val pinturaVazio = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.chart_text)
        textSize = 12f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
    }

    fun configurar(
        datas: List<String>,
        valores: List<Double>,
        cor: Int,
        casasDecimais: Int
    ) {
        this.datas = datas
        this.valores = valores
        this.corLinha = cor
        this.casasDecimais = casasDecimais
        pinturaLinha.color = cor
        pinturaPonto.color = cor
        pinturaArea.color = Color.argb(38, Color.red(cor), Color.green(cor), Color.blue(cor))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (valores.isEmpty()) {
            canvas.drawText(context.getString(R.string.sem_dados), width / 2f, height / 2f, pinturaVazio)
            return
        }

        val margemEsquerda = 46f * densidade
        val margemDireita = 8f * densidade
        val margemTopo = 10f * densidade
        val margemBase = 22f * densidade
        val larguraUtil = width - margemEsquerda - margemDireita
        val alturaUtil = height - margemTopo - margemBase
        if (larguraUtil <= 0 || alturaUtil <= 0) return

        var minimo = valores.min()
        var maximo = valores.max()
        if (minimo == maximo) {
            val delta = max(1.0, kotlin.math.abs(minimo) * 0.02)
            minimo -= delta
            maximo += delta
        } else {
            val folga = (maximo - minimo) * 0.12
            minimo -= folga
            maximo += folga
        }

        // Linhas de grade + rótulos do eixo Y
        val linhasGrade = 4
        for (i in 0..linhasGrade) {
            val y = margemTopo + alturaUtil * i / linhasGrade
            canvas.drawLine(margemEsquerda, y, width - margemDireita, y, pinturaGrade)
            val valor = maximo - (maximo - minimo) * i / linhasGrade
            val rotulo = formatar(valor)
            pinturaTexto.textAlign = Paint.Align.RIGHT
            canvas.drawText(rotulo, margemEsquerda - 6f * densidade, y + 3f * densidade, pinturaTexto)
        }

        val passo = if (valores.size > 1) larguraUtil / (valores.size - 1) else 0f
        fun xDe(posicao: Int) = if (valores.size > 1) margemEsquerda + passo * posicao else margemEsquerda + larguraUtil / 2f
        fun yDe(valor: Double) =
            margemTopo + alturaUtil * ((maximo - valor) / (maximo - minimo)).toFloat()

        // Área preenchida sob a linha
        val caminho = Path()
        val area = Path()
        valores.indices.forEach { i ->
            val x = xDe(i)
            val y = yDe(valores[i])
            if (i == 0) {
                caminho.moveTo(x, y)
                area.moveTo(x, margemTopo + alturaUtil)
                area.lineTo(x, y)
            } else {
                caminho.lineTo(x, y)
                area.lineTo(x, y)
            }
        }
        area.lineTo(xDe(valores.size - 1), margemTopo + alturaUtil)
        area.close()
        canvas.drawPath(area, pinturaArea)
        canvas.drawPath(caminho, pinturaLinha)

        // Pontos (apenas quando há poucos registros)
        if (valores.size <= 20) {
            for (i in valores.indices) {
                canvas.drawCircle(xDe(i), yDe(valores[i]), 2.5f * densidade, pinturaPonto)
            }
        }

        // Rótulos do eixo X (primeiro, último e, se couber, o do meio)
        if (valores.size == 1) {
            pinturaTexto.textAlign = Paint.Align.CENTER
            canvas.drawText(
                formatarData(datas.firstOrNull()),
                margemEsquerda + larguraUtil / 2f, height - 6f * densidade, pinturaTexto
            )
        } else {
            pinturaTexto.textAlign = Paint.Align.LEFT
            canvas.drawText(formatarData(datas.firstOrNull()), margemEsquerda, height - 6f * densidade, pinturaTexto)
            if (valores.size > 2) {
                pinturaTexto.textAlign = Paint.Align.CENTER
                val meio = valores.size / 2
                canvas.drawText(formatarData(datas.getOrNull(meio)), xDe(meio), height - 6f * densidade, pinturaTexto)
            }
            pinturaTexto.textAlign = Paint.Align.RIGHT
            canvas.drawText(formatarData(datas.lastOrNull()), width - margemDireita, height - 6f * densidade, pinturaTexto)
        }
    }

    private fun formatar(valor: Double): String {
        val formatador = NumberFormat.getNumberInstance(Locale("pt", "BR"))
        formatador.maximumFractionDigits = min(4, casasDecimais)
        formatador.minimumFractionDigits = 0
        return formatador.format(valor)
    }

    private fun formatarData(data: String?): String {
        if (data.isNullOrBlank()) return ""
        return runCatching {
            LocalDate.parse(data).format(DateTimeFormatter.ofPattern("dd/MM"))
        }.getOrDefault(data)
    }
}
