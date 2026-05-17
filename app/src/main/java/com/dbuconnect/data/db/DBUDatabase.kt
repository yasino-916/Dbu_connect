package com.dbuconnect.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dbuconnect.data.models.*

@Database(
    entities = [User::class, ProfileCard::class, Match::class, Message::class, Event::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DBUDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun profileCardDao(): ProfileCardDao
    abstract fun matchDao(): MatchDao
    abstract fun messageDao(): MessageDao
    abstract fun eventDao(): EventDao
}
