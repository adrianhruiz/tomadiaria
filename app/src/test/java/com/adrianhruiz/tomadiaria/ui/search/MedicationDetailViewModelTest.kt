package com.adrianhruiz.tomadiaria.ui.search

import androidx.lifecycle.SavedStateHandle
import com.adrianhruiz.tomadiaria.data.remote.FakeCimaApiService
import com.adrianhruiz.tomadiaria.data.remote.dto.MedicamentoDetailDto
import com.adrianhruiz.tomadiaria.data.remote.dto.PrincipioActivoDto
import com.adrianhruiz.tomadiaria.data.repository.FakeMedicationHistoryRepository
import com.adrianhruiz.tomadiaria.data.repository.MedicationCatalogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val detailDto = MedicamentoDetailDto(
        nregistro = "77758",
        nombre = "PARACETAMOL ABAMED 1G",
        principiosActivos = listOf(PrincipioActivoDto(id = 745, nombre = "PARACETAMOL")),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        historyRepository: FakeMedicationHistoryRepository = FakeMedicationHistoryRepository(),
    ) = MedicationDetailViewModel(
        MedicationCatalogRepository(FakeCimaApiService(medicamentoDetailResponse = detailDto)),
        historyRepository,
        SavedStateHandle(mapOf("nregistro" to "77758")),
    )

    @Test
    fun `loads the medication detail with its active ingredients`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val medication = viewModel.uiState.value.medication
        assertEquals("PARACETAMOL ABAMED 1G", medication?.nombre)
        assertEquals(listOf("PARACETAMOL"), medication?.principiosActivos?.map { it.nombre })
    }

    @Test
    fun `a medication already in the history is flagged with its reasons`() = runTest(dispatcher) {
        val historyRepository = FakeMedicationHistoryRepository()
        historyRepository.add(
            nregistro = "77758",
            nombreMedicamento = "PARACETAMOL ABAMED 1G",
            reasonId = "r1",
            reasonNombre = "Fiebre",
            dosisSnapshot = null,
            fotoUrl = null,
        )
        val viewModel = createViewModel(historyRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isInHistory)
        assertEquals(listOf("Fiebre"), viewModel.uiState.value.historyReasons)
    }

    @Test
    fun `a medication that is not in the history is not flagged`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isInHistory)
    }

    @Test
    fun `entries of other medications do not flag this one`() = runTest(dispatcher) {
        val historyRepository = FakeMedicationHistoryRepository()
        historyRepository.add(
            nregistro = "99999",
            nombreMedicamento = "IBUPROFENO",
            reasonId = "r1",
            reasonNombre = "Fiebre",
            dosisSnapshot = null,
            fotoUrl = null,
        )
        val viewModel = createViewModel(historyRepository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isInHistory)
    }
}
