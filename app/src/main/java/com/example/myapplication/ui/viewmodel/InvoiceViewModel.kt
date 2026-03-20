package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.InvoiceRepository
import com.example.myapplication.data.Result
import com.example.myapplication.model.Invoice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class InvoiceDetailUiState(
    val isLoading: Boolean = false,
    val invoice: Invoice? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val isPaying: Boolean = false,
    val operationSuccess: Boolean = false
)

class InvoiceViewModel(private val repository: InvoiceRepository) : ViewModel() {

    private val _detailState = MutableStateFlow(InvoiceDetailUiState())
    val detailState: StateFlow<InvoiceDetailUiState> = _detailState

    fun loadInvoiceByReservation(reservationId: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                isLoading = true,
                errorMessage = null,
                operationSuccess = false
            )
            when (val result = repository.getInvoiceByReservation(reservationId)) {
                is Result.Success -> _detailState.value = _detailState.value.copy(
                    isLoading = false,
                    invoice = result.data
                )
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun createInvoice(reservationId: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                isSaving = true,
                errorMessage = null,
                operationSuccess = false
            )
            when (val result = repository.createInvoiceForReservation(reservationId)) {
                is Result.Success -> _detailState.value = _detailState.value.copy(
                    isSaving = false,
                    operationSuccess = true,
                    invoice = result.data
                )
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isSaving = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun markInvoiceAsPaid(id: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isPaying = true, errorMessage = null)
            when (val result = repository.markInvoiceAsPaid(id)) {
                is Result.Success -> _detailState.value = _detailState.value.copy(
                    isPaying = false,
                    operationSuccess = true,
                    invoice = result.data
                )
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isPaying = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun resetDetailState() {
        _detailState.value = InvoiceDetailUiState()
    }

    class Factory(private val repository: InvoiceRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return InvoiceViewModel(repository) as T
        }
    }
}
