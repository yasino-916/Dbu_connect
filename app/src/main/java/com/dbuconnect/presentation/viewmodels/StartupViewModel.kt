package com.dbuconnect.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbuconnect.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StartupState(
    val isReady: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isProfileComplete: Boolean = false
)

@HiltViewModel
class StartupViewModel @Inject constructor(
    repository: AppRepository
) : ViewModel() {

    val state: StateFlow<StartupState> = combine(
        repository.onboardingCompleted,
        repository.isLoggedIn,
        repository.isProfileComplete
    ) { onboardingCompleted, isLoggedIn, isProfileComplete ->
        StartupState(
            isReady = true,
            onboardingCompleted = onboardingCompleted,
            isLoggedIn = isLoggedIn,
            isProfileComplete = isProfileComplete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StartupState()
    )
}
