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
class EventsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    val events: StateFlow<List<Event>> = repository.observeEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<User?> = repository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.refreshEvents()
            result.onSuccess {
                _uiState.value = UiState.Success(Unit)
            }.onFailure { error ->
                _uiState.value = UiState.Error(error.message ?: "Failed to load events")
            }
        }
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun rsvp(eventId: String, status: RsvpStatus) {
        viewModelScope.launch {
            repository.rsvpEvent(eventId, status)
        }
    }
}
