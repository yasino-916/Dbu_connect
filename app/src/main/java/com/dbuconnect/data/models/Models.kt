package com.dbuconnect.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val age: Int,
    val department: String,
    val year: Int,
    val bio: String,
    val photos: List<String>,
    val interests: List<String>,
    val intent: String,
    val email: String = "",
    val phone: String = "",
    val isProfileComplete: Boolean = false,
    val isAdmin: Boolean = false
)

@Entity(tableName = "profile_cards")
data class ProfileCard(
    @PrimaryKey val id: String,
    val userId: String,
    val photoUrl: String,
    val name: String,
    val age: Int,
    val department: String,
    val year: Int,
    val interests: List<String>,
    val distance: Float? = null,
    val bio: String = "",
    val intent: String = ""
)

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey val id: String,
    val userAId: String,
    val userBId: String,
    val userName: String,
    val userPhotoUrl: String,
    val createdAt: Long,
    val isNew: Boolean = true,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ, FAILED
}

@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val dateTime: Long,
    val endTime: Long? = null,
    val location: String,
    val imageUrl: String = "",
    val tags: List<String>,
    val rsvpStatus: RsvpStatus = RsvpStatus.NONE,
    val attendeeCount: Int = 0,
    val attendeesFromDept: Int = 0
)

enum class RsvpStatus {
    NONE, GOING, INTERESTED, NOT_GOING
}

data class PrivacySettings(
    val showDepartment: Boolean = true,
    val showYear: Boolean = true,
    val hideProfile: Boolean = false
)

data class FilterSettings(
    val departments: List<String> = emptyList(),
    val yearRange: IntRange = 1..5,
    val intent: String = ""
)
