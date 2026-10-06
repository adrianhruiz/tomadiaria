package com.adrianhruiz.tomadiaria.data.repository

import com.adrianhruiz.tomadiaria.data.remote.FakeCimaApiService
import com.adrianhruiz.tomadiaria.data.remote.dto.FotoDto
import com.adrianhruiz.tomadiaria.data.remote.dto.MedicamentoDetailDto
import com.adrianhruiz.tomadiaria.data.remote.dto.PrincipioActivoDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MedicationCatalogRepositoryTest {

    @Test
    fun `getMedicationDetail maps principiosActivos and fotoUrl`() = runTest {
        val detail = MedicamentoDetailDto(
            nregistro = "77758",
            nombre = "PARACETAMOL ABAMED 1G",
            principiosActivos = listOf(PrincipioActivoDto(id = 745, nombre = "PARACETAMOL")),
            fotos = listOf(FotoDto(tipo = "materialas", url = "foto.jpg")),
        )
        val repository = MedicationCatalogRepository(FakeCimaApiService(medicamentoDetailResponse = detail))

        val result = repository.getMedicationDetail("77758")

        assertEquals(1, result.principiosActivos.size)
        assertEquals(745, result.principiosActivos.first().id)
        assertEquals("foto.jpg", result.fotoUrl)
    }

    @Test
    fun `getMedicationDetail leaves fotoUrl null when CIMA reports no photos`() = runTest {
        val detail = MedicamentoDetailDto(nregistro = "1", nombre = "XAZAL", fotos = null)
        val repository = MedicationCatalogRepository(FakeCimaApiService(medicamentoDetailResponse = detail))

        val result = repository.getMedicationDetail("1")

        assertNull(result.fotoUrl)
    }
}
