package com.adrianhruiz.tomadiaria.data.remote

import com.adrianhruiz.tomadiaria.data.remote.dto.MedicamentoDetailDto
import com.adrianhruiz.tomadiaria.data.remote.dto.MedicamentosResponseDto
import okhttp3.Headers
import retrofit2.Response

class FakeCimaApiService(
    private val medicamentosResponse: MedicamentosResponseDto? = null,
    private val medicamentoDetailResponse: MedicamentoDetailDto? = null,
    /** Simula que la búsqueda se ha servido desde la caché de disco. */
    private val medicamentosFromCache: Boolean = false,
) : CimaApiService {

    var lastBuscarMedicamentosNombre: String? = null
        private set

    override suspend fun buscarMedicamentos(nombre: String, pagina: Int): Response<MedicamentosResponseDto> {
        lastBuscarMedicamentosNombre = nombre
        val body = medicamentosResponse ?: error("No medicamentosResponse configured on FakeCimaApiService")
        val headers = if (medicamentosFromCache) {
            Headers.headersOf(HEADER_SERVED_FROM_CACHE, "true")
        } else {
            Headers.headersOf()
        }
        return Response.success(body, headers)
    }

    override suspend fun obtenerMedicamento(nregistro: String): MedicamentoDetailDto {
        return medicamentoDetailResponse ?: error("No medicamentoDetailResponse configured on FakeCimaApiService")
    }
}
