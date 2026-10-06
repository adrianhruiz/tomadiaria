package com.adrianhruiz.tomadiaria.ui.account

import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.adrianhruiz.tomadiaria.TomaDiariaApp
import com.adrianhruiz.tomadiaria.data.backup.BackupFileStore
import com.adrianhruiz.tomadiaria.data.repository.AccountRepository
import com.adrianhruiz.tomadiaria.data.repository.InvalidPasswordException
import com.adrianhruiz.tomadiaria.data.repository.ScheduleRepository
import com.adrianhruiz.tomadiaria.notifications.DoseAlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeleteAccountUiState(
    val password: String = "",
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val deleted: Boolean = false,
)

/**
 * Borra la cuenta y todo lo que queda de ella: datos en Firestore, cuenta de Authentication,
 * alarmas programadas en el móvil y copias automáticas locales.
 *
 * Lo que toca el sistema Android llega como funciones para poder probarlo sin dispositivo.
 */
class DeleteAccountViewModel(
    private val accountRepository: AccountRepository,
    private val scheduleRepository: ScheduleRepository,
    private val cancelDoseAlarm: (scheduleId: String, doseId: String) -> Unit,
    private val clearLocalData: () -> Unit,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeleteAccountUiState())
    val uiState: StateFlow<DeleteAccountUiState> = _uiState.asStateFlow()

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun deleteAccount() {
        val password = _uiState.value.password
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Escribe tu contraseña para confirmar.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            try {
                // La contraseña se comprueba antes de tocar nada: si fallara a mitad, el usuario
                // se quedaría con los datos borrados y la cuenta viva.
                accountRepository.reauthenticate(password)
                // Las alarmas se apuntan antes de borrar, que después ya no hay de dónde leerlas,
                // pero se cancelan solo si el borrado sale bien.
                val pending = scheduleRepository.getActiveSchedulesWithPendingDoses()
                accountRepository.deleteAccountAndData()
                pending.forEach { (schedule, doses) ->
                    doses.forEach { cancelDoseAlarm(schedule.id, it.id) }
                }
                clearLocalData()
                _uiState.update { it.copy(isDeleting = false, deleted = true) }
            } catch (e: InvalidPasswordException) {
                _uiState.update { it.copy(isDeleting = false, errorMessage = "La contraseña no es correcta.") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = "No se pudo eliminar la cuenta. Comprueba tu conexión e inténtalo de nuevo.",
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TomaDiariaApp
                DeleteAccountViewModel(
                    accountRepository = app.container.accountRepository,
                    scheduleRepository = app.container.scheduleRepository,
                    cancelDoseAlarm = { scheduleId, doseId -> DoseAlarmScheduler.cancelDose(app, scheduleId, doseId) },
                    clearLocalData = {
                        BackupFileStore(app.getExternalFilesDir(null)).deleteAll()
                        NotificationManagerCompat.from(app).cancelAll()
                    },
                )
            }
        }
    }
}
