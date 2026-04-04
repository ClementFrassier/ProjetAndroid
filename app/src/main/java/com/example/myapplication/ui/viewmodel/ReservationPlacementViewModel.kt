package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ReservationPlacementRepository
import com.example.myapplication.data.Result
import com.example.myapplication.model.ReservationGamePlacement
import com.example.myapplication.model.ReservationGamePlacementCreateInput
import com.example.myapplication.model.ReservationGamePlacementUpdateInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReservationPlacementUiState(
    val isLoading: Boolean = false,
    val placements: List<ReservationGamePlacement> = emptyList(),
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

class ReservationPlacementViewModel(private val repository: ReservationPlacementRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationPlacementUiState())
    val uiState: StateFlow<ReservationPlacementUiState> = _uiState

    fun loadByReservation(reservationId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getPlacementsByReservation(reservationId)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    placements = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun create(input: ReservationGamePlacementCreateInput) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            when (val result = repository.createPlacement(input)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    loadByReservation(input.reservationId)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun update(id: Int, input: ReservationGamePlacementUpdateInput, reservationId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            when (val result = repository.updatePlacement(id, input)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    loadByReservation(reservationId)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun delete(id: Int, reservationId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            when (val result = repository.deletePlacement(id)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    loadByReservation(reservationId)
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

    class Factory(private val repository: ReservationPlacementRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ReservationPlacementViewModel(repository) as T
        }
    }
}
