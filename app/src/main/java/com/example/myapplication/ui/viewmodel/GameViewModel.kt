package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.GameRepository
import com.example.myapplication.data.Result
import com.example.myapplication.model.GameCreateInput
import com.example.myapplication.model.GameInput
import com.example.myapplication.model.GameWithEditor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GameListUiState(
    val isLoading: Boolean = false,
    val games: List<GameWithEditor> = emptyList(),
    val errorMessage: String? = null
)

data class GameDetailUiState(
    val isLoading: Boolean = false,
    val game: GameWithEditor? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    private val _listState = MutableStateFlow(GameListUiState())
    val listState: StateFlow<GameListUiState> = _listState

    private val _detailState = MutableStateFlow(GameDetailUiState())
    val detailState: StateFlow<GameDetailUiState> = _detailState

    fun loadGames() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getGames()) {
                is Result.Success -> _listState.value = GameListUiState(games = result.data)
                is Result.Error -> _listState.value = GameListUiState(errorMessage = result.message)
            }
        }
    }

    fun loadGame(id: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                isLoading = true, 
                errorMessage = null, 
                saveSuccess = false
            )
            when (val result = repository.getGameById(id)) {
                is Result.Success -> _detailState.value = _detailState.value.copy(
                    isLoading = false,
                    game = result.data
                )
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun createGame(input: GameCreateInput) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                isSaving = true, 
                errorMessage = null, 
                saveSuccess = false
            )
            when (val result = repository.createGame(input)) {
                is Result.Success -> {
                    _detailState.value = _detailState.value.copy(
                        isSaving = false, 
                        saveSuccess = true, 
                        game = result.data
                    )
                    loadGames()
                }
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isSaving = false, 
                    errorMessage = result.message
                )
            }
        }
    }

    fun updateGame(id: Int, input: GameInput) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                isSaving = true, 
                errorMessage = null, 
                saveSuccess = false
            )
            when (val result = repository.updateGame(id, input)) {
                is Result.Success -> {
                    _detailState.value = _detailState.value.copy(
                        isSaving = false, 
                        saveSuccess = true, 
                        game = result.data
                    )
                    loadGames()
                }
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isSaving = false, 
                    errorMessage = result.message
                )
            }
        }
    }

    fun deleteGame(id: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.deleteGame(id)) {
                is Result.Success -> {
                    _detailState.value = _detailState.value.copy(isLoading = false, saveSuccess = true)
                    loadGames()
                }
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isLoading = false, 
                    errorMessage = result.message
                )
            }
        }
    }

    fun resetDetailState() {
        _detailState.value = GameDetailUiState()
    }

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
    }
}
