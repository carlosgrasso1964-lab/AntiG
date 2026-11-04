package com.carlos.finas.services

import android.content.Context
import android.util.Log
import com.carlos.finas.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Serviço para enviar mensagens via Telegram Bot API.
 */
object TelegramService {

    private const val TAG = "TelegramService"
    private const val BASE_URL = "https://api.telegram.org/bot"
    private const val SEND_MESSAGE_ENDPOINT = "/sendMessage"
    private const val MAX_TEXT_LENGTH = 4096

    private val botToken: String
        get() = BuildConfig.TELEGRAM_BOT_TOKEN.trim()
    private val chatId: String
        get() = BuildConfig.TELEGRAM_CHAT_ID.trim()

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
        if (botToken.isEmpty() || chatId.isEmpty()) {
            Log.e(TAG, "Configuração ausente: TELEGRAM_BOT_TOKEN ou TELEGRAM_CHAT_ID vazios.")
            return false to "TELEGRAM_BOT_TOKEN ou TELEGRAM_CHAT_ID vazios"
        }

        val corpo = text.trim()
        val tamanho = corpo.toByteArray(StandardCharsets.UTF_8).size
        Log.d(TAG, "Enviando mensagem: ${corpo.length} caracteres, $tamanho bytes, parse_mode=$parseMode")

        var resultado = post(corpo, parseMode)

        // O Telegram recusa HTML malformado (ex.: tag não fechada, "&" solto) com
        // 400 "can't parse entities". Nesse caso reenviamos como texto puro para
        // não perder o relatório.
        if (!resultado.first && parseMode != null && resultado.third?.contains("entit", ignoreCase = true) == true) {
            Log.w(TAG, "HTML rejeitado pelo Telegram, reenviando como texto puro.")
            resultado = post(corpo, null)
        }

        return if (resultado.first) {
            Log.d(TAG, "Sucesso: Relatório enviado ao Telegram.")
            true to null
        } else {
            val erro = "HTTP ${resultado.second}: ${resultado.third}"
            Log.e(TAG, erro)
            false to traduzirErro(resultado.second, resultado.third)
        }
    }

    /**
     * Executa o POST em /sendMessage.
     * Retorna Triple<sucesso, httpCode, descricaoDoErro>.
     */
    private fun post(text: String, parseMode: String?): Triple<Boolean, Int, String?> {
        if (text.toByteArray(StandardCharsets.UTF_8).size > MAX_TEXT_LENGTH) {
            // Limite da Bot API: 4096 bytes por mensagem.
            return Triple(false, 400, "message is too long (limite de $MAX_TEXT_LENGTH bytes)")
        }

        val urlString = "$BASE_URL$botToken$SEND_MESSAGE_ENDPOINT"
        val postData = buildString {
            append("chat_id=${encode(chatId)}")
            append("&text=${encode(text)}")
            if (parseMode != null) append("&parse_mode=${encode(parseMode)}")
            append("&disable_web_page_preview=true")
        }

        return try {
            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 20000
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            }

            connection.outputStream.use { it.write(postData.toByteArray(StandardCharsets.UTF_8)) }

            val responseCode = connection.responseCode
            val responseBody = readResponse(connection)
            connection.disconnect()

            if (responseCode == 200) {
                Triple(true, 200, null)
            } else {
                Triple(false, responseCode, extrairDescricao(responseBody))
            }
        } catch (e: Exception) {
            Triple(false, -1, "Erro de conexão: ${e.message}")
        }
    }

    private fun encode(s: String): String = java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20")

    private fun readResponse(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode >= 400) connection.errorStream else connection.inputStream
        return stream?.bufferedReader()?.use { it.readText() } ?: ""
    }

    /** Extrai o campo "description" do JSON de resposta do Telegram. */
    private fun extrairDescricao(body: String): String {
        if (body.isBlank()) return "resposta vazia do Telegram"
        return try {
            JSONObject(body).optString("description").ifEmpty { body }
        } catch (e: Exception) {
            body
        }
    }

    /** Converte a mensagem técnica do Telegram em algo acionável. */
    private fun traduzirErro(httpCode: Int, descricao: String?): String {
        val desc = descricao.orEmpty()
        return when {
            desc.contains("chat not found", ignoreCase = true) ->
                "chat not found — o TELEGRAM_CHAT_ID ($chatId) não tem conversa com o bot. " +
                    "Abra o bot no Telegram, envie /start e confirme o ID em getUpdates."

            desc.contains("bot was blocked", ignoreCase = true) ->
                "o bot foi bloqueado pelo usuário. Desbloqueie o bot no Telegram."

            desc.contains("Unauthorized", ignoreCase = true) ->
                "token do bot inválido. Revise TELEGRAM_BOT_TOKEN no local.properties."

            desc.contains("entit", ignoreCase = true) ->
                "HTML inválido no relatório (mesmo em texto puro). Verifique caracteres como < > &."

            desc.contains("too long", ignoreCase = true) ->
                "relatório acima do limite de $MAX_TEXT_LENGTH bytes do Telegram."

            else -> if (httpCode > 0) "HTTP $httpCode: $desc" else desc
        }
    }
}
