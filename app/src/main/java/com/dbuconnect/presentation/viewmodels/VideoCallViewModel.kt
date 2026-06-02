package com.dbuconnect.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbuconnect.data.models.Match
import com.dbuconnect.data.models.Notification
import com.dbuconnect.data.models.NotificationType
import com.dbuconnect.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class CallState {
    DIALING,
    RINGING,
    CONNECTED,
    REJECTED,
    ENDED
}

data class VideoCallState(
    val matchId: String = "",
    val peerName: String = "Connecting...",
    val peerPhotoUrl: String = "",
    val callState: CallState = CallState.DIALING,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isVideoDisabled: Boolean = false,
    val isFrontCamera: Boolean = true
)

@HiltViewModel
class VideoCallViewModel @Inject constructor(
    private val repository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: String = savedStateHandle["matchId"] ?: ""
    private val isOutgoing: Boolean = savedStateHandle.get<Boolean>("isOutgoing") ?: true

    private val _state = MutableStateFlow(VideoCallState(matchId = matchId))
    val state: StateFlow<VideoCallState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var signalingJob: Job? = null

    init {
        loadPeerDetails()
        setupSignaling()
    }

    private fun loadPeerDetails() {
        viewModelScope.launch {
            repository.observeMatch(matchId).collect { match ->
                if (match != null) {
                    _state.update {
                        it.copy(
                            peerName = match.userName,
                            peerPhotoUrl = match.userPhotoUrl
                        )
                    }
                }
            }
        }
    }

    private fun setupSignaling() {
        viewModelScope.launch {
            if (isOutgoing) {
                // Outgoing call signaling
                _state.update { it.copy(callState = CallState.DIALING) }
                
                // Create an incoming call notification for the remote peer
                val currentUser = repository.getCurrentUser()
                val match = repository.getCurrentUser()?.id?.let { currentUserId ->
                    repository.observeMatch(matchId).firstOrNull()
                }

                if (currentUser != null) {
                    val notification = Notification(
                        id = UUID.randomUUID().toString(),
                        type = NotificationType.INCOMING_CALL,
                        title = "Incoming Video Call 📞",
                        message = "${currentUser.name} is calling you...",
                        fromUserId = currentUser.id,
                        fromUserName = currentUser.name,
                        fromUserPhoto = currentUser.photos.firstOrNull() ?: "",
                        relatedId = matchId,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.addNotification(notification)
                }

                delay(1500)
                _state.update { it.copy(callState = CallState.RINGING) }

                // Check for Call Accept or Reject
                signalingJob = viewModelScope.launch {
                    while (true) {
                        delay(2000)
                        val notifications = repository.getNotificationsDirectly()
                        val isRejected = notifications.any {
                            it.type == NotificationType.CALL_REJECTED && it.relatedId == matchId
                        }
                        val isAccepted = notifications.any {
                            it.type == NotificationType.CALL_ACCEPTED && it.relatedId == matchId
                        }

                        if (isRejected) {
                            rejectCall()
                            break
                        } else if (isAccepted) {
                            connectCall()
                            break
                        }
                    }
                }

                // Auto-connect call after 4.5 seconds for mock experience if no manual interaction occurs
                delay(4500)
                if (_state.value.callState == CallState.RINGING) {
                    connectCall()
                }
            } else {
                // Incoming call accepted state
                connectCall()
            }
        }
    }

    fun connectCall() {
        signalingJob?.cancel()
        _state.update { it.copy(callState = CallState.CONNECTED) }
        
        // Notify peer about connection if we accepted an incoming call
        if (!isOutgoing) {
            viewModelScope.launch {
                val currentUser = repository.getCurrentUser()
                if (currentUser != null) {
                    val acceptNotification = Notification(
                        id = UUID.randomUUID().toString(),
                        type = NotificationType.CALL_ACCEPTED,
                        title = "Call Connected ✅",
                        message = "Call connected with ${currentUser.name}",
                        fromUserId = currentUser.id,
                        fromUserName = currentUser.name,
                        relatedId = matchId,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.addNotification(acceptNotification)
                }
            }
        }

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }
    }

    fun toggleMute() {
        _state.update { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleVideo() {
        _state.update { it.copy(isVideoDisabled = !it.isVideoDisabled) }
    }

    fun flipCamera() {
        _state.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun rejectCall() {
        signalingJob?.cancel()
        timerJob?.cancel()
        _state.update { it.copy(callState = CallState.REJECTED) }
        
        // Send rejection notification
        viewModelScope.launch {
            val currentUser = repository.getCurrentUser()
            if (currentUser != null) {
                val rejectNotification = Notification(
                    id = UUID.randomUUID().toString(),
                    type = NotificationType.CALL_REJECTED,
                    title = "Call Rejected ❌",
                    message = "Call rejected by ${currentUser.name}",
                    fromUserId = currentUser.id,
                    fromUserName = currentUser.name,
                    relatedId = matchId,
                    timestamp = System.currentTimeMillis()
                )
                repository.addNotification(rejectNotification)
            }
        }
    }

    fun endCall() {
        signalingJob?.cancel()
        timerJob?.cancel()
        _state.update { it.copy(callState = CallState.ENDED) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        signalingJob?.cancel()
    }
}
