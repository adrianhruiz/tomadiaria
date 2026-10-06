package com.adrianhruiz.tomadiaria.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MedicamentoDetailDto(
    val nregistro: String,
    val nombre: String,
    val labtitular: String? = null,
    val dosis: String? = null,
    val comerc: Boolean? = null,
    val receta: Boolean? = null,
    val fotos: List<FotoDto>? = null,
    val principiosActivos: List<PrincipioActivoDto>? = null,
)

@Serializable
data class PrincipioActivoDto(
    val id: Int,
    val codigo: String? = null,
    val nombre: String,
    val cantidad: String? = null,
    val unidad: String? = null,
    val orden: Int? = null,
)
