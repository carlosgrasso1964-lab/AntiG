package com.antig.calcAS

import android.os.Bundle
import java.util.Locale
import android.view.KeyEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var expressionText: TextView
    private lateinit var resultText: TextView
    private lateinit var historyList: RecyclerView

    private var currentExpression = ""
    private var lastResult: Double? = null
    private var justCalculated = false
    private val historyEntries = mutableListOf<HistoryEntry>()
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expressionText = findViewById(R.id.expressionText)
        resultText = findViewById(R.id.resultText)
        historyList = findViewById(R.id.historyList)

        historyAdapter = HistoryAdapter(historyEntries)
        historyList.layoutManager = LinearLayoutManager(this)
        historyList.adapter = historyAdapter

        setupButtonListeners()
        updateDisplay()
    }

    private fun setupButtonListeners() {
        findViewById<Button>(R.id.btn0).setOnClickListener { handleNumber("0") }
        findViewById<Button>(R.id.btn1).setOnClickListener { handleNumber("1") }
        findViewById<Button>(R.id.btn2).setOnClickListener { handleNumber("2") }
        findViewById<Button>(R.id.btn3).setOnClickListener { handleNumber("3") }
        findViewById<Button>(R.id.btn4).setOnClickListener { handleNumber("4") }
        findViewById<Button>(R.id.btn5).setOnClickListener { handleNumber("5") }
        findViewById<Button>(R.id.btn6).setOnClickListener { handleNumber("6") }
        findViewById<Button>(R.id.btn7).setOnClickListener { handleNumber("7") }
        findViewById<Button>(R.id.btn8).setOnClickListener { handleNumber("8") }
        findViewById<Button>(R.id.btn9).setOnClickListener { handleNumber("9") }

        findViewById<Button>(R.id.btnAdd).setOnClickListener { handleOperator("add") }
        findViewById<Button>(R.id.btnSubtract).setOnClickListener { handleOperator("subtract") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { handleOperator("multiply") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { handleOperator("divide") }

        findViewById<Button>(R.id.btnDot).setOnClickListener { handleDot() }
        findViewById<Button>(R.id.btnPercent).setOnClickListener { handlePercent() }
        findViewById<Button>(R.id.btnSqrt).setOnClickListener { handleSquareRoot() }
        findViewById<Button>(R.id.btnPower).setOnClickListener { handlePower() }
        findViewById<Button>(R.id.btnOpenParen).setOnClickListener { handleOpenParen() }
        findViewById<Button>(R.id.btnCloseParen).setOnClickListener { handleCloseParen() }

        findViewById<Button>(R.id.btnClearAll).setOnClickListener { onClearAll() }
        findViewById<Button>(R.id.btnClearEntry).setOnClickListener { onClearEntry() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { onDeleteLast() }
        findViewById<Button>(R.id.btnEqual).setOnClickListener { performCalculation() }
    }

    private fun handleNumber(num: String) {
        if (justCalculated) {
            currentExpression = num
            justCalculated = false
        } else {
            currentExpression += num
        }
        updateDisplay()
    }

    private fun handleOperator(op: String) {
        val operatorSymbol = when (op) {
            "add" -> "+"
            "subtract" -> "−"
            "multiply" -> "×"
            "divide" -> "÷"
            else -> op
        }

        if (justCalculated && lastResult != null) {
            currentExpression = formatNumber(lastResult!!) + operatorSymbol
            justCalculated = false
        } else if (currentExpression.isEmpty() && lastResult != null) {
            currentExpression = formatNumber(lastResult!!) + operatorSymbol
        } else if (currentExpression.isNotEmpty()) {
            val lastChar = currentExpression.last()
            if (lastChar in listOf('+', '−', '×', '÷', '^')) {
                currentExpression = currentExpression.dropLast(1) + operatorSymbol
            } else {
                currentExpression += operatorSymbol
            }
        }
        updateDisplay()
    }

    private fun handleDot() {
        if (justCalculated) {
            currentExpression = "0."
            justCalculated = false
            updateDisplay()
            return
        }
        val parts = currentExpression.split(Regex("[+\\-−×÷^√]"))
        val lastPart = parts.lastOrNull() ?: ""
        if (lastPart.contains(".").not()) {
            currentExpression += if (currentExpression.isEmpty()) "0." else "."
        }
        updateDisplay()
    }

    private fun handlePercent() {
        if (justCalculated && lastResult != null) {
            currentExpression = (lastResult!! / 100.0).toString()
            justCalculated = false
            updateDisplay()
            return
        }
        if (currentExpression.isEmpty() && lastResult != null) {
            currentExpression = formatNumber(lastResult!!) + "%"
        } else if (currentExpression.isNotEmpty()) {
            currentExpression += "%"
        }
        updateDisplay()
    }

    private fun handleSquareRoot() {
        if (justCalculated && lastResult != null) {
            val sqrtResult = sqrt(lastResult!!)
            addHistoryRecord("√(${formatNumber(lastResult!!)})", sqrtResult)
            lastResult = sqrtResult
            currentExpression = ""
            justCalculated = true
            resultText.text = formatNumber(lastResult!!)
            updateDisplay()
            return
        }
        if (currentExpression.isNotEmpty()) {
            currentExpression = "√($currentExpression)"
            updateDisplay()
        } else if (lastResult != null) {
            currentExpression = "√(${formatNumber(lastResult!!)})"
            justCalculated = false
            updateDisplay()
        }
    }

    private fun handlePower() {
        if (justCalculated && lastResult != null) {
            currentExpression = formatNumber(lastResult!!) + "^"
            justCalculated = false
            updateDisplay()
            return
        }
        if (currentExpression.isNotEmpty()) {
            currentExpression += "^"
            updateDisplay()
        } else if (lastResult != null) {
            currentExpression = formatNumber(lastResult!!) + "^"
            updateDisplay()
        }
    }

    private fun handleOpenParen() {
        if (justCalculated) {
            currentExpression = "("
            justCalculated = false
        } else {
            currentExpression += "("
        }
        updateDisplay()
    }

    private fun handleCloseParen() {
        currentExpression += ")"
        updateDisplay()
    }

    private fun performCalculation(): Boolean {
        var expr = currentExpression.trim()
        if (expr.isEmpty() && lastResult != null) {
            expr = formatNumber(lastResult!!)
        }
        if (expr.isEmpty()) return false

        val evalResult = CalculatorEngine.evaluate(expr)
        if (evalResult != null && evalResult.isNaN().not()) {
            addHistoryRecord(expr, evalResult)
            lastResult = evalResult
            currentExpression = ""
            justCalculated = true
            resultText.text = formatNumber(evalResult)
            expressionText.text = ""
            return true
        } else {
            resultText.text = "ERRO"
            lastResult = null
            justCalculated = true
            expressionText.postDelayed({
                if (resultText.text == "ERRO") {
                    resultText.text = if (lastResult != null) formatNumber(lastResult!!) else "0"
                }
            }, 1200)
            return false
        }
    }

    private fun onClearAll() {
        currentExpression = ""
        lastResult = null
        justCalculated = false
        expressionText.text = ""
        resultText.text = "0"
        updateDisplay()
    }

    private fun onClearEntry() {
        currentExpression = ""
        justCalculated = false
        updateDisplay()
        resultText.text = if (lastResult != null) formatNumber(lastResult!!) else "0"
    }

    private fun onDeleteLast() {
        if (justCalculated) {
            justCalculated = false
            currentExpression = ""
            updateDisplay()
            if (lastResult != null) resultText.text = formatNumber(lastResult!!)
            return
        }
        if (currentExpression.isNotEmpty()) {
            currentExpression = currentExpression.dropLast(1)
        }
        updateDisplay()
    }

    private fun updateDisplay() {
        expressionText.text = currentExpression
        when {
            currentExpression.isEmpty() && lastResult != null && !justCalculated ->
                resultText.text = formatNumber(lastResult!!)
            currentExpression.isNotEmpty() ->
                resultText.text = if (lastResult != null && !justCalculated) formatNumber(lastResult!!) else ""
            justCalculated && lastResult != null ->
                resultText.text = formatNumber(lastResult!!)
            lastResult == null ->
                resultText.text = "0"
        }
    }

    private fun addHistoryRecord(calculationText: String, resultValue: Double) {
        val displayCalc = calculationText
            .replace("/", "÷")
            .replace("*", "×")
            .replace("-", "−")
        val resultStr = formatNumber(resultValue)
        historyEntries.add(0, HistoryEntry(displayCalc, resultStr))
        if (historyEntries.size > 20) {
            historyEntries.removeAt(historyEntries.lastIndex)
        }
        historyAdapter.notifyDataSetChanged()
    }

    private fun formatNumber(value: Double): String {
        return if (value == value.toLong().toDouble() && value.isFinite()) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.8f", value).trimEnd('0').trimEnd('.')
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_0 -> handleNumber("0")
            KeyEvent.KEYCODE_1 -> handleNumber("1")
            KeyEvent.KEYCODE_2 -> handleNumber("2")
            KeyEvent.KEYCODE_3 -> handleNumber("3")
            KeyEvent.KEYCODE_4 -> handleNumber("4")
            KeyEvent.KEYCODE_5 -> handleNumber("5")
            KeyEvent.KEYCODE_6 -> handleNumber("6")
            KeyEvent.KEYCODE_7 -> handleNumber("7")
            KeyEvent.KEYCODE_8 -> handleNumber("8")
            KeyEvent.KEYCODE_9 -> handleNumber("9")
            KeyEvent.KEYCODE_PLUS -> handleOperator("add")
            KeyEvent.KEYCODE_MINUS -> handleOperator("subtract")
            KeyEvent.KEYCODE_PERIOD -> handleDot()
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_EQUALS -> performCalculation()
            KeyEvent.KEYCODE_DEL -> onDeleteLast()
            KeyEvent.KEYCODE_FORWARD_DEL -> onClearAll()
            KeyEvent.KEYCODE_ESCAPE -> onClearEntry()
            KeyEvent.KEYCODE_NUMPAD_LEFT_PAREN -> handleOpenParen()
            KeyEvent.KEYCODE_NUMPAD_RIGHT_PAREN -> handleCloseParen()
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }
}
