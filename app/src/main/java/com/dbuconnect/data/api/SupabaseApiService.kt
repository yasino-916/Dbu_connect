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
        val auth = api.signUp(
            SignUpRequest(
                email = email,
                password = password,
                data = mapOf("name" to name, "phone" to phone)
            )
        )
        dataStore.setAuthToken(auth.accessToken)

        val authUser = auth.user ?: error("Supabase did not return a user")
        createProfileFromAuth(authUser.copy(metadata = mapOf("name" to name)), phone = phone)
    }

    override suspend fun getDiscoverCards(filters: FilterSettings): Result<List<ProfileCard>> = runCatching {
        val currentUserId = dataStore.userId.first()
        api.getProfiles(
            idFilter = currentUserId?.let { "neq.$it" },
            completeFilter = "eq.true"
        )
            .asSequence()
            .filter { profile -> filters.intent.isBlank() || profile.intent == filters.intent }
            .filter { profile -> profile.year in filters.yearRange }
            .map { it.toProfileCard() }
            .toList()
    }

    override suspend fun likeProfile(userId: String): Result<Match?> = runCatching {
        val currentUser = currentUser()
        val likedProfile = api.getProfiles(idFilter = "eq.$userId").firstOrNull()
        val shouldCreateMatch = likedProfile != null

        if (!shouldCreateMatch) {
            null
        } else {
            val match = MatchDto(
                id = UUID.randomUUID().toString(),
                userAId = currentUser.id,
                userBId = userId,
                userName = likedProfile.name,
                userPhotoUrl = likedProfile.photos.firstOrNull().orEmpty(),
                createdAt = System.currentTimeMillis(),
                isNew = true
            )
            api.createMatch(match).firstOrNull()?.toMatch()
        }
    }

    override suspend fun passProfile(userId: String): Result<Unit> = runCatching {
        Unit
    }

    override suspend fun getMatches(): Result<List<Match>> = runCatching {
        val currentUser = currentUser()
        api.getMatches(userFilter = "(user_a_id.eq.${currentUser.id},user_b_id.eq.${currentUser.id})")
            .map { it.toMatch() }
    }

    override suspend fun getMessages(chatId: String): Result<List<Message>> = runCatching {
        api.getMessages(chatFilter = "eq.$chatId").map { it.toMessage() }
    }

    override suspend fun sendMessage(chatId: String, text: String): Result<Message> = runCatching {
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

    override suspend fun getEvents(): Result<List<Event>> = runCatching {
        api.getEvents().map { it.toEvent() }
    }

    override suspend fun createEvent(event: Event): Result<Event> = runCatching {
        val eventToCreate = event.copy(
            id = event.id.ifBlank { UUID.randomUUID().toString() }
        )
        api.createEvent(eventToCreate.toEventDto()).firstOrNull()?.toEvent() ?: eventToCreate
    }

    override suspend fun rsvpEvent(eventId: String, status: RsvpStatus): Result<Event> = runCatching {
        val existing = api.getEvents().firstOrNull { it.id == eventId } ?: error("Event not found")
        val updated = existing.copy(
            rsvpStatus = status.name,
            attendeeCount = existing.attendeeCount + if (status == RsvpStatus.GOING) 1 else 0
        )
        api.updateEvent("eq.$eventId", updated).firstOrNull()?.toEvent() ?: updated.toEvent()
    }

    override suspend fun updateProfile(user: User): Result<User> = runCatching {
        api.updateProfile("eq.${user.id}", user.toProfileDto()).firstOrNull()?.toUser() ?: user
    }

    override suspend fun updatePrivacy(settings: PrivacySettings): Result<Unit> = runCatching {
        Unit
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
