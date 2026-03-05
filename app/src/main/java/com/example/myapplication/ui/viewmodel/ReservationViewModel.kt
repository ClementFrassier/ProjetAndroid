package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ReservationRepository
import com.example.myapplication.data.Result
import com.example.myapplication.model.Reservation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReservationUiState(
    val isLoading: Boolean = false,
    val reservations: List<Reservation> = emptyList(),
    val errorMessage: String? = null
)

class ReservationViewModel(private val repository: ReservationRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState

    fun loadReservationsByFestival(festivalId: Int) {
        viewModelScope.launch {
            _uiState.value = ReservationUiState(isLoading = true)
            when (val result = repository.getReservationsByFestival(festivalId)) {
                is Result.Success -> _uiState.value = ReservationUiState(reservations = result.data)
                is Result.Error -> _uiState.value = ReservationUiState(errorMessage = result.message)
            }
        }
    }

    class Factory(private val repository: ReservationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ReservationViewModel(repository) as T
        }
    }
}
