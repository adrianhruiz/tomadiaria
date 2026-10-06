package com.adrianhruiz.tomadiaria.ui.account

import com.adrianhruiz.tomadiaria.data.repository.FakeAccountRepository
import com.adrianhruiz.tomadiaria.data.repository.FakeScheduleRepository
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
class DeleteAccountViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val accountRepository = FakeAccountRepository(correctPassword = "secret1")
    private val scheduleRepository = FakeScheduleRepository()
    private val cancelledAlarms = mutableListOf<Pair<String, String>>()
    private var localDataCleared = false

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DeleteAccountViewModel(
        accountRepository = accountRepository,
        scheduleRepository = scheduleRepository,
        cancelDoseAlarm = { scheduleId, doseId -> cancelledAlarms.add(scheduleId to doseId) },
        clearLocalData = { localDataCleared = true },
    )

    private suspend fun createScheduleWithThreeDoses() = scheduleRepository.createSchedule(
        nregistro = "77758",
        nombreMedicamento = "PARACETAMOL",
        fotoUrl = null,
        intervalHours = 8,
        totalDoses = 3,
        startAtMillis = 0L,
        reasonId = "r1",
        reasonNombre = "Pauta",
    )

    @Test
    fun `deletes the account, cancels every pending alarm and clears local data`() = runTest(dispatcher) {
        val created = createScheduleWithThreeDoses()
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("secret1")

        viewModel.deleteAccount()
        advanceUntilIdle()

        assertTrue(accountRepository.deleted)
        assertEquals(created.doses.map { created.schedule.id to it.id }, cancelledAlarms)
        assertTrue(localDataCleared)
        assertTrue(viewModel.uiState.value.deleted)
    }

    @Test
    fun `a wrong password deletes nothing`() = runTest(dispatcher) {
        createScheduleWithThreeDoses()
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("wrong")

        viewModel.deleteAccount()
        advanceUntilIdle()

        assertFalse(accountRepository.deleted)
        assertTrue(cancelledAlarms.isEmpty())
        assertFalse(localDataCleared)
        assertEquals("La contraseña no es correcta.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `alarms and local data are kept if the deletion fails`() = runTest(dispatcher) {
        createScheduleWithThreeDoses()
        accountRepository.shouldFailDeletion = true
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("secret1")

        viewModel.deleteAccount()
        advanceUntilIdle()

        assertTrue(cancelledAlarms.isEmpty())
        assertFalse(localDataCleared)
        assertFalse(viewModel.uiState.value.deleted)
        assertEquals(
            "No se pudo eliminar la cuenta. Comprueba tu conexión e inténtalo de nuevo.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun `asks for the password before trying anything`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.deleteAccount()
        advanceUntilIdle()

        assertFalse(accountRepository.deleted)
        assertEquals("Escribe tu contraseña para confirmar.", viewModel.uiState.value.errorMessage)
    }
}
