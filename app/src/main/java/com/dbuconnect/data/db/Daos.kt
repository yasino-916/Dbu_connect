package com.dbuconnect.data.db

import androidx.room.*
import com.dbuconnect.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUser(userId: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUser(userId: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

@Dao
interface ProfileCardDao {
    @Query("SELECT * FROM profile_cards")
    fun observeCards(): Flow<List<ProfileCard>>

    @Query("SELECT * FROM profile_cards")
    suspend fun getCards(): List<ProfileCard>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<ProfileCard>)

    @Query("DELETE FROM profile_cards WHERE id = :id")
    suspend fun deleteCard(id: String)

    @Query("DELETE FROM profile_cards")
    suspend fun deleteAll()
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY createdAt DESC")
    fun observeMatches(): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun observeMatch(matchId: String): Flow<Match?>

    @Query("SELECT * FROM matches WHERE isNew = 1")
    fun observeNewMatches(): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE lastMessage IS NOT NULL ORDER BY lastMessageTime DESC")
    fun observeChats(): Flow<List<Match>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: Match)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<Match>)

    @Update
    suspend fun updateMatch(match: Match)

    @Query("DELETE FROM matches")
    suspend fun deleteAll()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun observeMessages(chatId: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)

    @Update
    suspend fun updateMessage(message: Message)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY dateTime ASC")
    fun observeEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEvent(id: String): Event?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<Event>)

    @Update
    suspend fun updateEvent(event: Event)

    @Query("DELETE FROM events")
    suspend fun deleteAll()
}
