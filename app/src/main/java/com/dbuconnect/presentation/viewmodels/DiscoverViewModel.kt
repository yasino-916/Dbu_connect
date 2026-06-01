package com.dbuconnect.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbuconnect.data.models.*
import com.dbuconnect.data.repository.AppRepository
import com.dbuconnect.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ProfileCard>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ProfileCard>>> = _uiState.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _matchResult = MutableStateFlow<Match?>(null)
    val matchResult: StateFlow<Match?> = _matchResult.asStateFlow()

    private val _likedNotification = MutableSharedFlow<String>()
    val likedNotification = _likedNotification.asSharedFlow()

    private val _filters = MutableStateFlow(FilterSettings())
    val filters: StateFlow<FilterSettings> = _filters.asStateFlow()

    init {
        loadCards()
        viewModelScope.launch {
            repository.filterSettings.collect { _filters.value = it }
        }
    }

    fun loadCards() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.getDiscoverCards()
            result.onSuccess { cards ->
                if (cards.isEmpty()) {
                    _uiState.value = UiState.Empty
                } else {
                    _uiState.value = UiState.Success(cards)
                    _currentIndex.value = 0
                }
            }.onFailure { error ->
                _uiState.value = UiState.Error(error.message ?: "Failed to load profiles")
            }
        }
    }

    fun likeProfile(card: ProfileCard) {
        viewModelScope.launch {
            val result = repository.likeProfile(card.userId)
            result.onSuccess { match ->
                if (match != null) {
                    _matchResult.value = match
                } else {
                    _likedNotification.emit("Liked ${card.name}! You will match when they like you back.")
                }
            }
            advanceCard()
        }
    }

    fun passProfile(card: ProfileCard) {
        viewModelScope.launch {
            repository.passProfile(card.userId)
            advanceCard()
        }
    }

    private fun advanceCard() {
        val state = _uiState.value
        if (state is UiState.Success) {
            val newIndex = _currentIndex.value + 1
            if (newIndex >= state.data.size) {
                _uiState.value = UiState.Empty
            } else {
                _currentIndex.value = newIndex
            }
        }
    }

    fun clearMatchResult() {
        _matchResult.value = null
    }

    fun updateFilters(settings: FilterSettings) {
        viewModelScope.launch {
            repository.updateFilterSettings(settings)
            _filters.value = settings
            loadCards()
        }
    }
}
