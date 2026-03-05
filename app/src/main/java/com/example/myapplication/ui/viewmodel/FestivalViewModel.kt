package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FestivalRepository
import com.example.myapplication.data.Result
import com.example.myapplication.model.Festival
import com.example.myapplication.model.Jeu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FestivalListUiState(
    val isLoading: Boolean = false,
    val festivals: List<Festival> = emptyList(),
    val errorMessage: String? = null
)

data class FestivalDetailUiState(
    val isLoading: Boolean = false,
    val festival: Festival? = null,
    val games: List<Jeu> = emptyList(),
    val isLoadingGames: Boolean = false,
    val errorMessage: String? = null
)

class FestivalViewModel(private val repository: FestivalRepository) : ViewModel() {

    private val _listState = MutableStateFlow(FestivalListUiState())
    val listState: StateFlow<FestivalListUiState> = _listState

    private val _detailState = MutableStateFlow(FestivalDetailUiState())
    val detailState: StateFlow<FestivalDetailUiState> = _detailState

    fun loadFestivals() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getFestivals()) {
                is Result.Success -> _listState.value = FestivalListUiState(festivals = result.data)
                is Result.Error -> _listState.value = FestivalListUiState(errorMessage = result.message)
            }
        }
    }

    fun loadFestival(id: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getFestival(id)) {
                is Result.Success -> _detailState.value = _detailState.value.copy(
                    isLoading = false,
                    festival = result.data
                )
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun loadFestivalGames(id: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isLoadingGames = true)
            when (val result = repository.getFestivalGames(id)) {
                is Result.Success -> _detailState.value = _detailState.value.copy(
                    isLoadingGames = false,
                    games = result.data
                )
                is Result.Error -> _detailState.value = _detailState.value.copy(
                    isLoadingGames = false
                )
            }
        }
    }

    class Factory(private val repository: FestivalRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FestivalViewModel(repository) as T
        }
    }
}
