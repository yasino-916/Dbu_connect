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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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

            // Issue #17: Parse the actual date and time entered by the user
            val dateTimeMillis = parseDateTimeToMillis(date, time)
            if (dateTimeMillis == null) {
                _error.value = "Invalid date/time format. Use MM/DD/YYYY and HH:MM AM/PM."
                _isPublishing.value = false
                return@launch
            }

            val event = Event(
                id = "", // Will be set by backend
                title = title,
                description = description,
                dateTime = dateTimeMillis,
                location = location,
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?q=80&w=2070&auto=format&fit=crop",
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

    /**
     * Parses user-entered date (MM/DD/YYYY) and time (HH:MM AM/PM or HH:MM) into epoch millis.
     * Falls back to date-only if time is blank. Returns null on parse failure.
     */
    private fun parseDateTimeToMillis(date: String, time: String): Long? {
        return try {
            val combined = if (time.isNotBlank()) "$date $time" else date
            val formats = listOf(
                SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US),
                SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US),
                SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.US),
                SimpleDateFormat("MM/dd/yyyy", Locale.US),
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US),
                SimpleDateFormat("dd/MM/yyyy", Locale.US),
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US),
                SimpleDateFormat("yyyy-MM-dd", Locale.US)
            )

            for (format in formats) {
                format.isLenient = false
                try {
                    val parsed = format.parse(combined)
                    if (parsed != null) return parsed.time
                } catch (_: Exception) { /* try next */ }
            }

            null
        } catch (_: Exception) {
            null
        }
    }
}
