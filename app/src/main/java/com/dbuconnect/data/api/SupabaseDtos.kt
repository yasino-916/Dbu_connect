package com.dbuconnect.data.api

import com.dbuconnect.data.models.Event
import com.dbuconnect.data.models.Match
import com.dbuconnect.data.models.Message
import com.dbuconnect.data.models.MessageStatus
import com.dbuconnect.data.models.ProfileCard
import com.dbuconnect.data.models.RsvpStatus
import com.dbuconnect.data.models.User
import com.google.gson.annotations.SerializedName

data class EmailPasswordRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>
)

data class SupabaseAuthResponse(
    @SerializedName("access_token") val accessToken: String?,
    val user: SupabaseAuthUser?
)

data class SupabaseAuthUser(
    val id: String,
    val email: String?,
    @SerializedName("user_metadata") val metadata: Map<String, String>?
)

data class ProfileDto(
    val id: String,
    val name: String,
    val age: Int = 18,
    val department: String = "",
    val year: Int = 1,
    val bio: String = "",
    val photos: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val intent: String = "Friends",
    val email: String = "",
    val phone: String = "",
    @SerializedName("is_profile_complete") val isProfileComplete: Boolean = false,
    @SerializedName("is_admin") val isAdmin: Boolean = false
) {
    fun toUser(): User = User(
        id = id,
        name = name,
        age = age,
        department = department,
        year = year,
        bio = bio,
        photos = photos,
        interests = interests,
        intent = intent,
        email = email,
        phone = phone,
        isProfileComplete = isProfileComplete,
        isAdmin = isAdmin
    )

    fun toProfileCard(): ProfileCard = ProfileCard(
        id = id,
        userId = id,
        photoUrl = photos.firstOrNull().orEmpty(),
        name = name,
        age = age,
        department = department,
        year = year,
        interests = interests,
        bio = bio,
        intent = intent
    )
}

fun User.toProfileDto(): ProfileDto = ProfileDto(
    id = id,
    name = name,
    age = age,
    department = department,
    year = year,
    bio = bio,
    photos = photos,
    interests = interests,
    intent = intent,
    email = email,
    phone = phone,
    isProfileComplete = isProfileComplete,
    isAdmin = isAdmin
)

data class EventDto(
    val id: String,
    val title: String,
    val description: String,
    @SerializedName("date_time") val dateTime: Long,
    @SerializedName("end_time") val endTime: Long? = null,
    val location: String,
    @SerializedName("image_url") val imageUrl: String = "",
    val tags: List<String> = emptyList(),
    @SerializedName("rsvp_status") val rsvpStatus: String = RsvpStatus.NONE.name,
    @SerializedName("attendee_count") val attendeeCount: Int = 0,
    @SerializedName("attendees_from_dept") val attendeesFromDept: Int = 0
) {
    fun toEvent(): Event = Event(
        id = id,
        title = title,
        description = description,
        dateTime = dateTime,
        endTime = endTime,
        location = location,
        imageUrl = imageUrl,
        tags = tags,
        rsvpStatus = runCatching { RsvpStatus.valueOf(rsvpStatus) }.getOrDefault(RsvpStatus.NONE),
        attendeeCount = attendeeCount,
        attendeesFromDept = attendeesFromDept
    )
}

fun Event.toEventDto(): EventDto = EventDto(
    id = id,
    title = title,
    description = description,
    dateTime = dateTime,
    endTime = endTime,
    location = location,
    imageUrl = imageUrl,
    tags = tags,
    rsvpStatus = rsvpStatus.name,
    attendeeCount = attendeeCount,
    attendeesFromDept = attendeesFromDept
)

data class MatchDto(
    val id: String,
    @SerializedName("user_a_id") val userAId: String,
    @SerializedName("user_b_id") val userBId: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("user_photo_url") val userPhotoUrl: String = "",
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("is_new") val isNew: Boolean = true,
    @SerializedName("last_message") val lastMessage: String? = null,
    @SerializedName("last_message_time") val lastMessageTime: Long? = null,
    @SerializedName("unread_count") val unreadCount: Int = 0,
    @SerializedName("is_online") val isOnline: Boolean = false
) {
    fun toMatch(): Match = Match(
        id = id,
        userAId = userAId,
        userBId = userBId,
        userName = userName,
        userPhotoUrl = userPhotoUrl,
        createdAt = createdAt,
        isNew = isNew,
        lastMessage = lastMessage,
        lastMessageTime = lastMessageTime,
        unreadCount = unreadCount,
        isOnline = isOnline
    )
}

data class MessageDto(
    val id: String,
    @SerializedName("chat_id") val chatId: String,
    @SerializedName("sender_id") val senderId: String,
    val text: String,
    val timestamp: Long,
    val status: String = MessageStatus.SENT.name
) {
    fun toMessage(): Message = Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        status = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT)
    )
}

fun Message.toMessageDto(): MessageDto = MessageDto(
    id = id,
    chatId = chatId,
    senderId = senderId,
    text = text,
    timestamp = timestamp,
    status = status.name
)
