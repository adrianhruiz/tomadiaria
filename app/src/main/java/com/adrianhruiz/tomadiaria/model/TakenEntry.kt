package com.adrianhruiz.tomadiaria.model

data class TakenEntry(
    val id: String,
    val nregistro: String,
    val nombreMedicamento: String,
    val reasonId: String,
    val reasonNombre: String,
    val timestampMillis: Long,
    val dosisSnapshot: String?,
    val fotoUrl: String?,
)
