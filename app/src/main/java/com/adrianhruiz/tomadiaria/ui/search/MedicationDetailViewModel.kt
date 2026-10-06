package com.adrianhruiz.tomadiaria.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.adrianhruiz.tomadiaria.TomaDiariaApp
import com.adrianhruiz.tomadiaria.data.repository.MedicationCatalogRepository
import com.adrianhruiz.tomadiaria.data.repository.MedicationHistoryRepository
import com.adrianhruiz.tomadiaria.model.MedicationDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MedicationDetailUiState(
    val isLoading: Boolean = true,
    val medication: MedicationDetail? = null,
    val errorMessage: String? = null,
    /** Motivos con los que este medicamento ya está en el historial. */
    val historyReasons: List<String> = emptyList(),
) {
    val isInHistory: Boolean get() = historyReasons.isNotEmpty()
}

class MedicationDetailViewModel(
    private val catalogRepository: MedicationCatalogRepository,
    historyRepository: MedicationHistoryRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val nregistro: String = requireNotNull(savedStateHandle["nregistro"])

    private val _uiState = MutableStateFlow(MedicationDetailUiState())
    val uiState: StateFlow<MedicationDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val detail = catalogRepository.getMedicationDetail(nregistro)
                _uiState.update { it.copy(medication = detail, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "No se pudo cargar el medicamento.")
                }
            }
        }
        historyRepository.getByMedication(nregistro)
            .onEach { entries ->
                _uiState.update { state -> state.copy(historyReasons = entries.map { it.reasonNombre }) }
            }
            .launchIn(viewModelScope)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TomaDiariaApp
                MedicationDetailViewModel(
                    app.container.medicationCatalogRepository,
                    app.container.medicationHistoryRepository,
                    createSavedStateHandle(),
                )
            }
        }
    }
}
