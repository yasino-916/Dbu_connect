package com.dbuconnect.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbuconnect.data.models.*
import com.dbuconnect.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val privacySettings: PrivacySettings = PrivacySettings(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,

    // Edit fields
    val editName: String = "",
    val editBio: String = "",
    val editDepartment: String = "",
    val editYear: Int = 1,
    val editInterests: List<String> = emptyList(),
    val editPhotos: List<String> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
        viewModelScope.launch {
            repository.privacySettings.collect { settings ->
                _state.update { it.copy(privacySettings = settings) }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val user = repository.getCurrentUser()
            _state.update {
                it.copy(
                    isLoading = false,
                    user = user,
                    editName = user?.name ?: "",
                    editBio = user?.bio ?: "",
                    editDepartment = user?.department ?: "",
                    editYear = user?.year ?: 1,
                    editInterests = user?.interests ?: emptyList(),
                    editPhotos = user?.photos ?: emptyList()
                )
            }
        }
    }

    fun updateName(name: String) { _state.update { it.copy(editName = name) } }
    fun updateBio(bio: String) { _state.update { it.copy(editBio = bio) } }
    fun updateDepartment(dept: String) { _state.update { it.copy(editDepartment = dept) } }
    fun updateYear(year: Int) { _state.update { it.copy(editYear = year) } }
    fun updatePhotos(photos: List<String>) { _state.update { it.copy(editPhotos = photos) } }

    fun toggleInterest(interest: String) {
        _state.update { state ->
            val current = state.editInterests.toMutableList()
            if (current.contains(interest)) current.remove(interest) else current.add(interest)
            state.copy(editInterests = current)
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val current = _state.value
            val user = current.user?.copy(
                name = current.editName,
                bio = current.editBio,
                department = current.editDepartment,
                year = current.editYear,
                interests = current.editInterests,
                photos = current.editPhotos,
                isProfileComplete = true
            ) ?: return@launch

            _state.update { it.copy(isSaving = true) }
            val result = repository.updateProfile(user)
            result.onSuccess {
                _state.update { it.copy(isSaving = false, user = user) }
            }.onFailure { error ->
                _state.update { it.copy(isSaving = false, error = error.message) }
            }
        }
    }

    fun updatePrivacySetting(settings: PrivacySettings) {
        viewModelScope.launch {
            repository.updatePrivacySettings(settings)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
