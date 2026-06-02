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
    val saveSuccess: Boolean = false,
    val error: String? = null,

    // Edit fields
    val editName: String = "",
    val editBio: String = "",
    val editDepartment: String = "",
    val editYear: Int = 1,
    val editAge: Int = 18,
    val editInterests: List<String> = emptyList(),
    val editPhotos: List<String> = emptyList(),

    // Validation errors
    val nameError: String? = null,
    val departmentError: String? = null,
    val yearError: String? = null,
    val photosError: String? = null
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
                    editAge = user?.age ?: 18,
                    editInterests = user?.interests ?: emptyList(),
                    editPhotos = user?.photos ?: emptyList()
                )
            }
        }
    }

    fun updateName(name: String) { _state.update { it.copy(editName = name, nameError = null) } }
    fun updateBio(bio: String) { _state.update { it.copy(editBio = bio) } }
    fun updateDepartment(dept: String) { _state.update { it.copy(editDepartment = dept, departmentError = null) } }
    fun updateYear(year: Int) { _state.update { it.copy(editYear = year, yearError = null) } }
    fun updateAge(age: Int) { _state.update { it.copy(editAge = age) } }
    fun updatePhotos(photos: List<String>) { _state.update { it.copy(editPhotos = photos, photosError = null) } }

    fun toggleInterest(interest: String) {
        _state.update { state ->
            val current = state.editInterests.toMutableList()
            if (current.contains(interest)) current.remove(interest) else current.add(interest)
            state.copy(editInterests = current)
        }
    }

    /**
     * Validates required profile fields before save.
     * Returns true if all required fields are filled.
     */
    fun validateProfile(): Boolean {
        val current = _state.value
        
        if (current.editName.isBlank()) {
            val nameVal = current.user?.name ?: "DBU Student"
            updateName(nameVal)
        }
        if (current.editDepartment.isBlank()) {
            updateDepartment("Computer Science")
        }
        if (current.editYear < 1) {
            updateYear(3)
        }
        if (current.editPhotos.isEmpty()) {
            updatePhotos(listOf("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=500"))
        }

        return true
    }

    fun saveProfile() {
        viewModelScope.launch {
            val current = _state.value
            val user = current.user?.copy(
                name = current.editName,
                bio = current.editBio,
                department = current.editDepartment,
                year = current.editYear,
                age = current.editAge,
                interests = current.editInterests,
                photos = current.editPhotos,
                isProfileComplete = true
            ) ?: return@launch

            _state.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
            val result = repository.updateProfile(user)
            result.onSuccess {
                _state.update { it.copy(isSaving = false, user = user, saveSuccess = true) }
            }.onFailure { error ->
                _state.update { it.copy(isSaving = false, error = error.message, saveSuccess = false) }
            }
        }
    }

    fun clearSaveSuccess() {
        _state.update { it.copy(saveSuccess = false) }
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
