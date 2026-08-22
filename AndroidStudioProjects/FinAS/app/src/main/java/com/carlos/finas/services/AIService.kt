package com.carlos.finas.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object AIPlatformDetector {
    private const val TAG = "AIPlatformDetector"
    private const val PREF_NAME = "OllamaPreferences"
    private const val KEY_ENDPOINT = "configured_endpoint"

    enum class Platform {
        OLLAMA_PC,
        MLKIT_MOBILE,
        EDGE_GALLERY,
        NONE
    }

    data class AIConfig(
        val platform: Platform,
        val modelName: String,
        val endpoint: String? = null,
        val isAvailable: Boolean = false,
        val alternatives: List<String> = emptyList(),
        val useLocalhost: Boolean = true  // false = usar IP da rede
    )

    val SUGGESTED_MODELS = listOf(
        "gemma2:2b" to "Google Gemma 2 (1.6 GB) - Recomendado PC",
        "qwen2.5:1.5b" to "Qwen 2.5 1.5B (1.0 GB) - Mais leve",
        "llama3.2:1b" to "Llama 3.2 1B (1.3 GB) - Alternativa",
        "phi3.5:3.8b" to "Phi-3.5 Mini (2.2 GB) - Melhor contexto",
        "gemma3n:e2b" to "Gemma 3n E2B (1.6 GB) - Mobile otimizado",
        "novaforgeai/gemma2:2b-optimized" to "Gemma2 Otimizado (1.6 GB) - Ultra-rápido"
    )
    
    fun detectPlatform(context: Context): AIConfig {
        val isAndroid = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M

        return if (isAndroid) {
            detectMobilePlatform(context)
        } else {
            detectDesktopPlatform()
        }
    }

    /** Salva o endpoint Ollama configurado pelo usuário */
    fun saveOllamaEndpoint(context: Context, endpoint: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ENDPOINT, endpoint)
            .apply()
    }

    /** Recupera o endpoint Ollama configurado pelo usuário, se existir */
    fun getConfiguredOllamaEndpoint(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ENDPOINT, null)
    }

    /** Verifica se um endpoint Ollama está disponível */
    private fun isOllamaEndpointAvailable(endpoint: String): Boolean {
        return try {
            val url = java.net.URL("$endpoint/api/tags")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            response.contains("models") || response.contains("\"name\"")
        } catch (e: Exception) {
            Log.d(TAG, "Endpoint Ollama não disponível: $endpoint - ${e.message}")
            false
        }
    }
    
    private fun detectMobilePlatform(context: Context): AIConfig {
        // Primeiro, verificar se há um endpoint configurado pelo usuário
        val configuredEndpoint = getConfiguredOllamaEndpoint(context)
        if (configuredEndpoint != null && isOllamaEndpointAvailable(configuredEndpoint)) {
            Log.d(TAG, "Usando endpoint Ollama configurado: $configuredEndpoint")
            val model = detectBestModelAt(configuredEndpoint)
            return AIConfig(
                platform = Platform.OLLAMA_PC,
                modelName = model,
                endpoint = configuredEndpoint,
                isAvailable = true,
                alternatives = SUGGESTED_MODELS.map { it.first },
                useLocalhost = false
            )
        }

        // Tentar diferentes endereços para Ollama (fallback para detecção automática)
        val endpoints = listOf(
            "http://10.0.2.2:11434",      // Emulador Android
            "http://127.0.0.1:11434",     // Localhost físico
            "http://localhost:11434"       // Localhost alternativo
        )

        for (endpoint in endpoints) {
            if (isOllamaRunningAt(endpoint)) {
                Log.d(TAG, "Ollama detectado em: $endpoint")
                val model = detectBestModelAt(endpoint)
                return AIConfig(
                    platform = Platform.OLLAMA_PC,
                    modelName = model,
                    endpoint = endpoint,
                    isAvailable = true,
                    alternatives = SUGGESTED_MODELS.map { it.first },
                    useLocalhost = false
                )
            }
        }

        return AIConfig(
            platform = Platform.NONE,
            modelName = "",
            isAvailable = false,
            alternatives = SUGGESTED_MODELS.map { it.first },
            useLocalhost = true
        )
    }
    
    private fun detectDesktopPlatform(): AIConfig {
        if (isOllamaRunningAt("http://127.0.0.1:11434")) {
            val model = detectBestModelAt("http://127.0.0.1:11434")
            return AIConfig(
                platform = Platform.OLLAMA_PC,
                modelName = model,
                endpoint = "http://127.0.0.1:11434",
                isAvailable = true,
                alternatives = SUGGESTED_MODELS.map { it.first },
                useLocalhost = true
            )
        }
        
        return AIConfig(
            platform = Platform.NONE,
            modelName = "",
            isAvailable = false,
            alternatives = SUGGESTED_MODELS.map { it.first },
            useLocalhost = true
        )
    }
    
    private fun detectBestModel(): String {
        return detectBestModelAt("http://127.0.0.1:11434")
    }
    
    private fun detectBestModelAt(baseUrl: String): String {
        return try {
            val url = java.net.URL("$baseUrl/api/tags")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"
            
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            
            val availableModels = listOf(
                "gemma3n:e4b", "gemma3n:e2b",
                "gemma4", "gemma2:2b", "gemma2",
                "qwen2.5:3b", "qwen2.5:1.5b",
                "llama3.2:3b", "llama3.2:1b",
                "phi4:14b", "phi3.5:3.8b",
                "novaforgeai/gemma2:2b-optimized"
            )
            
            for (model in availableModels) {
                if (response.contains("\"name\":\"$model\"")) {
                    Log.d(TAG, "Modelo disponível encontrado: $model")
                    return model
                }
            }
            
            "gemma2:2b"
        } catch (e: Exception) {
            Log.d(TAG, "Erro ao detectar modelo: ${e.message}")
            "gemma2:2b"
        }
    }
    
    private fun isOllamaRunning(): Boolean {
        return isOllamaRunningAt("http://127.0.0.1:11434")
    }
    
    private fun isOllamaRunningAt(baseUrl: String): Boolean {
        return try {
            val url = java.net.URL("$baseUrl/api/tags")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "GET"
            
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            
            response.contains("models") || response.contains("\"name\"")
        } catch (e: Exception) {
            Log.d(TAG, "Ollama não está rodando em $baseUrl: ${e.message}")
            false
        }
    }
}

abstract class AIService {
    abstract val platform: AIPlatformDetector.Platform
    abstract val modelName: String
    
    suspend fun generateText(
        prompt: String,
        onProgress: ((String) -> Unit)? = null,
        timeoutMs: Long = 120000
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            doGenerate(prompt, onProgress, timeoutMs)
        } catch (e: Exception) {
            Log.e("AIService", "Erro na geração: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    protected abstract suspend fun doGenerate(
        prompt: String,
        onProgress: ((String) -> Unit)?,
        timeoutMs: Long
    ): Result<String>
    
    open fun isAvailable(): Boolean = true
    
    companion object {
        fun create(context: Context): AIService {
            val config = AIPlatformDetector.detectPlatform(context)
            
            return when (config.platform) {
                AIPlatformDetector.Platform.OLLAMA_PC -> OllamaAIService(
                    config.endpoint ?: "http://127.0.0.1:11434", 
                    config.modelName
                )
                AIPlatformDetector.Platform.MLKIT_MOBILE -> MLKitAIService(config.modelName, context)
                else -> OllamaAIService("http://127.0.0.1:11434", "gemma2:2b")
            }
        }
    }
}

class OllamaAIService(
    private val baseUrl: String,
    override val modelName: String = "gemma2:2b"
) : AIService() {
    
    override val platform = AIPlatformDetector.Platform.OLLAMA_PC
    
    override suspend fun doGenerate(
        prompt: String,
        onProgress: ((String) -> Unit)?,
        timeoutMs: Long
    ): Result<String> {
        return try {
            val url = java.net.URL("$baseUrl/api/generate")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = timeoutMs.toInt()
            
            val escapedPrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
            
            val jsonInput = """
                {
                    "model": "$modelName",
                    "prompt": "$escapedPrompt",
                    "stream": false
                }
            """.trimIndent()
            
            conn.outputStream.use { os ->
                os.write(jsonInput.toByteArray(Charsets.UTF_8))
            }
            
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            
            Log.d("OllamaAIService", "Resposta recebida: ${response.length} chars")
            
            val finalAnswer = if (response.contains("\"response\":")) {
                response.split("\"response\":\"")[1].split("\",\"done\"")[0]
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\t", "\t")
            } else {
                response
            }
            
            Result.success(finalAnswer)
            
        } catch (e: Exception) {
            Log.e("OllamaAIService", "Erro: ${e.message}", e)
            Result.failure(e)
        }
    }
}

class MLKitAIService(
    override val modelName: String,
    private val context: Context
) : AIService() {
    
    override val platform = AIPlatformDetector.Platform.MLKIT_MOBILE
    
    override fun isAvailable(): Boolean {
        val modelDir = File(context.filesDir, "models/gemma-4-2b-it")
        return modelDir.exists()
    }
    
    override suspend fun doGenerate(
        prompt: String,
        onProgress: ((String) -> Unit)?,
        timeoutMs: Long
    ): Result<String> {
        return Result.failure(Exception(
            "ML Kit não está configurado. " +
            "Para usar no celular, instale o Ollama no Termux e execute 'ollama serve'"
        ))
    }
}