package com.dbuconnect.data.api

import com.dbuconnect.data.models.*
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockApiService @Inject constructor() : DBUApiService {

    private val mockProfiles = listOf(
        ProfileCard(
            id = "card_0",
            userId = "user_0",
            photoUrl = "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=400",
            name = "Gelila Birhanu",
            age = 21,
            department = "Medicine (MBBS)",
            year = 3,
            interests = listOf("Music", "Fitness", "Reading", "Coffee"),
            distance = 0.8f,
            bio = "Medical student at Asrat Woldeyes campus. Usually at the library studying or listening to classic music. Let's grab hot tea to survive the chilly Debre Berhan weather!",
            intent = "Friends"
        ),
        ProfileCard(
            id = "card_1",
            userId = "user_1",
            photoUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400",
            name = "Abel Tekle",
            age = 22,
            department = "Software Engineering",
            year = 4,
            interests = listOf("Technology", "Coffee", "Gaming", "Studying"),
            distance = 1.2f,
            bio = "Software Engineering senior. Usually coding at the Computing Lab or finding the best Macchiato spot on campus. Let's team up for final projects!",
            intent = "Study Buddy"
        ),
        ProfileCard(
            id = "card_2",
            userId = "user_2",
            photoUrl = "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=400",
            name = "Bethlehem Kassahun",
            age = 20,
            department = "Accounting and Finance",
            year = 2,
            interests = listOf("Reading", "Photography", "Art", "Travel"),
            distance = 0.5f,
            bio = "Sophomore studying Accounting. Love reading Ethiopian fiction and photography. Let's find beautiful spots around DBU to take cool pictures!",
            intent = "Friends"
        ),
        ProfileCard(
            id = "card_3",
            userId = "user_3",
            photoUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400",
            name = "Naod Kassahun",
            age = 23,
            department = "Civil Engineering",
            year = 5,
            interests = listOf("Sports", "Coffee", "Travel", "Movies"),
            distance = 2.1f,
            bio = "Civil Engineering graduating student. Passionate about structural design. Love football, hiking around the beautiful Debre Berhan hills, and deep conversations.",
            intent = "Dating"
        ),
        ProfileCard(
            id = "card_4",
            userId = "user_4",
            photoUrl = "https://images.unsplash.com/photo-1534751516642-a131ffd10b7f?w=400",
            name = "Senait Hailu",
            age = 22,
            department = "Law (LLB)",
            year = 4,
            interests = listOf("Volunteering", "Reading", "Debate", "Studying"),
            distance = 1.5f,
            bio = "Future lawyer. Love debating, volunteering, and learning about human rights. Looking for friends who enjoy good books and warm tea on campus.",
            intent = "Friends"
        ),
        ProfileCard(
            id = "card_5",
            userId = "user_5",
            photoUrl = "https://images.unsplash.com/photo-1489980508314-941910ded1f4?w=400",
            name = "Kirubel Worku",
            age = 21,
            department = "Computer Science",
            year = 3,
            interests = listOf("Technology", "Coffee", "Debate", "Gaming"),
            distance = 0.9f,
            bio = "Computer Science junior. Building dynamic mobile apps. Let's debate about tech or find the best spots to grab a snack near DBU main gate!",
            intent = "Study Buddy"
        ),
        ProfileCard(
            id = "card_6",
            userId = "user_6",
            photoUrl = "https://images.unsplash.com/photo-1567532939604-b6b5b0db2604?w=400",
            name = "Kidist Lemma",
            age = 20,
            department = "Environmental Science",
            year = 2,
            interests = listOf("Nature", "Volunteering", "Travel", "Movies"),
            distance = 1.1f,
            bio = "Nature lover studying Environmental Science. Passionate about green campus initiatives. Let's hang out near the DBU main quad or plant some trees!",
            intent = "Friends"
        ),
        ProfileCard(
            id = "card_7",
            userId = "user_7",
            photoUrl = "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=400",
            name = "Yared Kassaye",
            age = 22,
            department = "Mechanical Engineering",
            year = 4,
            interests = listOf("Sports", "Music", "Fitness", "Nature"),
            distance = 1.7f,
            bio = "Mechanical Engineering senior. Loves sports, playing acoustic guitar, and fitness. Always up for an outdoor running session in the cold DB morning air!",
            intent = "Dating"
        ),
        ProfileCard(
            id = "card_8",
            userId = "user_8",
            photoUrl = "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=400",
            name = "Eyerusalem Negash",
            age = 21,
            department = "Pharmacy",
            year = 4,
            interests = listOf("Music", "Cooking", "Studying", "Coffee"),
            distance = 0.6f,
            bio = "Pharmacy student. Almost done! Love baking, listening to classic Ethiopian music, and chatting about healthcare. Let's be friends!",
            intent = "Study Buddy"
        ),
        ProfileCard(
            id = "card_9",
            userId = "user_9",
            photoUrl = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=400",
            name = "Mikias Solomon",
            age = 21,
            department = "Economics",
            year = 3,
            interests = listOf("Gaming", "Movies", "Sports", "Coffee"),
            distance = 1.4f,
            bio = "Economics junior. Let's talk about finance, watch action movies, or play a quick FIFA match. Looking for chill friends around DBU!",
            intent = "Friends"
        )
    )

    private val mockMatches = listOf(
        Match(
            id = "match_0",
            userAId = "current_user",
            userBId = "user_0",
            userName = "Gelila Birhanu",
            userPhotoUrl = "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=400",
            createdAt = System.currentTimeMillis() - 3600000L,
            isNew = true,
            lastMessage = "It was good! Yours?",
            lastMessageTime = System.currentTimeMillis() - 1800000L,
            unreadCount = 2,
            isOnline = true
        ),
        Match(
            id = "match_1",
            userAId = "current_user",
            userBId = "user_1",
            userName = "Abel Tekle",
            userPhotoUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400",
            createdAt = System.currentTimeMillis() - 7200000L,
            isNew = true,
            lastMessage = "Hey! Are you going to the campus event?",
            lastMessageTime = System.currentTimeMillis() - 3600000L,
            unreadCount = 1,
            isOnline = true
        ),
        Match(
            id = "match_2",
            userAId = "current_user",
            userBId = "user_2",
            userName = "Bethlehem Kassahun",
            userPhotoUrl = "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=400",
            createdAt = System.currentTimeMillis() - 10800000L,
            isNew = false,
            lastMessage = "Meet at the main library?",
            lastMessageTime = System.currentTimeMillis() - 5400000L,
            unreadCount = 0,
            isOnline = false
        ),
        Match(
            id = "match_3",
            userAId = "current_user",
            userBId = "user_3",
            userName = "Naod Kassahun",
            userPhotoUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400",
            createdAt = System.currentTimeMillis() - 14400000L,
            isNew = false,
            lastMessage = "Coffee soon?",
            lastMessageTime = System.currentTimeMillis() - 7200000L,
            unreadCount = 0,
            isOnline = true
        )
    )

    private val mockMessages = listOf(
        Message("msg_1", "match_0", "current_user", "Hey Gelila! How was your clinic practice today?", System.currentTimeMillis() - 600000, MessageStatus.READ),
        Message("msg_2", "match_0", "user_0", "It was good! Yours?", System.currentTimeMillis() - 540000, MessageStatus.READ),
        Message("msg_3", "match_1", "user_1", "Hey! Are you going to the campus event?", System.currentTimeMillis() - 480000, MessageStatus.READ),
        Message("msg_4", "match_1", "current_user", "Yeah, I will be there in 10 minutes.", System.currentTimeMillis() - 420000, MessageStatus.READ)
    )

    private var mockEvents = listOf(
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

    override suspend fun login(email: String, password: String): Result<User> {
        delay(1200)

        val isAdmin = email.lowercase() == "admin@dbu.edu.et"

        return Result.success(
            User(
                id = if (isAdmin) "admin_user" else "current_user",
                name = if (isAdmin) "Campus Admin" else "Dani M.",
                age = 21,
                department = "Computer Science",
                year = 3,
                bio = if (isAdmin) "DBU Connect Administrator" else "Love coding and campus life!",
                photos = listOf("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400"),
                interests = listOf("Coffee", "Sports", "Music", "Studying", "Volunteering"),
                intent = "Friends",
                email = email,
                isProfileComplete = true,
                isAdmin = isAdmin
            )
        )
    }

    override suspend fun recoverPassword(recoveryEmail: String): Result<PasswordRecoveryInfo> {
        delay(500)
        // Allow any valid personal Gmail email ending with @gmail.com for seamless testing
        val isValidEmail = recoveryEmail.trim().lowercase().endsWith("@gmail.com")
        return if (isValidEmail) {
            val prefix = recoveryEmail.substringBefore("@")
            val uniEmail = "${prefix}@dbu.edu.et"
            Result.success(PasswordRecoveryInfo(
                universityEmail = uniEmail,
                recoveryEmail = recoveryEmail,
                verificationCode = "123456"
            ))
        } else {
            Result.failure(Exception("This recovery email address is not registered in our database."))
        }
    }

    override suspend fun getUniversityEmailByRecovery(recoveryEmail: String): Result<String> {
        delay(500)
        val prefix = recoveryEmail.substringBefore("@")
        return Result.success("${prefix}@dbu.edu.et")
    }

    override suspend fun getRecoveryEmail(uniEmail: String): Result<String> {
        delay(500)
        val prefix = uniEmail.substringBefore("@")
        return Result.success("${prefix}_recovery@gmail.com")
    }

    override suspend fun resetUserPassword(uniEmail: String, newPassword: String): Result<Boolean> {
        delay(500)
        return Result.success(true)
    }

    override suspend fun signUp(
        name: String,
        email: String,
        phone: String,
        password: String,
        recoveryEmail: String
    ): Result<User> {
        delay(1200)

        return Result.success(
            User(
                id = "user_${UUID.randomUUID()}",
                name = name,
                age = 18,
                department = "",
                year = 1,
                bio = "",
                photos = emptyList(),
                interests = emptyList(),
                intent = "Friends",
                email = email,
                phone = phone,
                isProfileComplete = false,
                recoveryEmail = recoveryEmail
            )
        )
    }

    override suspend fun getDiscoverCards(filters: FilterSettings): Result<List<ProfileCard>> {
        delay(800)
        val filtered = mockProfiles.filter { profile ->
            (filters.intent.isBlank() || profile.intent == filters.intent) &&
            (filters.departments.isEmpty() || profile.department in filters.departments) &&
            (profile.year in filters.yearRange)
        }
        return Result.success(filtered)
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

    override suspend fun createEvent(event: Event): Result<Event> {
        delay(800)
        val newEvent = event.copy(id = "event_${System.currentTimeMillis()}")
        val mutableEvents = mockEvents.toMutableList()
        mutableEvents.add(0, newEvent)
        mockEvents = mutableEvents
        return Result.success(newEvent)
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

    override suspend fun reportUser(userId: String, reason: String, details: String): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }

    override suspend fun blockUser(userId: String): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }

    override suspend fun markMessagesAsRead(chatId: String, currentUserId: String): Result<Unit> {
        delay(100)
        return Result.success(Unit)
    }

    override suspend fun getNotifications(): Result<List<Notification>> = Result.success(
        listOf(
            Notification(
                id = "notif_1",
                type = NotificationType.MUTUAL_LIKE,
                title = "It's a Match! 🎉",
                message = "You and Gelila liked each other! Start chatting now.",
                fromUserId = "user_0",
                fromUserName = "Gelila",
                fromUserPhoto = "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=500",
                timestamp = System.currentTimeMillis() - 600000,
                isRead = false
            ),
            Notification(
                id = "notif_2",
                type = NotificationType.NEW_MESSAGE,
                title = "Gelila",
                message = "It was good! Yours?",
                fromUserId = "user_0",
                fromUserName = "Gelila",
                fromUserPhoto = "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=500",
                timestamp = System.currentTimeMillis() - 540000,
                isRead = false
            )
        )
    )

    override suspend fun markNotificationAsReadRemote(id: String): Result<Unit> = Result.success(Unit)

    override suspend fun markAllNotificationsAsReadRemote(userId: String): Result<Unit> = Result.success(Unit)
}
