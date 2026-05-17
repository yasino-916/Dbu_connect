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
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val phone: String = "",
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

    fun updateEmail(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun updatePassword(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun updateName(name: String) {
        _state.update { it.copy(name = name, error = null) }
    }

    fun updatePhone(phone: String) {
        _state.update { it.copy(phone = phone, error = null) }
    }

    fun toggleSignUpMode() {
        _state.update { it.copy(isSignUpMode = !it.isSignUpMode, error = null) }
    }

    /**
     * Validates that the email is a DBU university email (ends with @dbu.edu.et)
     */
    private fun isValidDBUEmail(email: String): Boolean {
        return email.isNotBlank() && email.trim().lowercase().endsWith("@dbu.edu.et")
    }

    fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val email = _state.value.email.trim()
            val password = _state.value.password

            // Validate university email
            if (!isValidDBUEmail(email)) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Please use your university email (e.g., name@dbu.edu.et)"
                    )
                }
                return@launch
            }

            if (password.length < 6) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Password must be at least 6 characters"
                    )
                }
                return@launch
            }

            val result = repository.login(email, password)
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

            val email = _state.value.email.trim()
            val password = _state.value.password
            val name = _state.value.name

            // Validate name
            if (name.isBlank()) {
                _state.update { it.copy(isLoading = false, error = "Please enter your full name") }
                return@launch
            }

            // Validate university email
            if (!isValidDBUEmail(email)) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Please use your university email (e.g., name@dbu.edu.et)"
                    )
                }
                return@launch
            }

            if (password.length < 6) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Password must be at least 6 characters"
                    )
                }
                return@launch
            }

            val result = repository.signUp(name.trim(), email, _state.value.phone.trim(), password)
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
