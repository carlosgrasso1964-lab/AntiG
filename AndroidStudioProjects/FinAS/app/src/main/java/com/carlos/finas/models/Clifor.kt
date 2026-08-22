package com.carlos.finas.models

data class Clifor(
    val codCliFor: String,
    val Tipo: String,
    val nomeCliFor: String,
    val apelidoCliFor: String?,
    val email: String?,
    val celular: String?,
    val telefone: String?,
    val cep: String?,
    val endereco: String?,
    val numero: Int,
    val complemento: String?,
    val bairro: String?,
    val cidade: String?,
    val estado: String?,
    val rg: String?,
    val cpf: String?,
    val contatoCliFor: String?,
    val obs: String?,
    val fkCliForGp: String?
)