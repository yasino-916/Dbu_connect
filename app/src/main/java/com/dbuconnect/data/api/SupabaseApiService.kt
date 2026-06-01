package com.dbuconnect.data.api

import com.dbuconnect.data.datastore.AppDataStore
import com.dbuconnect.data.models.Event
import com.dbuconnect.data.models.FilterSettings
import com.dbuconnect.data.models.Match
import com.dbuconnect.data.models.Message
import com.dbuconnect.data.models.MessageStatus
import com.dbuconnect.data.models.PrivacySettings
import com.dbuconnect.data.models.ProfileCard
import com.dbuconnect.data.models.RsvpStatus
import com.dbuconnect.data.models.User
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseApiService @Inject constructor(
    private val api: SupabaseRestApi,
    private val dataStore: AppDataStore
) : DBUApiService {

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val auth = api.login(EmailPasswordRequest(email, password))
        dataStore.setAuthToken(auth.accessToken)

        val authUser = auth.user ?: error("Supabase did not return a user")
        api.getProfiles(idFilter = "eq.${authUser.id}")
            .firstOrNull()
            ?.toUser()
            ?: createProfileFromAuth(authUser, phone = "")
    }

    override suspend fun signUp(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Result<User> = runCatching {
        // Attempt signup
        val authResult = runCatching {
            api.signUp(
                SignUpRequest(
                    email = email,
                    password = password,
                    data = mapOf("name" to name, "phone" to phone)
                )
            )
        }
        
        // Always attempt login immediately since the DB trigger auto-confirms email
        val loginResult = runCatching {
            val auth = api.login(EmailPasswordRequest(email, password))
            dataStore.setAuthToken(auth.accessToken)
            val authUser = auth.user ?: error("Supabase did not return a user")
            api.getProfiles(idFilter = "eq.${authUser.id}")
                .firstOrNull()
                ?.toUser()
                ?: createProfileFromAuth(authUser.copy(metadata = mapOf("name" to name)), phone = phone)
        }
        
        if (loginResult.isSuccess) {
            return@runCatching loginResult.getOrThrow()
        }
        
        // If signup succeeded but login failed with invalid credentials, the email is already registered
        val loginException = loginResult.exceptionOrNull()
        if (authResult.isSuccess && loginException is retrofit2.HttpException) {
            val code = loginException.code()
            val errorBody = runCatching { loginException.response()?.errorBody()?.string() }.getOrNull()
            if (code == 400 && (errorBody?.contains("invalid_grant", ignoreCase = true) == true || 
                               errorBody?.contains("credentials", ignoreCase = true) == true)) {
                throw Exception("This email is already registered. Please sign in instead.")
            }
        }
        
        // Fall back to original exceptions
        if (authResult.isFailure) {
            throw authResult.exceptionOrNull() ?: Exception("Sign up failed")
        }
        throw loginException ?: Exception("Login failed after sign up")
    }

    override suspend fun recoverPassword(email: String): Result<Unit> = runCatching {
        // Supabase prevents email enumeration by design (returns 200 for any email).
        // We cannot reliably check if an email is registered without authentication.
        // The real protection is at login time - if the account doesn't exist,
        // the new password won't work anyway.
        api.recoverPassword(RecoverRequest(email))
    }

    override suspend fun getDiscoverCards(filters: FilterSettings): Result<List<ProfileCard>> = runCatching {
        val currentUserId = dataStore.userId.first()
        val remoteProfiles = runCatching {
            api.getProfiles(
                idFilter = currentUserId?.let { "neq.$it" },
                completeFilter = "eq.true"
            )
        }.getOrNull() ?: emptyList()

        val cards = remoteProfiles
            .asSequence()
            .filter { profile -> filters.intent.isBlank() || profile.intent == filters.intent }
            .filter { profile -> filters.departments.isEmpty() || profile.department in filters.departments }
            .filter { profile -> profile.year in filters.yearRange }
            .map { it.toProfileCard() }
            .toList()

        if (cards.isEmpty()) {
            MockApiService().getDiscoverCards(filters).getOrThrow()
        } else {
            cards
        }
    }

    override suspend fun likeProfile(userId: String): Result<Match?> = runCatching {
        if (userId.startsWith("user_")) {
            MockApiService().likeProfile(userId).getOrThrow()
        } else {
            api.likeProfile(ProfileActionRequest(userId)).firstOrNull()?.toMatch()
        }
    }

    override suspend fun passProfile(userId: String): Result<Unit> = runCatching {
        if (userId.startsWith("user_")) {
            MockApiService().passProfile(userId).getOrThrow()
        } else {
            api.passProfile(ProfileActionRequest(userId))
        }
    }

    override suspend fun getMatches(): Result<List<Match>> = runCatching {
        runCatching { api.getMatchesForCurrentUser().map { it.toMatch() } }.getOrNull() ?: emptyList()
    }

    override suspend fun getMessages(chatId: String): Result<List<Message>> = runCatching {
        if (chatId.startsWith("match_")) {
            MockApiService().getMessages(chatId).getOrThrow()
        } else {
            runCatching { api.getMessages(chatFilter = "eq.$chatId").map { it.toMessage() } }.getOrNull() ?: emptyList()
        }
    }

    override suspend fun sendMessage(chatId: String, text: String): Result<Message> = runCatching {
        if (chatId.startsWith("match_")) {
            MockApiService().sendMessage(chatId, text).getOrThrow()
        } else {
            val currentUser = currentUser()
            val message = Message(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = currentUser.id,
                text = text,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENT
            )
            api.createMessage(message.toMessageDto()).firstOrNull()?.toMessage() ?: message
        }
    }

    override suspend fun getEvents(): Result<List<Event>> = runCatching {
        val remote = runCatching { api.getEventsForCurrentUser().map { it.toEvent() } }.getOrNull() ?: emptyList()
        if (remote.isEmpty()) {
            MockApiService().getEvents().getOrThrow()
        } else {
            remote
        }
    }

    override suspend fun createEvent(event: Event): Result<Event> = runCatching {
        val eventToCreate = event.copy(
            id = event.id.ifBlank { UUID.randomUUID().toString() }
        )
        api.createEvent(eventToCreate.toEventDto()).firstOrNull()?.toEvent() ?: eventToCreate
    }

    override suspend fun rsvpEvent(eventId: String, status: RsvpStatus): Result<Event> = runCatching {
        if (eventId.startsWith("event_")) {
            MockApiService().rsvpEvent(eventId, status).getOrThrow()
        } else {
            api.rsvpEvent(RsvpEventRequest(eventId, status.name)).firstOrNull()?.toEvent()
                ?: error("Event not found")
        }
    }

    override suspend fun updateProfile(user: User): Result<User> = runCatching {
        api.updateProfile("eq.${user.id}", user.toProfileDto()).firstOrNull()?.toUser() ?: user
    }

    override suspend fun updatePrivacy(settings: PrivacySettings): Result<Unit> = runCatching {
        api.updatePrivacy(
            UpdatePrivacyRequest(
                showDepartment = settings.showDepartment,
                showYear = settings.showYear,
                hideProfile = settings.hideProfile
            )
        )
    }

    override suspend fun reportUser(userId: String, reason: String, details: String): Result<Unit> = runCatching {
        api.reportUser(ReportUserRequest(userId, reason, details))
    }

    override suspend fun blockUser(userId: String): Result<Unit> = runCatching {
        api.blockUser(ProfileActionRequest(userId))
    }

    private suspend fun createProfileFromAuth(authUser: SupabaseAuthUser, phone: String): User {
        val name = authUser.metadata?.get("name").orEmpty().ifBlank {
            authUser.email?.substringBefore("@").orEmpty()
        }
        val profile = ProfileDto(
            id = authUser.id,
            name = name,
            email = authUser.email.orEmpty(),
            phone = phone,
            isProfileComplete = false
        )

        return api.createProfile(profile).firstOrNull()?.toUser() ?: profile.toUser()
    }

    private suspend fun currentUser(): User {
        val userId = dataStore.userId.first() ?: error("No signed-in user")
        return api.getProfiles(idFilter = "eq.$userId").firstOrNull()?.toUser()
            ?: error("Signed-in user profile was not found")
    }
}
