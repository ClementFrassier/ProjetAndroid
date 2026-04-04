package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.EditorRepository
import com.example.myapplication.data.Result
import com.example.myapplication.model.Editor
import com.example.myapplication.model.EditorDetail
import com.example.myapplication.model.EditorInput
import com.example.myapplication.model.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditorListUiState(
    val isLoading: Boolean = false,
    val editors: List<Editor> = emptyList(),
    val errorMessage: String? = null
)

data class EditorDetailUiState(
    val isLoading: Boolean = false,
    val editor: EditorDetail? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

data class EditorGamesUiState(
    val isLoading: Boolean = false,
    val games: List<Game> = emptyList(),
    val errorMessage: String? = null
)

class EditorViewModel(private val repository: EditorRepository) : ViewModel() {

    private val _listState = MutableStateFlow(EditorListUiState())
    val listState: StateFlow<EditorListUiState> = _listState

    private val _detailState = MutableStateFlow(EditorDetailUiState())
    val detailState: StateFlow<EditorDetailUiState> = _detailState

    private val _gamesState = MutableStateFlow(EditorGamesUiState())
    val gamesState: StateFlow<EditorGamesUiState> = _gamesState

    fun loadEditors() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getEditors()) {
                is Result.Success -> _listState.value = EditorListUiState(editors = result.data)
                is Result.Error -> _listState.value = EditorListUiState(errorMessage = result.message)
            }
        }
    }

    fun loadEditor(id: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                isLoading = true, 
                errorMessage = null, 
                saveSuccess = false
            )
            when (val result = repository.getEditorById(id)) {
                is Result.Success -> _detailState.value = _detailState.value.copy(
                    isLoading = false,
                    editor = result.data
                )
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun loadEditorGames(id: Int) {
        viewModelScope.launch {
            _gamesState.value = _gamesState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getEditorGames(id)) {
                is Result.Success -> _gamesState.value = EditorGamesUiState(games = result.data)
                is Result.Error -> _gamesState.value = EditorGamesUiState(errorMessage = result.message)
            }
        }
    }

    fun createEditor(input: EditorInput) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                isSaving = true, 
                errorMessage = null, 
                saveSuccess = false
            )
            when (val result = repository.createEditor(input)) {
                is Result.Success -> {
                    _detailState.value = _detailState.value.copy(
                        isSaving = false, 
                        saveSuccess = true, 
                        editor = result.data
                    )
                    loadEditors()
                }
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isSaving = false, 
                    errorMessage = result.message
                )
            }
        }
    }

    fun updateEditor(id: Int, input: EditorInput) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(
                isSaving = true, 
                errorMessage = null, 
                saveSuccess = false
            )
            when (val result = repository.updateEditor(id, input)) {
                is Result.Success -> {
                    _detailState.value = _detailState.value.copy(
                        isSaving = false, 
                        saveSuccess = true, 
                        editor = result.data
                    )
                    loadEditors()
                }
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isSaving = false, 
                    errorMessage = result.message
                )
            }
        }
    }

    fun deleteEditor(id: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.deleteEditor(id)) {
                is Result.Success -> {
                    _detailState.value = _detailState.value.copy(isLoading = false, saveSuccess = true)
                    loadEditors()
                }
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isLoading = false, 
                    errorMessage = result.message
                )
            }
        }
    }
    
    fun resetDetailState() {
        _detailState.value = EditorDetailUiState()
        _gamesState.value = EditorGamesUiState()
    }

    class Factory(private val repository: EditorRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return EditorViewModel(repository) as T
        }
    }
}
