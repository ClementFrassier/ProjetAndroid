package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Result
import com.example.myapplication.data.UserRepository
import com.example.myapplication.model.CreateUserInput
import com.example.myapplication.model.UpdateUserRoleInput
import com.example.myapplication.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserListUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val errorMessage: String? = null,
    val isActionInProgress: Boolean = false,
    val actionErrorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, actionErrorMessage = null, actionSuccessMessage = null)
            when (val result = repository.getUsers()) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    users = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun createUser(input: CreateUserInput) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true, actionErrorMessage = null, actionSuccessMessage = null)
            when (val result = repository.createUser(input)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        actionSuccessMessage = "Utilisateur créé avec succès"
                    )
                    loadUsers()
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isActionInProgress = false,
                    actionErrorMessage = result.message
                )
            }
        }
    }

    fun updateUserRole(id: Int, payload: UpdateUserRoleInput) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true, actionErrorMessage = null, actionSuccessMessage = null)
            when (val result = repository.updateUserRole(id, payload)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        actionSuccessMessage = "Rôle mis à jour avec succès"
                    )
                    loadUsers()
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isActionInProgress = false,
                    actionErrorMessage = result.message
                )
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true, actionErrorMessage = null, actionSuccessMessage = null)
            when (val result = repository.deleteUser(id)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        actionSuccessMessage = "Utilisateur supprimé"
                    )
                    loadUsers()
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isActionInProgress = false,
                    actionErrorMessage = result.message
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            actionErrorMessage = null,
            actionSuccessMessage = null
        )
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
    }
}
