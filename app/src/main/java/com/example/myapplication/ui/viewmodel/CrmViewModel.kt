package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CrmRepository
import com.example.myapplication.data.Result
import com.example.myapplication.model.CrmContactCreateInput
import com.example.myapplication.model.CrmFollowUp
import com.example.myapplication.model.CrmRow
import com.example.myapplication.model.CrmUpsertInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CrmUiState(
    val isLoading: Boolean = false,
    val rows: List<CrmRow> = emptyList(),
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val saveErrorMessage: String? = null,
    val contactsByEditor: Map<Int, List<CrmFollowUp>> = emptyMap(),
    val contactsLoading: Set<Int> = emptySet()
)

class CrmViewModel(private val repository: CrmRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CrmUiState())
    val uiState: StateFlow<CrmUiState> = _uiState

    // Le CRM se lit toujours a l'echelle d'un festival :
    // on recharge donc toute la table de suivi en une fois.
    fun loadFestivalCrm(festivalId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                saveErrorMessage = null
            )
            when (val result = repository.getRowsByFestival(festivalId)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    rows = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    // Un changement de statut repasse par l'API CRM existante, puis on recharge
    // la liste pour rester colle a l'etat reel du backend.
    fun updateStatus(editorId: Int, festivalId: Int, status: String, notes: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveErrorMessage = null)
            when (
                val result = repository.upsertStatus(
                    CrmUpsertInput(
                        editorId = editorId,
                        festivalId = festivalId,
                        status = status,
                        notes = notes
                    )
                )
            ) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    loadFestivalCrm(festivalId)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveErrorMessage = result.message
                )
            }
        }
    }

    fun saveNotes(editorId: Int, festivalId: Int, currentStatus: String?, notes: String?) {
        updateStatus(editorId, festivalId, currentStatus ?: "pas_de_contact", notes)
    }

    // Quand on ajoute un contact, on suit la logique du projet web :
    // le simple fait d'avoir pris contact fait basculer le statut en "contact_pris".
    fun addContact(editorId: Int, festivalId: Int, contactType: String?, notes: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveErrorMessage = null)
            when (
                val result = repository.addContact(
                    CrmContactCreateInput(
                        editorId = editorId,
                        festivalId = festivalId,
                        contactType = contactType,
                        notes = notes
                    )
                )
            ) {
                is Result.Success -> updateStatus(editorId, festivalId, "contact_pris")
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveErrorMessage = result.message
                )
            }
        }
    }

    // L'historique des contacts est charge editeur par editeur pour eviter
    // de charger tout le monde d'un coup a l'ouverture de l'ecran.
    fun loadContacts(editorId: Int, festivalId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                contactsLoading = _uiState.value.contactsLoading + editorId
            )
            when (val result = repository.getContacts(editorId, festivalId)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    contactsByEditor = _uiState.value.contactsByEditor + (editorId to result.data),
                    contactsLoading = _uiState.value.contactsLoading - editorId
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    contactsLoading = _uiState.value.contactsLoading - editorId,
                    saveErrorMessage = result.message
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, saveErrorMessage = null)
    }

    class Factory(private val repository: CrmRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CrmViewModel(repository) as T
        }
    }
}
