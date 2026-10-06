package com.adrianhruiz.tomadiaria.ui.auth

import com.adrianhruiz.tomadiaria.data.repository.FakeAuthRepository
import com.adrianhruiz.tomadiaria.legal.PrivacyPolicy
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signIn requires both email and password`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        var succeeded = false

        viewModel.signIn { succeeded = true }

        assertFalse(succeeded)
        assertEquals("Introduce email y contraseña.", viewModel.uiState.value.errorMessage)
        assertTrue(repository.signInCalls.isEmpty())
    }

    @Test
    fun `signIn calls the repository and reports success`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.onEmailChanged("user@test.com")
        viewModel.onPasswordChanged("secret1")
        var succeeded = false

        viewModel.signIn { succeeded = true }
        advanceUntilIdle()

        assertTrue(succeeded)
        assertEquals(listOf("user@test.com" to "secret1"), repository.signInCalls)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `signIn surfaces a friendly error when the repository throws`() = runTest(dispatcher) {
        val repository = FakeAuthRepository().apply { shouldFailAuth = true }
        val viewModel = AuthViewModel(repository)
        viewModel.onEmailChanged("user@test.com")
        viewModel.onPasswordChanged("secret1")
        var succeeded = false

        viewModel.signIn { succeeded = true }
        advanceUntilIdle()

        assertFalse(succeeded)
        assertEquals("No se pudo iniciar sesión. Comprueba tus datos.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `signUp requires a password with at least 6 characters`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.onEmailChanged("user@test.com")
        viewModel.onPasswordChanged("123")

        viewModel.signUp {}

        assertEquals("La contraseña debe tener al menos 6 caracteres.", viewModel.uiState.value.errorMessage)
        assertTrue(repository.signUpCalls.isEmpty())
    }

    @Test
    fun `signUp calls the repository and reports success`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.onEmailChanged("new@test.com")
        viewModel.onPasswordChanged("secret1")
        viewModel.onPrivacyPolicyAcceptedChanged(true)
        var succeeded = false

        viewModel.signUp { succeeded = true }
        advanceUntilIdle()

        assertTrue(succeeded)
        assertEquals(listOf("new@test.com" to "secret1"), repository.signUpCalls)
    }

    @Test
    fun `signUp is refused until the privacy policy is accepted`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.onEmailChanged("new@test.com")
        viewModel.onPasswordChanged("secret1")

        viewModel.signUp {}
        advanceUntilIdle()

        assertEquals(
            "Para crear la cuenta tienes que aceptar la política de privacidad.",
            viewModel.uiState.value.errorMessage,
        )
        assertTrue(repository.signUpCalls.isEmpty())
    }

    @Test
    fun `signUp records the version of the privacy policy that was accepted`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.onEmailChanged("new@test.com")
        viewModel.onPasswordChanged("secret1")
        viewModel.onPrivacyPolicyAcceptedChanged(true)

        viewModel.signUp {}
        advanceUntilIdle()

        assertEquals(listOf(PrivacyPolicy.VERSION), repository.signUpPolicyVersions)
    }
}
