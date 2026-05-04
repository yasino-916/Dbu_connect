package com.dbuconnect.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbuconnect.data.datastore.AppDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val currentPage: Int = 0,
    val selectedIntent: String = "",
    val isCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStore: AppDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun nextPage() {
        _state.update { it.copy(currentPage = (it.currentPage + 1).coerceAtMost(2)) }
    }

    fun previousPage() {
        _state.update { it.copy(currentPage = (it.currentPage - 1).coerceAtLeast(0)) }
    }

    fun setPage(page: Int) {
        _state.update { it.copy(currentPage = page) }
    }

    fun selectIntent(intent: String) {
        _state.update { it.copy(selectedIntent = intent) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            dataStore.setOnboardingCompleted(true)
            if (_state.value.selectedIntent.isNotEmpty()) {
                dataStore.setSelectedIntent(_state.value.selectedIntent)
            }
            _state.update { it.copy(isCompleted = true) }
        }
    }

    fun skipToLogin() {
        viewModelScope.launch {
            dataStore.setOnboardingCompleted(true)
            _state.update { it.copy(isCompleted = true) }
        }
    }
}
