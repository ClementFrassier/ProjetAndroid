package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.InvoiceRepository
import com.example.myapplication.data.Result
import com.example.myapplication.model.Invoice
import com.example.myapplication.model.InvoiceCreateInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class InvoiceListUiState(
    val isLoading: Boolean = false,
    val invoices: List<Invoice> = emptyList(),
    val errorMessage: String? = null
)

data class InvoiceDetailUiState(
    val isLoading: Boolean = false,
    val invoice: Invoice? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val isPaying: Boolean = false,
    val isDeleting: Boolean = false,
    val operationSuccess: Boolean = false
)

class InvoiceViewModel(private val repository: InvoiceRepository) : ViewModel() {

    private val _listState = MutableStateFlow(InvoiceListUiState())
    val listState: StateFlow<InvoiceListUiState> = _listState

    private val _detailState = MutableStateFlow(InvoiceDetailUiState())
    val detailState: StateFlow<InvoiceDetailUiState> = _detailState

    fun loadInvoices(festivalId: Int? = null, editorId: Int? = null, isPaid: Boolean? = null) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getInvoices(festivalId, editorId, isPaid)) {
                is Result.Success -> _listState.value = InvoiceListUiState(invoices = result.data)
                is Result.Error -> _listState.value = InvoiceListUiState(errorMessage = result.message)
            }
        }
    }

    fun loadInvoice(id: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isLoading = true, errorMessage = null, operationSuccess = false)
            when (val result = repository.getInvoiceById(id)) {
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

    fun createInvoice(input: InvoiceCreateInput, festivalId: Int? = null) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isSaving = true, errorMessage = null, operationSuccess = false)
            when (val result = repository.createInvoice(input)) {
                is Result.Success -> {
                    _detailState.value = _detailState.value.copy(isSaving = false, operationSuccess = true, invoice = result.data)
                    loadInvoices(festivalId = festivalId)
                }
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isSaving = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun markInvoiceAsPaid(id: Int, festivalId: Int? = null) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isPaying = true, errorMessage = null)
            when (val result = repository.markInvoiceAsPaid(id)) {
                is Result.Success -> {
                    // Mettre à jour la facture dans l'état avec la nouvelle valeur
                    val updatedInvoice = _detailState.value.invoice?.copy(isPaid = true, paidAt = result.data.paidAt)
                    _detailState.value = _detailState.value.copy(isPaying = false, invoice = updatedInvoice)
                    loadInvoices(festivalId = festivalId)
                }
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isPaying = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun deleteInvoice(id: Int, festivalId: Int? = null) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isDeleting = true, errorMessage = null)
            when (val result = repository.deleteInvoice(id)) {
                is Result.Success -> {
                    _detailState.value = _detailState.value.copy(isDeleting = false, operationSuccess = true)
                    loadInvoices(festivalId = festivalId)
                }
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isDeleting = false,
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
