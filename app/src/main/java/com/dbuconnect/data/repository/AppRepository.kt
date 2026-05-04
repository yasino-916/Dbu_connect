package com.dbuconnect.data.repository

import com.dbuconnect.data.api.DBUApiService
import com.dbuconnect.data.datastore.AppDataStore
import com.dbuconnect.data.db.*
import com.dbuconnect.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val api: DBUApiService,
    private val dataStore: AppDataStore,
    private val userDao: UserDao,
    private val profileCardDao: ProfileCardDao,
    private val matchDao: MatchDao,
    private val messageDao: MessageDao,
    private val eventDao: EventDao
) {
    // Auth
    suspend fun login(phone: String, otp: String): Result<User> {
        val result = api.login(phone, otp)
        result.onSuccess { user ->
            userDao.insertUser(user)
            dataStore.setLoggedIn(true, user.id)
            dataStore.setProfileComplete(user.isProfileComplete)
        }
        return result
    }

    suspend fun logout() {
        dataStore.clearAll()
        userDao.deleteAll()
        matchDao.deleteAll()
        messageDao.deleteAll()
        eventDao.deleteAll()
        profileCardDao.deleteAll()
    }

    val isLoggedIn: Flow<Boolean> = dataStore.isLoggedIn
    val isProfileComplete: Flow<Boolean> = dataStore.isProfileComplete
    val onboardingCompleted: Flow<Boolean> = dataStore.onboardingCompleted

    suspend fun completeOnboarding() = dataStore.setOnboardingCompleted(true)
    suspend fun setSelectedIntent(intent: String) = dataStore.setSelectedIntent(intent)

    // Profile
    suspend fun getCurrentUser(): User? {
        val userId = dataStore.userId.first() ?: return null
        return userDao.getUser(userId)
    }

    fun observeCurrentUser(): Flow<User?> {
        return userDao.observeUser("current_user")
    }

    suspend fun updateProfile(user: User): Result<User> {
        val result = api.updateProfile(user)
        result.onSuccess {
            userDao.updateUser(it)
            dataStore.setProfileComplete(true)
        }
        return result
    }

    // Discover
    suspend fun getDiscoverCards(): Result<List<ProfileCard>> {
        val filters = dataStore.filterSettings.first()
        val result = api.getDiscoverCards(filters)
        result.onSuccess { cards ->
            profileCardDao.deleteAll()
            profileCardDao.insertCards(cards)
        }
        return result
    }

    fun observeDiscoverCards(): Flow<List<ProfileCard>> = profileCardDao.observeCards()

    suspend fun likeProfile(userId: String): Result<Match?> {
        profileCardDao.deleteCard(userId)
        val result = api.likeProfile(userId)
        result.onSuccess { match ->
            match?.let { matchDao.insertMatch(it) }
        }
        return result
    }

    suspend fun passProfile(userId: String): Result<Unit> {
        profileCardDao.deleteCard(userId)
        return api.passProfile(userId)
    }

    // Matches
    suspend fun refreshMatches(): Result<List<Match>> {
        val result = api.getMatches()
        result.onSuccess { matches ->
            matchDao.insertMatches(matches)
        }
        return result
    }

    fun observeNewMatches(): Flow<List<Match>> = matchDao.observeNewMatches()
    fun observeChats(): Flow<List<Match>> = matchDao.observeChats()
    fun observeAllMatches(): Flow<List<Match>> = matchDao.observeMatches()

    // Messages
    suspend fun refreshMessages(chatId: String): Result<List<Message>> {
        val result = api.getMessages(chatId)
        result.onSuccess { messages ->
            messageDao.insertMessages(messages)
        }
        return result
    }

    fun observeMessages(chatId: String): Flow<List<Message>> = messageDao.observeMessages(chatId)

    suspend fun sendMessage(chatId: String, text: String): Result<Message> {
        val result = api.sendMessage(chatId, text)
        result.onSuccess { message ->
            messageDao.insertMessage(message)
        }
        return result
    }

    // Events
    suspend fun refreshEvents(): Result<List<Event>> {
        val result = api.getEvents()
        result.onSuccess { events ->
            eventDao.insertEvents(events)
        }
        return result
    }

    fun observeEvents(): Flow<List<Event>> = eventDao.observeEvents()

    suspend fun rsvpEvent(eventId: String, status: RsvpStatus): Result<Event> {
        val result = api.rsvpEvent(eventId, status)
        result.onSuccess { event ->
            eventDao.updateEvent(event)
        }
        return result
    }

    // Privacy
    val privacySettings: Flow<PrivacySettings> = dataStore.privacySettings

    suspend fun updatePrivacySettings(settings: PrivacySettings) {
        dataStore.updatePrivacySettings(settings)
        api.updatePrivacy(settings)
    }

    // Filters
    val filterSettings: Flow<FilterSettings> = dataStore.filterSettings

    suspend fun updateFilterSettings(settings: FilterSettings) {
        dataStore.updateFilterSettings(settings)
    }
}
