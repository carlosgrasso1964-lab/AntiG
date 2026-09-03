package com.carlos.indextracker.database

data class Indice(
    val id: Long = 0,
    val nome: String,
    val ativo: String,
    val fonte: String, // "exchangerate" | "coingecko" | "gold" | "yahoo"
    val sufixo: String,
    val cor: String,
    val ativoAtivo: Boolean = true,
    val criadoEm: Long = System.currentTimeMillis()
)

data class Cotacao(
    val id: Long = 0,
    val indiceId: Long,
    val data: String, // yyyy-MM-dd
    val valor: Double,
    val criadoEm: Long = System.currentTimeMillis()
)
