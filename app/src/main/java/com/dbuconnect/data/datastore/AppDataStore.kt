package com.dbuconnect.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.dbuconnect.data.models.FilterSettings
import com.dbuconnect.data.models.PrivacySettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dbu_connect_prefs")

@Singleton
class AppDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Keys
    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LOGGED_IN = booleanPreferencesKey("logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val PROFILE_COMPLETE = booleanPreferencesKey("profile_complete")
        val SELECTED_INTENT = stringPreferencesKey("selected_intent")

        // Privacy
        val SHOW_DEPT = booleanPreferencesKey("show_dept")
        val SHOW_YEAR = booleanPreferencesKey("show_year")
        val HIDE_PROFILE = booleanPreferencesKey("hide_profile")

        // Filters
        val FILTER_INTENT = stringPreferencesKey("filter_intent")
        val FILTER_YEAR_MIN = intPreferencesKey("filter_year_min")
        val FILTER_YEAR_MAX = intPreferencesKey("filter_year_max")
    }

    // Onboarding
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    // Auth
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.LOGGED_IN] ?: false
    }

    val userId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_ID]
    }

    val authToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.AUTH_TOKEN]
    }

    val isProfileComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.PROFILE_COMPLETE] ?: false
    }

    val selectedIntent: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_INTENT] ?: ""
    }

    suspend fun setLoggedIn(loggedIn: Boolean, userId: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOGGED_IN] = loggedIn
            userId?.let { prefs[Keys.USER_ID] = it }
        }
    }

    suspend fun setAuthToken(token: String?) {
        context.dataStore.edit { prefs ->
            if (token.isNullOrBlank()) {
                prefs.remove(Keys.AUTH_TOKEN)
            } else {
                prefs[Keys.AUTH_TOKEN] = token
            }
        }
    }

    suspend fun setProfileComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PROFILE_COMPLETE] = complete
        }
    }

    suspend fun setSelectedIntent(intent: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_INTENT] = intent
        }
    }

    // Privacy
    val privacySettings: Flow<PrivacySettings> = context.dataStore.data.map { prefs ->
        PrivacySettings(
            showDepartment = prefs[Keys.SHOW_DEPT] ?: true,
            showYear = prefs[Keys.SHOW_YEAR] ?: true,
            hideProfile = prefs[Keys.HIDE_PROFILE] ?: false
        )
    }

    suspend fun updatePrivacySettings(settings: PrivacySettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SHOW_DEPT] = settings.showDepartment
            prefs[Keys.SHOW_YEAR] = settings.showYear
            prefs[Keys.HIDE_PROFILE] = settings.hideProfile
        }
    }

    // Filters
    val filterSettings: Flow<FilterSettings> = context.dataStore.data.map { prefs ->
        FilterSettings(
            intent = prefs[Keys.FILTER_INTENT] ?: "",
            yearRange = (prefs[Keys.FILTER_YEAR_MIN] ?: 1)..(prefs[Keys.FILTER_YEAR_MAX] ?: 5)
        )
    }

    suspend fun updateFilterSettings(settings: FilterSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FILTER_INTENT] = settings.intent
            prefs[Keys.FILTER_YEAR_MIN] = settings.yearRange.first
            prefs[Keys.FILTER_YEAR_MAX] = settings.yearRange.last
        }
    }

    // Logout
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
