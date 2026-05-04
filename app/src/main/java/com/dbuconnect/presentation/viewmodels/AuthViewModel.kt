package com.dbuconnect.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbuconnect.data.models.User
import com.dbuconnect.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val phone: String = "",
    val email: String = "",
    val otp: String = "",
    val name: String = "",
    val password: String = "",
    val isPhoneMode: Boolean = true,
    val isOtpSent: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val isSignUpMode: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun updatePhone(phone: String) {
        _state.update { it.copy(phone = phone, error = null) }
    }

    fun updateEmail(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun updateOtp(otp: String) {
        _state.update { it.copy(otp = otp, error = null) }
    }

    fun updateName(name: String) {
        _state.update { it.copy(name = name, error = null) }
    }

    fun updatePassword(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun togglePhoneMode() {
        _state.update { it.copy(isPhoneMode = !it.isPhoneMode, error = null) }
    }

    fun toggleSignUpMode() {
        _state.update { it.copy(isSignUpMode = !it.isSignUpMode, error = null) }
    }

    fun sendOtp() {
        val phone = _state.value.phone
        if (phone.length < 9) {
            _state.update { it.copy(error = "Please enter a valid phone number") }
            return
        }
        _state.update { it.copy(isOtpSent = true, error = null) }
    }

    fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val phone = _state.value.phone
            val otp = _state.value.otp

            if (otp.length < 4) {
                _state.update { it.copy(isLoading = false, error = "Please enter the verification code") }
                return@launch
            }

            val result = repository.login(phone, otp)
            result.onSuccess { user ->
                _state.update { it.copy(isLoading = false, user = user, isLoggedIn = true) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.message ?: "Login failed") }
            }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = repository.login(_state.value.phone, "mock_otp")
            result.onSuccess { user ->
                _state.update { it.copy(isLoading = false, user = user, isLoggedIn = true) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.message ?: "Sign up failed") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
