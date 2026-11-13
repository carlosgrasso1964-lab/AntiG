package com.carlos.indextracker.database

/**
 * Um índice acompanhado pelo app.
 *
 * fonte:
 *  - [FONTE_MANUAL]: o valor é digitado pelo usuário no card.
 *  - [FONTE_MOEDA]: cotação automática de uma moeda (ex: USD, EUR) em BRL.
 *  - [FONTE_CUSTOM]: cotação automática a partir de uma URL que devolve JSON.
 */
data class Indice(
    val id: Long = 0,
    val nome: String,
    val fonte: String = FONTE_MANUAL,
    val codigo: String = "",
    val apiUrl: String = "",
    val jsonPath: String = "",
    val sufixo: String = " R$",
    val cor: String = "#1f4f8a",
    val casasDecimais: Int = 2,
    val valorPadrao: Double = 0.0,
    val ativo: Boolean = true,
    val ordem: Int = 0,
    val criadoEm: Long = System.currentTimeMillis()
) {
    val manual: Boolean get() = fonte == FONTE_MANUAL

    companion object {
        const val FONTE_MANUAL = "manual"
        const val FONTE_MOEDA = "moeda"
        const val FONTE_CUSTOM = "custom"

        fun dolar() = Indice(
            id = 1,
            nome = "Dólar",
            fonte = FONTE_MOEDA,
            codigo = "USD",
            sufixo = " R$",
            cor = "#1f4f8a",
            casasDecimais = 3,
            valorPadrao = 5.70,
            ordem = 0
        )
    }
}

data class Cotacao(
    val id: Long = 0,
    val indiceId: Long,
    val data: String, // yyyy-MM-dd
    val valor: Double,
    val criadoEm: Long = System.currentTimeMillis()
)
