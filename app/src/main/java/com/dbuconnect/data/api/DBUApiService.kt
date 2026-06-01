package com.dbuconnect.data.api

import com.dbuconnect.data.models.*

interface DBUApiService {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signUp(name: String, email: String, phone: String, password: String): Result<User>
    suspend fun getDiscoverCards(filters: FilterSettings): Result<List<ProfileCard>>
    suspend fun likeProfile(userId: String): Result<Match?>
    suspend fun passProfile(userId: String): Result<Unit>
    suspend fun getMatches(): Result<List<Match>>
    suspend fun getMessages(chatId: String): Result<List<Message>>
    suspend fun sendMessage(chatId: String, text: String): Result<Message>
    suspend fun getEvents(): Result<List<Event>>
    suspend fun createEvent(event: Event): Result<Event>
    suspend fun rsvpEvent(eventId: String, status: RsvpStatus): Result<Event>
    suspend fun updateProfile(user: User): Result<User>
    suspend fun updatePrivacy(settings: PrivacySettings): Result<Unit>
    suspend fun reportUser(userId: String, reason: String, details: String = ""): Result<Unit>
    suspend fun blockUser(userId: String): Result<Unit>
}
