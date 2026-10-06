package com.adrianhruiz.tomadiaria.ui.search

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adrianhruiz.tomadiaria.data.remote.FakeCimaApiService
import com.adrianhruiz.tomadiaria.data.remote.dto.MedicamentoDetailDto
import com.adrianhruiz.tomadiaria.data.remote.dto.PrincipioActivoDto
import com.adrianhruiz.tomadiaria.data.repository.FakeMedicationHistoryRepository
import com.adrianhruiz.tomadiaria.data.repository.MedicationCatalogRepository
import com.adrianhruiz.tomadiaria.model.TakenEntry
import com.adrianhruiz.tomadiaria.ui.theme.TomaDiariaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicationDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val detailDto = MedicamentoDetailDto(
        nregistro = "77758",
        nombre = "PARACETAMOL ABAMED 1G",
        principiosActivos = listOf(PrincipioActivoDto(id = 745, nombre = "PARACETAMOL")),
    )

    private fun setContent(history: FakeMedicationHistoryRepository = FakeMedicationHistoryRepository()) {
        val viewModel = MedicationDetailViewModel(
            MedicationCatalogRepository(FakeCimaApiService(medicamentoDetailResponse = detailDto)),
            history,
            SavedStateHandle(mapOf("nregistro" to "77758")),
        )
        composeRule.setContent {
            TomaDiariaTheme {
                MedicationDetailScreen(
                    onAddToHistory = {},
                    onCreateReminder = {},
                    onBack = {},
                    viewModel = viewModel,
                )
            }
        }
    }

    @Test
    fun addToHistoryIsAlwaysEnabled() {
        setContent()

        composeRule.onNodeWithText("Añadir al historial").assertIsEnabled()
    }

    @Test
    fun showsTheActiveIngredientsToIdentifyTheBox() {
        setContent()

        composeRule.onNodeWithText("Componentes activos").assertExists()
        composeRule.onNodeWithText("PARACETAMOL").assertExists()
    }

    @Test
    fun aMedicationAlreadyInTheHistoryShowsTheAddedBadge() {
        val history = FakeMedicationHistoryRepository(
            listOf(
                TakenEntry(
                    id = "entry-1",
                    nregistro = "77758",
                    nombreMedicamento = "PARACETAMOL ABAMED 1G",
                    reasonId = "r1",
                    reasonNombre = "Fiebre",
                    timestampMillis = 0L,
                    dosisSnapshot = null,
                    fotoUrl = null,
                ),
            ),
        )
        setContent(history)

        composeRule.onNodeWithText("Ya en tu historial").assertExists()
        composeRule.onNodeWithText("Motivos registrados: Fiebre").assertExists()
    }

    @Test
    fun aMedicationOutsideTheHistoryDoesNotShowTheAddedBadge() {
        setContent()

        composeRule.onNodeWithText("Ya en tu historial").assertDoesNotExist()
    }
}
