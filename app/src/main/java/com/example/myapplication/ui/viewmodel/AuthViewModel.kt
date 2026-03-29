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

private const val ROLE_SUPER_ADMIN = "super_admin"
private const val ROLE_SUPER_ORGANISATEUR = "super_organisateur"
private const val ROLE_ORGANISATEUR = "organisateur"
private const val ROLE_BENEVOLE = "benevole"

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

    fun hasAnyRole(vararg roles: String): Boolean {
        val role = _uiState.value.userRole ?: return false
        if (role == ROLE_SUPER_ADMIN) return true
        return roles.contains(role)
    }

    fun canManageFestivals(): Boolean = hasAnyRole(ROLE_SUPER_ADMIN, ROLE_SUPER_ORGANISATEUR)

    fun canManageReservations(): Boolean = hasAnyRole(ROLE_SUPER_ADMIN, ROLE_SUPER_ORGANISATEUR)

    fun canManagePlacement(): Boolean =
        hasAnyRole(ROLE_SUPER_ADMIN, ROLE_SUPER_ORGANISATEUR, ROLE_ORGANISATEUR)

    fun canReadReservations(): Boolean =
        hasAnyRole(ROLE_SUPER_ADMIN, ROLE_SUPER_ORGANISATEUR, ROLE_ORGANISATEUR, ROLE_BENEVOLE)

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
