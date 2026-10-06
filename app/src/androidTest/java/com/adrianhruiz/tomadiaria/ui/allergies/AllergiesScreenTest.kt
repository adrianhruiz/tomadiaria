package com.adrianhruiz.tomadiaria.ui.allergies

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adrianhruiz.tomadiaria.data.remote.FakeCimaApiService
import com.adrianhruiz.tomadiaria.data.remote.dto.MaestraItemDto
import com.adrianhruiz.tomadiaria.data.remote.dto.MaestrasResponseDto
import com.adrianhruiz.tomadiaria.data.repository.FakeAllergyRepository
import com.adrianhruiz.tomadiaria.data.repository.MedicationCatalogRepository
import com.adrianhruiz.tomadiaria.model.Allergy
import com.adrianhruiz.tomadiaria.model.AllergyType
import com.adrianhruiz.tomadiaria.ui.theme.TomaDiariaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AllergiesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val familyResults = MaestrasResponseDto(
        totalFilas = 1,
        pagina = 1,
        tamanioPagina = 200,
        resultados = listOf(MaestraItemDto(codigo = "J01C", nombre = "Penicilinas")),
    )

    private fun setContent(allergies: List<Allergy>) {
        val viewModel = AllergiesViewModel(
            FakeAllergyRepository(allergies),
            MedicationCatalogRepository(FakeCimaApiService(maestrasResponse = familyResults)),
        )
        composeRule.setContent {
            TomaDiariaTheme {
                AllergiesScreen(contentPadding = PaddingValues(), viewModel = viewModel)
            }
        }
    }

    @Test
    fun anAllergyAlreadyMarkedIsFlaggedInTheSearchResults() {
        setContent(
            listOf(Allergy(id = "1", tipo = AllergyType.FAMILIA, atcCodigo = "J01C", atcNombre = "Penicilinas")),
        )

        composeRule.onNodeWithText("Familia (ATC)").performClick()
        composeRule.onNodeWithText("Buscar familia (ej. Penicilina)").performTextInput("penicilina")
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Ya añadida").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Ya añadida").assertExists()
    }

    @Test
    fun anAllergyNotMarkedYetOffersTheAddAction() {
        setContent(emptyList())

        composeRule.onNodeWithText("Familia (ATC)").performClick()
        composeRule.onNodeWithText("Buscar familia (ej. Penicilina)").performTextInput("penicilina")
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithContentDescription("Añadir alergia").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Ya añadida").assertDoesNotExist()
    }
}
