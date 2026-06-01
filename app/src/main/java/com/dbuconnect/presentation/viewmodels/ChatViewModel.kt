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
    val otherUserId: String = "",
    val currentUserId: String = "current_user",
    val isOnline: Boolean = false,
    val messageText: String = "",
    val isLoading: Boolean = true,
    val actionMessage: String? = null,
    val error: String? = null
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
        loadCurrentUserId()
        observeMatch()
        loadMessages()
    }

    private fun loadCurrentUserId() {
        viewModelScope.launch {
            val user = repository.getCurrentUser()
            if (user != null) {
                _state.update { it.copy(currentUserId = user.id) }
            }
        }
    }

    private fun observeMatch() {
        viewModelScope.launch {
            val currentUserId = repository.getCurrentUser()?.id
            repository.observeMatch(matchId).collect { match ->
                if (match != null) {
                    val otherUserId = when (currentUserId) {
                        match.userAId -> match.userBId
                        match.userBId -> match.userAId
                        else -> match.userBId
                    }

                    _state.update {
                        it.copy(
                            matchName = match.userName,
                            matchPhotoUrl = match.userPhotoUrl,
                            otherUserId = otherUserId,
                            isOnline = match.isOnline
                        )
                    }
                }
            }
        }
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
            val result = repository.sendMessage(matchId, text)
            // Issue #15: update match last message locally for immediate UI feedback
            result.onSuccess { message ->
                repository.updateMatchLastMessage(matchId, message.text, message.timestamp)
            }
        }
    }

    fun sendQuickPrompt(prompt: String) {
        viewModelScope.launch {
            repository.sendMessage(matchId, prompt)
        }
    }

    fun reportMatch(reason: String = "Inappropriate behavior", details: String = "") {
        val userId = _state.value.otherUserId
        if (userId.isBlank()) {
            _state.update { it.copy(error = "Could not identify the user to report") }
            return
        }

        viewModelScope.launch {
            val result = repository.reportUser(userId, reason, details)
            result.onSuccess {
                _state.update { it.copy(actionMessage = "Report submitted. Thank you for helping keep DBU Connect safe.") }
            }.onFailure { error ->
                _state.update { it.copy(error = error.message ?: "Failed to submit report") }
            }
        }
    }

    fun blockMatch() {
        val userId = _state.value.otherUserId
        if (userId.isBlank()) {
            _state.update { it.copy(error = "Could not identify the user to block") }
            return
        }

        viewModelScope.launch {
            val result = repository.blockUser(userId)
            result.onSuccess {
                _state.update { it.copy(actionMessage = "User blocked") }
            }.onFailure { error ->
                _state.update { it.copy(error = error.message ?: "Failed to block user") }
            }
        }
    }

    fun clearTransientMessages() {
        _state.update { it.copy(actionMessage = null, error = null) }
    }
}
