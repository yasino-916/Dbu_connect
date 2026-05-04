package com.dbuconnect.data.api

import com.dbuconnect.data.models.*
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockApiService @Inject constructor() : DBUApiService {

    private val departments = listOf(
        "Computer Science", "Electrical Engineering", "Civil Engineering",
        "Business Administration", "Medicine", "Law", "Architecture",
        "Chemistry", "Physics", "Mathematics", "Biology", "Psychology"
    )

    private val interestsList = listOf(
        "Coffee", "Sports", "Music", "Reading", "Photography", "Cooking",
        "Travel", "Gaming", "Art", "Volunteering", "Studying", "Movies",
        "Dancing", "Fitness", "Technology", "Writing", "Nature", "Debate"
    )

    private val names = listOf(
        "Abigail T.", "Bethlehem K.", "Daniel M.", "Eden S.", "Fikir A.",
        "Gelila B.", "Hanna W.", "Isaac D.", "Jerusalem N.", "Kidist L.",
        "Liya M.", "Meron G.", "Nahom T.", "Olana B.", "Ruth H.",
        "Samuel A.", "Tigist F.", "Yared K.", "Zara D.", "Abel S."
    )

    private val photoUrls = listOf(
        "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=400",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
        "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400",
        "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400",
        "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400"
    )

    private val mockProfiles = (0 until 20).map { i ->
        ProfileCard(
            id = "card_$i",
            userId = "user_$i",
            photoUrl = photoUrls[i % photoUrls.size],
            name = names[i % names.size],
            age = (18..26).random(),
            department = departments[i % departments.size],
            year = (1..5).random(),
            interests = interestsList.shuffled().take((2..5).random()),
            distance = (0.1f..5.0f).random(),
            bio = "Hey! I love meeting new people on campus.",
            intent = listOf("Friends", "Dating", "Study Buddy").random()
        )
    }

    private val mockMatches = (0 until 8).map { i ->
        Match(
            id = "match_$i",
            userAId = "current_user",
            userBId = "user_$i",
            userName = names[i % names.size],
            userPhotoUrl = photoUrls[i % photoUrls.size],
            createdAt = System.currentTimeMillis() - (i * 3600000L),
            isNew = i < 4,
            lastMessage = if (i < 5) listOf(
                "Hey! Are you going to the campus event?",
                "It was good! Yours?",
                "Hey! How was your day?",
                "Meet at library?",
                "Coffee soon?"
            )[i % 5] else null,
            lastMessageTime = if (i < 5) System.currentTimeMillis() - (i * 1800000L) else null,
            unreadCount = if (i < 3) (1..6).random() else 0,
            isOnline = i % 2 == 0
        )
    }

    private val mockMessages = listOf(
        Message("msg_1", "match_0", "current_user", "Hey! How was your day?", System.currentTimeMillis() - 600000, MessageStatus.READ),
        Message("msg_2", "match_0", "user_0", "It was good! Yours?", System.currentTimeMillis() - 540000, MessageStatus.READ),
        Message("msg_3", "match_0", "current_user", "Hey! How was your day?", System.currentTimeMillis() - 480000, MessageStatus.READ),
        Message("msg_4", "match_0", "user_0", "It was good! Yours?", System.currentTimeMillis() - 420000, MessageStatus.READ),
        Message("msg_5", "match_0", "current_user", "Hey! How was your day?", System.currentTimeMillis() - 360000, MessageStatus.SENT),
        Message("msg_6", "match_0", "user_0", "Meet at library?", System.currentTimeMillis() - 300000, MessageStatus.READ),
        Message("msg_7", "match_0", "current_user", "Hey! How was your day?", System.currentTimeMillis() - 240000, MessageStatus.SENT)
    )

    private val mockEvents = listOf(
        Event(
            id = "event_1",
            title = "Annual Engineering Social",
            description = "Join us for the Annual Engineering Social! This is a great opportunity to connect with fellow students, faculty, and local industry professionals in a relaxed setting. Enjoy free food, outdoor games, and a chance to learn about upcoming department projects.",
            dateTime = System.currentTimeMillis() + 86400000L * 3,
            endTime = System.currentTimeMillis() + 86400000L * 3 + 10800000L,
            location = "Main Quad",
            imageUrl = "https://images.unsplash.com/photo-1523580494863-6f3031224c94?w=600",
            tags = listOf("Engineering", "Social", "Networking"),
            attendeeCount = 142,
            attendeesFromDept = 15
        ),
        Event(
            id = "event_2",
            title = "Campus Coffee Meetup",
            description = "A casual coffee meetup for all students. Meet new friends, discover study partners, and enjoy great conversations over freshly brewed coffee.",
            dateTime = System.currentTimeMillis() + 86400000L * 5,
            location = "Student Center Café",
            imageUrl = "https://images.unsplash.com/photo-1511920170033-f8396924c348?w=600",
            tags = listOf("Social", "Coffee", "Casual"),
            attendeeCount = 67,
            attendeesFromDept = 8
        ),
        Event(
            id = "event_3",
            title = "Study Group: Data Structures",
            description = "Weekly study group for Data Structures and Algorithms. All CS students welcome!",
            dateTime = System.currentTimeMillis() + 86400000L * 2,
            location = "Library Room 204",
            imageUrl = "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=600",
            tags = listOf("Study", "CS", "Academic"),
            attendeeCount = 28,
            attendeesFromDept = 20
        ),
        Event(
            id = "event_4",
            title = "Sports Day: Volleyball Tournament",
            description = "Annual inter-department volleyball tournament. Form your team and compete!",
            dateTime = System.currentTimeMillis() + 86400000L * 7,
            location = "Sports Complex",
            imageUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=600",
            tags = listOf("Sports", "Tournament", "Fun"),
            attendeeCount = 200,
            attendeesFromDept = 30
        )
    )

    private fun Float.Companion.random(range: ClosedFloatingPointRange<Float>): Float {
        return range.start + Math.random().toFloat() * (range.endInclusive - range.start)
    }

    private fun ClosedFloatingPointRange<Float>.random(): Float {
        return start + Math.random().toFloat() * (endInclusive - start)
    }

    override suspend fun login(phone: String, otp: String): Result<User> {
        delay(1200)
        return Result.success(
            User(
                id = "current_user",
                name = "Dani M.",
                age = 21,
                department = "Computer Science",
                year = 3,
                bio = "Love coding and campus life!",
                photos = listOf(photoUrls[0]),
                interests = listOf("Coffee", "Sports", "Music", "Studying", "Volunteering"),
                intent = "Dating",
                phone = phone,
                isProfileComplete = true
            )
        )
    }

    override suspend fun getDiscoverCards(filters: FilterSettings): Result<List<ProfileCard>> {
        delay(800)
        return Result.success(mockProfiles)
    }

    override suspend fun likeProfile(userId: String): Result<Match?> {
        delay(500)
        // 30% chance of match
        return if (Math.random() < 0.3) {
            val match = Match(
                id = UUID.randomUUID().toString(),
                userAId = "current_user",
                userBId = userId,
                userName = mockProfiles.find { it.userId == userId }?.name ?: "Unknown",
                userPhotoUrl = mockProfiles.find { it.userId == userId }?.photoUrl ?: "",
                createdAt = System.currentTimeMillis(),
                isNew = true
            )
            Result.success(match)
        } else {
            Result.success(null)
        }
    }

    override suspend fun passProfile(userId: String): Result<Unit> {
        delay(200)
        return Result.success(Unit)
    }

    override suspend fun getMatches(): Result<List<Match>> {
        delay(600)
        return Result.success(mockMatches)
    }

    override suspend fun getMessages(chatId: String): Result<List<Message>> {
        delay(400)
        return Result.success(mockMessages.filter { it.chatId == chatId })
    }

    override suspend fun sendMessage(chatId: String, text: String): Result<Message> {
        delay(300)
        val message = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "current_user",
            text = text,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
        return Result.success(message)
    }

    override suspend fun getEvents(): Result<List<Event>> {
        delay(600)
        return Result.success(mockEvents)
    }

    override suspend fun rsvpEvent(eventId: String, status: RsvpStatus): Result<Event> {
        delay(400)
        val event = mockEvents.find { it.id == eventId }
            ?: return Result.failure(Exception("Event not found"))
        return Result.success(event.copy(rsvpStatus = status))
    }

    override suspend fun updateProfile(user: User): Result<User> {
        delay(800)
        return Result.success(user)
    }

    override suspend fun updatePrivacy(settings: PrivacySettings): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }
}
