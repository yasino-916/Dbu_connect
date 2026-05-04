package com.dbuconnect.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbuconnect.data.models.*
import com.dbuconnect.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatState(
    val matchId: String = "",
    val matchName: String = "",
    val matchPhotoUrl: String = "",
    val isOnline: Boolean = false,
    val messageText: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: String = savedStateHandle["matchId"] ?: ""

    private val _state = MutableStateFlow(ChatState(matchId = matchId))
    val state: StateFlow<ChatState> = _state.asStateFlow()

    val messages: StateFlow<List<Message>> = repository.observeMessages(matchId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.refreshMessages(matchId)
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun updateMessageText(text: String) {
        _state.update { it.copy(messageText = text) }
    }

    fun sendMessage() {
        val text = _state.value.messageText.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(messageText = "") }
            repository.sendMessage(matchId, text)
        }
    }

    fun sendQuickPrompt(prompt: String) {
        viewModelScope.launch {
            repository.sendMessage(matchId, prompt)
        }
    }
}
