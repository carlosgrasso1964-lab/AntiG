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
     * Agora com tratamento de erro detalhado.
     */
    fun sendMessage(
        context: Context,
        text: String,
        parseMode: String? = "HTML"
    ): Boolean {
        if (BOT_TOKEN.isEmpty() || CHAT_ID.isEmpty()) {
            Log.e(TAG, "Configuração ausente: BOT_TOKEN ou CHAT_ID vazios.")
            return false
        }

        // Limpar e preparar o texto
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
                true
            } else {
                // Aqui o Logcat vai dizer EXATAMENTE o erro (ex: Bad Request: can't parse entities)
                Log.e(TAG, "Falha no Telegram (HTTP $responseCode): $responseBody")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro de conexão: ${e.message}")
            false
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