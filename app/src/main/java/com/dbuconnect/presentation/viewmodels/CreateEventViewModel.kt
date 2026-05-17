package com.dbuconnect.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbuconnect.data.models.Event
import com.dbuconnect.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing.asStateFlow()

    private val _publishSuccess = MutableStateFlow(false)
    val publishSuccess: StateFlow<Boolean> = _publishSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun publishEvent(title: String, description: String, date: String, time: String, location: String) {
        viewModelScope.launch {
            _isPublishing.value = true
            _error.value = null

            // Parse date and time if needed, or just use current time for MVP
            val event = Event(
                id = "", // Will be set by backend
                title = title,
                description = description,
                dateTime = System.currentTimeMillis() + 86400000, // +1 day for mock
                location = location,
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?q=80&w=2070&auto=format&fit=crop", // placeholder
                tags = listOf("Campus", "General")
            )

            val result = repository.createEvent(event)
            
            result.onSuccess {
                _publishSuccess.value = true
            }.onFailure {
                _error.value = it.message ?: "Failed to publish event"
            }
            
            _isPublishing.value = false
        }
    }
}
