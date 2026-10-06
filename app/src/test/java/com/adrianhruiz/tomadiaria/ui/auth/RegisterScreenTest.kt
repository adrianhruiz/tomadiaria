package com.adrianhruiz.tomadiaria.ui.auth

import android.app.Application
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import com.adrianhruiz.tomadiaria.data.repository.FakeAuthRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// La Application real inicializa Firebase, que no arranca fuera del dispositivo.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class RegisterScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val createAccountButton = hasText("Crear cuenta") and hasClickAction()

    private fun setContent() {
        composeRule.setContent {
            RegisterScreen(
                onRegistered = {},
                onNavigateToLogin = {},
                viewModel = AuthViewModel(FakeAuthRepository(initialUserId = null)),
            )
        }
    }

    @Test
    fun createAccountStaysDisabledUntilTheConsentIsGiven() {
        setContent()

        composeRule.onNode(createAccountButton).assertIsNotEnabled()

        composeRule.onNode(isToggleable()).performClick()

        composeRule.onNode(createAccountButton).assertIsEnabled()
    }
}
