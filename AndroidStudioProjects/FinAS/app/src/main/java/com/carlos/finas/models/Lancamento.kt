package com.carlos.finas.models

data class Lancamento(
    val idMov: Int,
    val recurso: String,
    val vrecurso: String,
    val clifor: String,
    val vCliFor: String,
    val dtLancto: String,
    val dtEmi: String,
    val dtVcto: String,
    val documento: String?,
    val classif: String,
    val Descr: String,
    val Valor: Double,
    val dtApr: String?,
    val statusMov: String,
    val Prev: String,
    var saldoAcumulado: Double? = null
)