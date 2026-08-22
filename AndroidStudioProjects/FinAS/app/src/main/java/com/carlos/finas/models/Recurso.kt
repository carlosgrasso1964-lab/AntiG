package com.carlos.finas.models

data class Recurso(
    val codigo: String,
    val nomebco: String?,
    val agencia: String?,
    val fluxo: String?,
    val limite: Double?,
    val abertura: String?,
    val encerramento: String?,
    val status: String?,
    val fk_gpprinc: String?
)