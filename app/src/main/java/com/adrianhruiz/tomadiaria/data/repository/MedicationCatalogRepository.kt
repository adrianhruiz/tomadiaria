package com.adrianhruiz.tomadiaria.data.repository

import com.adrianhruiz.tomadiaria.data.remote.CimaApiService
import com.adrianhruiz.tomadiaria.model.MedicationDetail
import com.adrianhruiz.tomadiaria.model.PrincipioActivo

class MedicationCatalogRepository(
    private val apiService: CimaApiService,
) {
    suspend fun getMedicationDetail(nregistro: String): MedicationDetail {
        val dto = apiService.obtenerMedicamento(nregistro)
        return MedicationDetail(
            nregistro = dto.nregistro,
            nombre = dto.nombre,
            labtitular = dto.labtitular,
            dosis = dto.dosis,
            comercializado = dto.comerc,
            requiereReceta = dto.receta,
            fotoUrl = dto.fotos
                ?.firstOrNull { it.tipo == "materialas" }
                ?.url
                ?: dto.fotos?.firstOrNull()?.url,
            principiosActivos = dto.principiosActivos.orEmpty().map {
                PrincipioActivo(id = it.id, nombre = it.nombre, cantidad = it.cantidad, unidad = it.unidad)
            },
        )
    }
}
