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

data class RecoverRequest(
    val email: String
)

data class GetRecoveryEmailRequest(
    @SerializedName("uni_email") val uniEmail: String
)

data class GetUniversityEmailRequest(
    @SerializedName("rec_email") val recEmail: String
)

data class ResetUserPasswordRequest(
    @SerializedName("uni_email") val uniEmail: String,
    @SerializedName("new_password") val newPassword: String
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

class EmptyRequest

data class ProfileActionRequest(
    @SerializedName("target_user_id") val targetUserId: String
)

data class UpdatePrivacyRequest(
    @SerializedName("show_department") val showDepartment: Boolean,
    @SerializedName("show_year") val showYear: Boolean,
    @SerializedName("hide_profile") val hideProfile: Boolean
)

data class RsvpEventRequest(
    @SerializedName("target_event_id") val eventId: String,
    @SerializedName("new_status") val status: String
)

data class ReportUserRequest(
    @SerializedName("target_user_id") val targetUserId: String,
    @SerializedName("report_reason") val reason: String,
    @SerializedName("report_details") val details: String = ""
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
    @SerializedName("is_admin") val isAdmin: Boolean = false,
    @SerializedName("recovery_email") val recoveryEmail: String = ""
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
        isAdmin = isAdmin,
        recoveryEmail = recoveryEmail
    )

    fun toProfileCard(): ProfileCard = ProfileCard(
        id = id,
        userId = id,
        photoUrl = getValidPhotoUrl(photos.firstOrNull(), id, name),
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
    isAdmin = isAdmin,
    recoveryEmail = recoveryEmail
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
        userPhotoUrl = getValidPhotoUrl(userPhotoUrl, id, userName),
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

fun getFallbackPhotoUrl(id: String, name: String): String {
    val fallbackPhotos = listOf(
        "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=500",
        "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=500",
        "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=500",
        "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=500",
        "https://images.unsplash.com/photo-1534751516642-a131ffd10b7f?w=500",
        "https://images.unsplash.com/photo-1489980508314-941910ded1f4?w=500",
        "https://images.unsplash.com/photo-1567532939604-b6b5b0db2604?w=500",
        "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=500",
        "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=500",
        "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=500"
    )
    val hash = kotlin.math.abs(id.hashCode() + name.hashCode())
    val index = hash % fallbackPhotos.size
    return fallbackPhotos[index]
}

fun getValidPhotoUrl(url: String?, id: String, name: String): String {
    if (url.isNullOrBlank() || url.startsWith("content://") || url.startsWith("file://") || !url.startsWith("http")) {
        return getFallbackPhotoUrl(id, name)
    }
    return url
}
