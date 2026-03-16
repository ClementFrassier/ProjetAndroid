package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ReservationRepository
import com.example.myapplication.data.Result
import com.example.myapplication.model.Reservation
import com.example.myapplication.model.ReservationCreateInput
import com.example.myapplication.model.ReservationUpdateInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReservationUiState(
    val isLoading: Boolean = false,
    val reservations: List<Reservation> = emptyList(),
    val errorMessage: String? = null
)

data class ReservationDetailUiState(
    val isLoading: Boolean = false,
    val reservation: Reservation? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class ReservationViewModel(private val repository: ReservationRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState

    private val _detailState = MutableStateFlow(ReservationDetailUiState())
    val detailState: StateFlow<ReservationDetailUiState> = _detailState

    fun loadReservationsByFestival(festivalId: Int) {
        viewModelScope.launch {
            _uiState.value = ReservationUiState(isLoading = true)
            when (val result = repository.getReservationsByFestival(festivalId)) {
                is Result.Success -> _uiState.value = ReservationUiState(reservations = result.data)
                is Result.Error -> _uiState.value = ReservationUiState(errorMessage = result.message)
            }
        }
    }

    fun loadReservation(id: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isLoading = true, errorMessage = null, saveSuccess = false)
            when (val result = repository.getReservation(id)) {
                is Result.Success -> _detailState.value = _detailState.value.copy(
                    isLoading = false,
                    reservation = result.data
                )
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun createReservation(input: ReservationCreateInput, festivalId: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            when (val result = repository.createReservation(input)) {
                is Result.Success -> {
                    _detailState.value = _detailState.value.copy(isSaving = false, saveSuccess = true, reservation = result.data)
                    loadReservationsByFestival(festivalId)
                }
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isSaving = false, 
                    errorMessage = result.message
                )
            }
        }
    }

    fun updateReservation(id: Int, input: ReservationUpdateInput, festivalId: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            when (val result = repository.updateReservation(id, input)) {
                is Result.Success -> {
                    _detailState.value = _detailState.value.copy(isSaving = false, saveSuccess = true, reservation = result.data)
                    loadReservationsByFestival(festivalId)
                }
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isSaving = false, 
                    errorMessage = result.message
                )
            }
        }
    }

    fun deleteReservation(id: Int, festivalId: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.deleteReservation(id)) {
                is Result.Success -> {
                    _detailState.value = _detailState.value.copy(isLoading = false, saveSuccess = true)
                    loadReservationsByFestival(festivalId)
                }
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isLoading = false, 
                    errorMessage = result.message
                )
            }
        }
    }

    fun resetDetailState() {
        _detailState.value = ReservationDetailUiState()
    }

    class Factory(private val repository: ReservationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ReservationViewModel(repository) as T
        }
    }
}
