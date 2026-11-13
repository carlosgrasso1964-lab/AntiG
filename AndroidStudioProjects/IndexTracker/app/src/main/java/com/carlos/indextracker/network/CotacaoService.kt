package com.carlos.indextracker.network

import com.carlos.indextracker.database.Indice
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Busca cotações na internet.
 *  - Moedas: AwesomeAPI (USD-BRL, EUR-BRL...) com fallback no exchangerate-api.
 *  - Índices personalizados: URL informada pelo usuário + caminho opcional no JSON.
 */
object CotacaoService {

    suspend fun buscar(indice: Indice): Double? = withContext(Dispatchers.IO) {
        try {
            when (indice.fonte) {
                Indice.FONTE_MOEDA -> buscarMoeda(indice.codigo.uppercase())
                Indice.FONTE_CUSTOM -> buscarUrl(indice.apiUrl, indice.jsonPath)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun buscarMoeda(codigo: String): Double? {
        if (codigo.isBlank()) return null

        // 1) AwesomeAPI: devolve a cotação da moeda em reais.
        val jsonAwesome = httpGet("https://economia.awesomeapi.com.br/last/$codigo-BRL")
        if (jsonAwesome != null) {
            val valor = runCatching {
                val raiz = JsonParser.parseString(jsonAwesome).asJsonObject
                val chave = raiz.keySet().firstOrNull()
                val item = if (chave != null) raiz.getAsJsonObject(chave) else null
                item?.get("bid")?.asDouble
            }.getOrNull()
            if (valor != null && valor > 0) return valor
        }

        // 2) Fallback: exchangerate-api (base USD) -> converte para BRL.
        val jsonRates = httpGet("https://api.exchangerate-api.com/v4/latest/USD") ?: return null
        return runCatching {
            val rates = JsonParser.parseString(jsonRates).asJsonObject.getAsJsonObject("rates")
            val brl = rates.get("BRL").asDouble
            if (codigo == "USD") brl else rates.get(codigo).asDouble * brl
        }.getOrNull()?.takeIf { it > 0 }
    }

    private fun buscarUrl(url: String, jsonPath: String): Double? {
        if (url.isBlank()) return null
        val corpo = httpGet(url) ?: return null
        val raiz: JsonElement = runCatching { JsonParser.parseString(corpo) }.getOrNull() ?: return null

        val valor: Double? = if (jsonPath.isNotBlank()) {
            var atual: JsonElement? = raiz
            for (parte in jsonPath.split('.')) {
                if (atual == null) break
                atual = if (atual.isJsonArray) {
                    val indiceArray = parte.toIntOrNull()
                    if (indiceArray != null) {
                        val array = atual.asJsonArray
                        if (indiceArray in 0 until array.size()) array[indiceArray] else null
                    } else null
                } else if (atual.isJsonObject) {
                    atual.asJsonObject.get(parte)
                } else null
            }
            atual?.takeIf { it.isJsonPrimitive }?.asDouble
        } else {
            primeiroNumero(raiz, 0)
        }

        return valor?.takeIf { it > 0 }
    }

    private fun primeiroNumero(elemento: JsonElement, profundidade: Int): Double? {
        if (profundidade > 4) return null
        if (elemento.isJsonPrimitive) {
            val prim = elemento.asJsonPrimitive
            if (prim.isNumber) return prim.asDouble
            if (prim.isString) return prim.asString.replace(',', '.').toDoubleOrNull()
        }
        if (elemento.isJsonObject) {
            for (entrada in elemento.asJsonObject.entrySet()) {
                val achado = primeiroNumero(entrada.value, profundidade + 1)
                if (achado != null && achado > 0) return achado
            }
        }
        if (elemento.isJsonArray) {
            for (item in elemento.asJsonArray) {
                val achado = primeiroNumero(item, profundidade + 1)
                if (achado != null && achado > 0) return achado
            }
        }
        return null
    }

    private fun httpGet(urlStr: String): String? {
        var conexao: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conexao = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", "IndexTracker/2.0")
                setRequestProperty("Accept", "application/json")
            }
            if (conexao.responseCode != 200) return null
            conexao.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        } finally {
            conexao?.disconnect()
        }
    }
}
