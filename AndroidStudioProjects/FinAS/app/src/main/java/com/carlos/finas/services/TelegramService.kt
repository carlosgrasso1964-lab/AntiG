package com.carlos.finas.services

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Serviço para enviar mensagens via Telegram Bot API.
 */
object TelegramService {

    // Credenciais atuais (verifique se estão 100% corretas)
    private const val BOT_TOKEN = "8992452932:AAEURN-7BWOTf0xcEtE03Zx8jQAJ3rQnGOU"
    private const val CHAT_ID = "978630915"

    private const val TAG = "TelegramService"
    private const val BASE_URL = "https://api.telegram.org/bot"
    private const val SEND_MESSAGE_ENDPOINT = "/sendMessage"

    /**
     * Envia uma mensagem de texto via Telegram.
     * Retorna Pair<Boolean, String?> onde:
     * - first = sucesso
     * - second = mensagem de erro (null se sucesso)
     */
    fun sendMessage(
        context: Context,
        text: String,
        parseMode: String? = "HTML"
    ): Pair<Boolean, String?> {
        if (BOT_TOKEN.isEmpty() || CHAT_ID.isEmpty()) {
            Log.e(TAG, "Configuração ausente: BOT_TOKEN ou CHAT_ID vazios.")
            return false to "BOT_TOKEN ou CHAT_ID vazios"
        }

        val sanitizedText = if (parseMode == "HTML") sanitizeHtml(text) else text
        
        val urlString = "$BASE_URL$BOT_TOKEN$SEND_MESSAGE_ENDPOINT"
        val postData = "chat_id=${encode(CHAT_ID)}&text=${encode(sanitizedText)}${parseMode?.let { "&parse_mode=${encode(it)}" } ?: ""}"

        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 20000
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }

            connection.outputStream.use { it.write(postData.toByteArray(StandardCharsets.UTF_8)) }

            val responseCode = connection.responseCode
            val responseBody = readResponse(connection)

            if (responseCode == 200) {
                Log.d(TAG, "Sucesso: Relatório enviado ao Telegram.")
                true to null
            } else {
                Log.e(TAG, "Falha no Telegram (HTTP $responseCode): $responseBody")
                false to "HTTP $responseCode: $responseBody"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro de conexão: ${e.message}")
            false to "Erro de conexão: ${e.message}"
        }
    }

    private fun sanitizeHtml(text: String): String {
        // O Telegram só aceita tags específicas. Se houver um < solto, ele quebra.
        // Se você não estiver enviando tags propositalmente, o ideal é não usar HTML ou escapar tudo.
        return text // Por enquanto manteremos o texto, mas o log detalhado nos dirá se é o culpado.
    }

    private fun encode(s: String): String = java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20")

    private fun readResponse(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode >= 400) connection.errorStream else connection.inputStream
        return stream?.bufferedReader()?.use { it.readText() } ?: ""
    }
}