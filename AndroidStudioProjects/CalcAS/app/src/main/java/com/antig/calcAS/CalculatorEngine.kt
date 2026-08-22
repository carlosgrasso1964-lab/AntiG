package com.antig.calcAS

import kotlin.math.sqrt
import kotlin.math.pow

object CalculatorEngine {

    fun evaluate(expression: String): Double? {
        if (expression.isBlank()) return null
        try {
            val converted = convertExpression(expression)
            val result = evaluateSimple(converted)
            if (result.isNaN() || result.isInfinite()) return null
            return kotlin.math.round(result * 1_000_000_000.0) / 1_000_000_000.0
        } catch (e: Exception) {
            return null
        }
    }

    private fun convertExpression(expr: String): String {
        var result = expr
            .replace("÷", "/")
            .replace("×", "*")
            .replace("−", "-")
        result = result.replace(Regex("√\\(([^)]+)\\)")) { match ->
            "sqrt(${match.groupValues[1]})"
        }
        result = result.replace(Regex("√(\\d+\\.?\\d*)")) { match ->
            "sqrt(${match.groupValues[1]})"
        }
        result = result.replace(Regex("(\\d+\\.?\\d*)%")) { match ->
            "(${match.groupValues[1]}/100.0)"
        }
        return result
    }

    private fun evaluateSimple(expr: String): Double {
        val parts = tokenize(expr)
        return parseExpression(parts, 0).first
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> {
                    if (current.isNotEmpty()) {
                        tokens.add(current.toString())
                        current.clear()
                    }
                }
                c == '(' || c == ')' || c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == ',' -> {
                    if (current.isNotEmpty()) {
                        tokens.add(current.toString())
                        current.clear()
                    }
                    tokens.add(c.toString())
                }
                else -> current.append(c)
            }
            i++
        }
        if (current.isNotEmpty()) {
            tokens.add(current.toString())
        }
        return tokens
    }

    private fun parseExpression(tokens: List<String>, start: Int): Pair<Double, Int> {
        var (left, pos) = parseTerm(tokens, start)
        while (pos < tokens.size && (tokens[pos] == "+" || tokens[pos] == "-")) {
            val op = tokens[pos]
            val (right, nextPos) = parseTerm(tokens, pos + 1)
            pos = nextPos
            left = if (op == "+") left + right else left - right
        }
        return Pair(left, pos)
    }

    private fun parseTerm(tokens: List<String>, start: Int): Pair<Double, Int> {
        var (left, pos) = parsePower(tokens, start)
        while (pos < tokens.size && (tokens[pos] == "*" || tokens[pos] == "/")) {
            val op = tokens[pos]
            val (right, nextPos) = parsePower(tokens, pos + 1)
            pos = nextPos
            left = if (op == "*") left * right else left / right
        }
        return Pair(left, pos)
    }

    private fun parsePower(tokens: List<String>, start: Int): Pair<Double, Int> {
        var (left, pos) = parseUnary(tokens, start)
        if (pos < tokens.size && tokens[pos] == "^") {
            val (right, nextPos) = parsePower(tokens, pos + 1)
            return Pair(left.pow(right), nextPos)
        }
        return Pair(left, pos)
    }

    private fun parseUnary(tokens: List<String>, start: Int): Pair<Double, Int> {
        if (start >= tokens.size) return Pair(0.0, start)
        if (tokens[start] == "-") {
            val (value, pos) = parseAtom(tokens, start + 1)
            return Pair(-value, pos)
        }
        return parseAtom(tokens, start)
    }

    private fun parseAtom(tokens: List<String>, start: Int): Pair<Double, Int> {
        if (start >= tokens.size) return Pair(0.0, start)

        val token = tokens[start]

        if (token == "(") {
            val (value, pos) = parseExpression(tokens, start + 1)
            if (pos < tokens.size && tokens[pos] == ")") {
                return Pair(value, pos + 1)
            }
            return Pair(value, pos)
        }

        if (token.startsWith("sqrt")) {
            if (start + 1 < tokens.size && tokens[start + 1] == "(") {
                val (value, pos) = parseExpression(tokens, start + 2)
                if (pos < tokens.size && tokens[pos] == ")") {
                    return Pair(sqrt(value), pos + 1)
                }
                return Pair(sqrt(value), pos)
            }
        }

        return Pair(token.toDouble(), start + 1)
    }

    fun factorial(n: Int): Long {
        if (n < 0) return -1
        if (n == 0 || n == 1) return 1
        var result = 1L
        for (i in 2..n) {
            result *= i
        }
        return result
    }
}
