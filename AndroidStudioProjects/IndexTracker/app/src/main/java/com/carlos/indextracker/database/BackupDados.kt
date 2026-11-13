package com.carlos.indextracker.database

/**
 * Conteúdo do arquivo de backup em JSON.
 * Guarda os índices e todo o histórico de cotações para transferir entre aparelhos.
 */
data class BackupDados(
    val versao: Int = 1,
    val exportadoEm: Long = System.currentTimeMillis(),
    val indices: List<Indice> = emptyList(),
    val cotacoes: List<Cotacao> = emptyList()
)
