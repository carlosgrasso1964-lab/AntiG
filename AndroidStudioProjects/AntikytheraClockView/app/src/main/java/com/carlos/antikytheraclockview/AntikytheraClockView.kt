package com.carlos.antikytheraclockview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RadialGradient
import android.graphics.Shader
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import java.time.LocalDateTime
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class AntikytheraClockView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val sunPointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 215, 0)
        style = Paint.Style.FILL
    }
    private val moonPointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(192, 192, 192)
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 24f
        style = Paint.Style.FILL
    }
    private val digitalClockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 20f
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }
    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 18f
        style = Paint.Style.FILL
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val skeletonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        colorFilter = PorterDuffColorFilter(Color.parseColor("#FFD700"), PorterDuff.Mode.SRC_IN)
    }
    private val roosterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        colorFilter = PorterDuffColorFilter(Color.parseColor("#FFD700"), PorterDuff.Mode.SRC_IN)
    }
    private var maxRadius: Float = 0f
    private var skeletonX: Float = 0f
    private var skeletonOffset: Float = 0f
    private var skeletonAnimator: ValueAnimator? = null
    private var roosterAlphaAnimator: ValueAnimator? = null
    private var mediaPlayer: MediaPlayer? = null
    private var lastRoosterTrigger: Long = 0
    private var roosterVisibleUntil: Long = 0
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            invalidate()
            handler.postDelayed(this, 1000)
        }
    }
    private data class Pointer(val x: Float, val y: Float, val info: String)
    private val pointers = mutableListOf<Pointer>()

    init {
        handler.post(updateRunnable)
        isClickable = true
        isFocusable = true
        setupSkeletonAnimation()
        setupRoosterAnimation()
        setupMediaPlayer()
    }

    private fun setupSkeletonAnimation() {
        skeletonAnimator = ValueAnimator.ofFloat(-10f, 10f).apply {
            duration = 1000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                skeletonOffset = animation.animatedValue as Float
                invalidate()
            }
        }
        skeletonAnimator?.start()
    }

    private fun setupRoosterAnimation() {
        roosterAlphaAnimator = ValueAnimator.ofInt(0, 255).apply {
            duration = 5000 // Animação de 5 segundos
            addUpdateListener { animation ->
                roosterPaint.alpha = animation.animatedValue as Int
                invalidate()
            }
        }
    }

    private fun setupMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.rooster_sound)
            mediaPlayer?.setOnCompletionListener {
                it.reset()
                try {
                    it.setDataSource(context, android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.rooster_sound}"))
                    it.prepare()
                } catch (e: Exception) {
                    Log.e("AntikytheraClockView", "Erro ao reiniciar MediaPlayer: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("AntikytheraClockView", "Erro ao inicializar MediaPlayer: ${e.message}")
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = LocalDateTime.now()
        val centerX = width / 2f
        val centerY = height / 2f
        maxRadius = minOf(centerX, centerY) - 60f
        pointers.clear()

        val secondsInDay = now.hour * 3600 + now.minute * 60 + now.second
        val totalSecondsInDay = 24 * 3600
        skeletonX = ((secondsInDay.toFloat() / totalSecondsInDay) * (width - 96f)).coerceIn(0f, width - 96f)

        val gradientColors = intArrayOf(Color.parseColor("#FFD700"), Color.parseColor("#1C2526"))
        backgroundPaint.shader = RadialGradient(
            centerX, centerY, maxRadius * 1.2f, gradientColors, null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Desenha o rooster apenas no horário certo com animação
        val currentTimeMillis = System.currentTimeMillis()
        if (now.minute == 0 && now.second == 0 || now.minute == 30 && now.second == 0) {
            if (currentTimeMillis - lastRoosterTrigger > 1000) {
                mediaPlayer?.start()
                lastRoosterTrigger = currentTimeMillis
                roosterVisibleUntil = currentTimeMillis + 5000 // Visível por 5 segundos
                roosterAlphaAnimator?.start()
                Log.d("AntikytheraClockView", "Rooster sound and animation triggered at ${now.hour}:${now.minute}:${now.second}")
            }
        } else if (currentTimeMillis <= roosterVisibleUntil) {
            // Mantém visível por 5 segundos após o disparo
            roosterPaint.alpha = 255
        } else {
            roosterPaint.alpha = 0 // Oculta quando não é horário e passou o tempo
        }

        try {
            val roosterBitmap = BitmapFactory.decodeResource(resources, R.drawable.rooster)
            if (roosterBitmap != null) {
                val roosterWidth = 96f
                val roosterHeight = 96f
                val roosterLeft = centerX - roosterWidth / 2
                val roosterTop = centerY - maxRadius - roosterHeight - 50f
                val scaledBitmap = Bitmap.createScaledBitmap(roosterBitmap, roosterWidth.toInt(), roosterHeight.toInt(), true)
                canvas.drawBitmap(scaledBitmap, roosterLeft, roosterTop, roosterPaint)
                Log.d("AntikytheraClockView", "Rooster drawn at left=$roosterLeft, top=$roosterTop, width=$roosterWidth, height=$roosterHeight, alpha=${roosterPaint.alpha}")
            } else {
                Log.e("AntikytheraClockView", "rooster.png is null")
            }
        } catch (e: Exception) {
            Log.e("AntikytheraClockView", "Erro ao carregar rooster.png: ${e.message}")
        }

        // Desenha o skeleton animado na base (original)
        try {
            val skeletonBitmap = BitmapFactory.decodeResource(resources, R.drawable.skeleton)
            if (skeletonBitmap != null) {
                val scaledBitmap = Bitmap.createScaledBitmap(skeletonBitmap, 96, 96, true)
                canvas.drawBitmap(scaledBitmap, skeletonX + skeletonOffset, height - 100f, skeletonPaint)
            } else {
                Log.e("AntikytheraClockView", "skeleton.png is null")
            }
        } catch (e: Exception) {
            Log.e("AntikytheraClockView", "Erro ao carregar skeleton.png: ${e.message}")
        }

        val dayOfYear = now.dayOfYear
        val isLeapYear = now.year % 4 == 0 && (now.year % 100 != 0 || now.year % 400 == 0)
        val totalDays = if (isLeapYear) 366 else 365
        val sunPosition = getZodiac(now)
        val sunPositionName = sunPosition.first
        val sunPositionSymbol = sunPosition.second
        val sunZodiacDates = getZodiacDates(sunPosition.third)
        val moonPosition = getMoonZodiac(now)
        val moonPositionName = moonPosition.first
        val moonPositionSymbol = moonPosition.second
        val moonZodiacDates = getZodiacDates(moonPosition.third)
        val season = getSeason(now)
        val seasonName = when (season) {
            0 -> "Verão ☀️"
            1 -> "Outono 🍂"
            2 -> "Inverno ❄️"
            else -> "Primavera 🌸"
        }
        val weekOfYear = now.get(WeekFields.of(Locale.getDefault()).weekOfYear())
        val month = now.monthValue
        val dayOfWeek = now.dayOfWeek.value
        val dayOfWeekName = when (dayOfWeek) {
            1 -> "Segunda"
            2 -> "Terça"
            3 -> "Quarta"
            4 -> "Quinta"
            5 -> "Sexta"
            6 -> "Sábado"
            else -> "Domingo"
        }
        val hour = now.hour
        val minute = now.minute
        val second = now.second
        val moonPhase = calculateMoonPhase(now)
        val moonPhaseName = when {
            moonPhase < 0.125 || moonPhase >= 0.875 -> "Nova 🌑"
            moonPhase < 0.375 -> "Crescente 🌒"
            moonPhase < 0.625 -> "Cheia 🌕"
            else -> "Minguante 🌖"
        }

        drawDaysOfYear(canvas, centerX, centerY, maxRadius, Color.WHITE)
        drawSunPosition(canvas, centerX, centerY, maxRadius * 0.9f, Color.YELLOW)
        drawSeasons(canvas, centerX, centerY, maxRadius * 0.8f, Color.GREEN)
        drawMoonPhases(canvas, centerX, centerY, maxRadius * 0.7f, Color.LTGRAY)
        drawCircleWithTicks(canvas, maxRadius * 0.6f, Color.rgb(200, 150, 0), 52)
        drawCircleWithTicks(canvas, maxRadius * 0.5f, Color.rgb(128, 0, 128), 7)
        drawCircleWithTicks(canvas, maxRadius * 0.4f, Color.CYAN, 24)
        drawCircleWithTicks(canvas, maxRadius * 0.3f, Color.MAGENTA, 60)
        drawCircleWithTicks(canvas, maxRadius * 0.2f, Color.GRAY, 60)

        pointers.add(drawPointer(canvas, centerX, centerY, maxRadius, (dayOfYear.toFloat() / totalDays) * 360f, 10f, "Dia do Ano: $dayOfYear/$totalDays", pointerPaint))
        pointers.add(drawPointer(canvas, centerX, centerY, maxRadius * 0.9f, ((sunPosition.third - 1).toFloat() / 12f) * 360f, 8f, "Posição do Sol: $sunPositionName $sunPositionSymbol: $sunZodiacDates", sunPointerPaint))
        pointers.add(drawPointer(canvas, centerX, centerY, maxRadius * 0.9f, ((moonPosition.third - 1).toFloat() / 12f) * 360f, 8f, "Posição da Lua: $moonPositionName $moonPositionSymbol: $moonZodiacDates", moonPointerPaint))
        pointers.add(drawPointer(canvas, centerX, centerY, maxRadius * 0.8f, (season / 4f) * 360f, 8f, "Estação: $seasonName", pointerPaint))
        pointers.add(drawPointer(canvas, centerX, centerY, maxRadius * 0.7f, moonPhase * 360f, 6f, "Fase da Lua: $moonPhaseName", pointerPaint))
        pointers.add(drawPointer(canvas, centerX, centerY, maxRadius * 0.6f, (weekOfYear.toFloat() / 53f) * 360f, 6f, "Semana: $weekOfYear/53", pointerPaint))
        pointers.add(drawPointer(canvas, centerX, centerY, maxRadius * 0.5f, ((dayOfWeek - 1).toFloat() / 7f) * 360f, 6f, "Dia da Semana: $dayOfWeekName", pointerPaint))
        pointers.add(drawPointer(canvas, centerX, centerY, maxRadius * 0.4f, (hour.toFloat() / 24f) * 360f, 6f, "Hora: $hour", pointerPaint))
        pointers.add(drawPointer(canvas, centerX, centerY, maxRadius * 0.3f, (minute.toFloat() / 60f) * 360f, 6f, "Minuto: $minute", pointerPaint))
        pointers.add(drawPointer(canvas, centerX, centerY, maxRadius * 0.2f, (second.toFloat() / 60f) * 360f, 6f, "Segundo: $second", pointerPaint))

        val hourText = String.format("%02d", hour)
        val minuteText = String.format("%02d", minute)
        val secondText = String.format("%02d", second)
        val lineHeight = 36f//48f
        canvas.drawText(hourText, centerX, centerY - lineHeight, digitalClockPaint)
        canvas.drawText(minuteText, centerX, centerY, digitalClockPaint)
        canvas.drawText(secondText, centerX, centerY + lineHeight, digitalClockPaint)

        val dayOfWeekColor = Color.rgb(186, 85, 211)
        drawLegends(canvas, listOf(
            Pair(Color.WHITE, "Dia do Ano: $dayOfYear/$totalDays"),
            Pair(Color.YELLOW, "Posição do Sol: $sunPositionName $sunPositionSymbol $sunZodiacDates com Lua em $moonPositionName $moonPositionSymbol $moonZodiacDates"),
            Pair(Color.GREEN, "Estações: $seasonName"),
            Pair(Color.LTGRAY, "Fases da Lua: $moonPhaseName"),
            Pair(Color.rgb(200, 150, 0), "Semanas: $weekOfYear/53"),
            Pair(dayOfWeekColor, "Dia da Semana: $dayOfWeekName"),
            Pair(Color.CYAN, "Horas: $hour"),
            Pair(Color.MAGENTA, "Minutos: $minute"),
            Pair(Color.GRAY, "Segundos: $second")
        ))
    }

    private fun drawDaysOfYear(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        paint.color = color
        canvas.drawCircle(cx, cy, radius, paint)
        tickPaint.color = Color.WHITE
        val angleStep = 360f / 12
        val months = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
        for (i in 0 until 12) {
            val angleDeg = i * angleStep
            val angleRad = Math.toRadians(angleDeg.toDouble() - 90).toFloat()
            val startX = cx + radius * cos(angleRad)
            val startY = cy + radius * sin(angleRad)
            val endX = cx + (radius + 5f) * cos(angleRad)
            val endY = cy + (radius + 5f) * sin(angleRad)
            canvas.drawLine(startX, startY, endX, endY, tickPaint)
            val textX = cx + (radius + 25f) * cos(angleRad)
            val textY = cy + (radius + 25f) * sin(angleRad)
            canvas.drawText(months[i], textX, textY, smallTextPaint)
        }
    }

    private fun drawSunPosition(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        paint.color = color
        canvas.drawCircle(cx, cy, radius, paint)
        tickPaint.color = Color.WHITE
        val angleStep = 360f / 12
        val zodiacSymbols = listOf("♈", "♉", "♊", "♋", "♌", "♍", "♎", "♏", "♐", "♑", "♒", "♓")
        for (i in 0 until 12) {
            val angleDeg = i * angleStep
            val angleRad = Math.toRadians(angleDeg.toDouble() - 90).toFloat()
            val startX = cx + radius * cos(angleRad)
            val startY = cy + radius * sin(angleRad)
            val endX = cx + (radius + 5f) * cos(angleRad)
            val endY = cy + (radius + 5f) * sin(angleRad)
            canvas.drawLine(startX, startY, endX, endY, tickPaint)
            val textX = cx + (radius + 20f) * cos(angleRad)
            val textY = cy + (radius + 20f) * sin(angleRad)
            canvas.drawText(zodiacSymbols[i], textX, textY, smallTextPaint)
        }
    }

    private fun drawSeasons(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        paint.color = color
        canvas.drawCircle(cx, cy, radius, paint)
        tickPaint.color = Color.WHITE
        val angleStep = 360f / 4
        val seasonSymbols = listOf("☀️", "🍂", "❄️", "🌸")
        for (i in 0 until 4) {
            val angleDeg = i * angleStep
            val angleRad = Math.toRadians(angleDeg.toDouble() - 90).toFloat()
            val startX = cx + radius * cos(angleRad)
            val startY = cy + radius * sin(angleRad)
            val endX = cx + (radius + 5f) * cos(angleRad)
            val endY = cy + (radius + 5f) * sin(angleRad)
            canvas.drawLine(startX, startY, endX, endY, tickPaint)
            val textX = cx + (radius + 20f) * cos(angleRad)
            val textY = cy + (radius + 20f) * sin(angleRad)
            canvas.drawText(seasonSymbols[i], textX, textY, smallTextPaint)
        }
    }

    private fun drawMoonPhases(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        paint.color = color
        canvas.drawCircle(cx, cy, radius, paint)
        tickPaint.color = Color.WHITE
        val angleStep = 360f / 4
        val phases = listOf("🌑", "🌒", "🌕", "🌖")
        for (i in 0 until 4) {
            val angleDeg = i * angleStep
            val angleRad = Math.toRadians(angleDeg.toDouble() - 90).toFloat()
            val startX = cx + radius * cos(angleRad)
            val startY = cy + radius * sin(angleRad)
            val endX = cx + (radius + 5f) * cos(angleRad)
            val endY = cy + (radius + 5f) * sin(angleRad)
            canvas.drawLine(startX, startY, endX, endY, tickPaint)
            val textX = cx + (radius + 20f) * cos(angleRad)
            val textY = cy + (radius + 20f) * sin(angleRad)
            canvas.drawText(phases[i], textX, textY, smallTextPaint)
        }
    }

    private fun drawCircleWithTicks(canvas: Canvas, radius: Float, color: Int, numTicks: Int) {
        paint.color = color
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)
        tickPaint.color = Color.WHITE
        val angleStep = 360f / numTicks
        for (i in 0 until numTicks) {
            val angleDeg = i * angleStep
            val angleRad = Math.toRadians(angleDeg.toDouble() - 90).toFloat()
            val startX = width / 2f + radius * cos(angleRad)
            val startY = height / 2f + radius * sin(angleRad)
            val endX = width / 2f + (radius + 5f) * cos(angleRad)
            val endY = height / 2f + (radius + 5f) * sin(angleRad)
            canvas.drawLine(startX, startY, endX, endY, tickPaint)
        }
    }

    private fun drawLegends(canvas: Canvas, legends: List<Pair<Int, String>>) {
        val startY = height / 2f + maxRadius + 80f
        val lineHeight = 32f
        legends.forEachIndexed { index, (color, text) ->
            textPaint.color = color
            canvas.drawText(text, 20f, startY + index * lineHeight, textPaint)
        }
    }

    private fun drawPointer(canvas: Canvas, cx: Float, cy: Float, radius: Float, angleDeg: Float, size: Float, info: String, pointerPaint: Paint): Pointer {
        val angleRad = Math.toRadians(angleDeg.toDouble() - 90).toFloat()
        val x = cx + radius * cos(angleRad)
        val y = cy + radius * sin(angleRad)
        canvas.drawCircle(x, y, size, pointerPaint)
        Log.d("AntikytheraClockView", "Pointer added at x=$x, y=$y, info=$info")
        return Pointer(x, y, info)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x
            val touchY = event.y
            Log.d("AntikytheraClockView", "Touch at x=$touchX, y=$touchY")
            pointers.forEach { pointer ->
                val distance = sqrt((touchX - pointer.x) * (touchX - pointer.x) + (touchY - pointer.y) * (touchY - pointer.y))
                Log.d("AntikytheraClockView", "Distance to pointer at x=${pointer.x}, y=${pointer.y}: $distance")
                if (distance < 50f) {
                    Toast.makeText(context, pointer.info, Toast.LENGTH_SHORT).show()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getSeason(now: LocalDateTime): Int {
        val month = now.monthValue
        val day = now.dayOfMonth
        return when {
            (month == 12 && day >= 21) || (month in 1..2) || (month == 3 && day < 20) -> 0
            (month == 3 && day >= 20) || (month in 4..5) || (month == 6 && day < 21) -> 1
            (month == 6 && day >= 21) || (month in 7..8) || (month == 9 && day < 23) -> 2
            else -> 3
        }
    }

    private fun getZodiac(now: LocalDateTime): Triple<String, String, Int> {
        val month = now.monthValue
        val day = now.dayOfMonth
        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> Triple("Áries", "♈", 1)
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> Triple("Touro", "♉", 2)
            (month == 5 && day >= 21) || (month == 6 && day <= 20) -> Triple("Gêmeos", "♊", 3)
            (month == 6 && day >= 21) || (month == 7 && day <= 22) -> Triple("Câncer", "♋", 4)
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> Triple("Leão", "♌", 5)
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> Triple("Virgem", "♍", 6)
            (month == 9 && day >= 23) || (month == 10 && day <= 22) -> Triple("Libra", "♎", 7)
            (month == 10 && day >= 23) || (month == 11 && day <= 21) -> Triple("Escorpião", "♏", 8)
            (month == 11 && day >= 22) || (month == 12 && day <= 21) -> Triple("Sagitário", "♐", 9)
            (month == 12 && day >= 22) || (month == 1 && day <= 19) -> Triple("Capricórnio", "♑", 10)
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> Triple("Aquário", "♒", 11)
            else -> Triple("Peixes", "♓", 12)
        }
    }

    private fun getMoonZodiac(now: LocalDateTime): Triple<String, String, Int> {
        val julianDate = now.toLocalDate().toEpochDay() + 2451545.0
        val daysSinceNewMoon = (julianDate - 2451550.1) % 29.53058867
        val moonZodiacPosition = (daysSinceNewMoon / 2.27).toInt() % 12
        return when (moonZodiacPosition) {
            0 -> Triple("Áries", "♈", 1)
            1 -> Triple("Touro", "♉", 2)
            2 -> Triple("Gêmeos", "♊", 3)
            3 -> Triple("Câncer", "♋", 4)
            4 -> Triple("Leão", "♌", 5)
            5 -> Triple("Virgem", "♍", 6)
            6 -> Triple("Libra", "♎", 7)
            7 -> Triple("Escorpião", "♏", 8)
            8 -> Triple("Sagitário", "♐", 9)
            9 -> Triple("Capricórnio", "♑", 10)
            10 -> Triple("Aquário", "♒", 11)
            else -> Triple("Peixes", "♓", 12)
        }
    }

    private fun getZodiacDates(zodiacIndex: Int): String {
        return when (zodiacIndex) {
            1 -> "21/03-19/04"
            2 -> "20/04-20/05"
            3 -> "21/05-20/06"
            4 -> "21/06-22/07"
            5 -> "23/07-22/08"
            6 -> "23/08-22/09"
            7 -> "23/09-22/10"
            8 -> "23/10-21/11"
            9 -> "22/11-21/12"
            10 -> "22/12-19/01"
            11 -> "20/01-18/02"
            12 -> "19/02-20/03"
            else -> ""
        }
    }

    private fun calculateMoonPhase(now: LocalDateTime): Float {
        val julianDate = now.toLocalDate().toEpochDay() + 2451545.0
        val daysSinceNewMoon = (julianDate - 2451550.1) % 29.53058867
        return (daysSinceNewMoon / 29.53058867).toFloat()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(updateRunnable)
        skeletonAnimator?.cancel()
        roosterAlphaAnimator?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        invalidate()
    }
}