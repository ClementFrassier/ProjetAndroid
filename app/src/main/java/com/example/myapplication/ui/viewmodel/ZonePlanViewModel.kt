package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Result
import com.example.myapplication.data.ZonePlanRepository
import com.example.myapplication.model.ZonePlan
import com.example.myapplication.model.ZonePlanInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ZonePlanUiState(
    val isLoading: Boolean = false,
    val zones: List<ZonePlan> = emptyList(),
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

class ZonePlanViewModel(private val repository: ZonePlanRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ZonePlanUiState())
    val uiState: StateFlow<ZonePlanUiState> = _uiState

    fun loadByFestival(festivalId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getZonePlansByFestival(festivalId)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    zones = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun create(input: ZonePlanInput, festivalId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            when (val result = repository.createZonePlan(input)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    loadByFestival(festivalId)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun delete(id: Int, festivalId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            when (val result = repository.deleteZonePlan(id)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    loadByFestival(festivalId)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(private val repository: ZonePlanRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ZonePlanViewModel(repository) as T
        }
    }
}
