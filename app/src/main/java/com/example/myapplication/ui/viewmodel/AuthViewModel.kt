package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AuthManager
import com.example.myapplication.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val userLogin: String? = null,
    val userRole: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        _uiState.value = AuthUiState(
            isLoggedIn = authManager.isLoggedIn(),
            userLogin = authManager.getLogin(),
            userRole = authManager.getRole()
        )
    }

    fun login(login: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.login(login, password)
            when (result) {
                is com.example.myapplication.data.Result.Success -> {
                    _uiState.value = AuthUiState(
                        isLoggedIn = true,
                        userLogin = authManager.getLogin(),
                        userRole = authManager.getRole()
                    )
                }
                is com.example.myapplication.data.Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState(isLoggedIn = false)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(
        private val repository: AuthRepository,
        private val authManager: AuthManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, authManager) as T
        }
    }
}
