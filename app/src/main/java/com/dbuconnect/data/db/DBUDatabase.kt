package com.dbuconnect.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dbuconnect.data.models.*

@Database(
    entities = [User::class, ProfileCard::class, Match::class, Message::class, Event::class, Notification::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DBUDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun profileCardDao(): ProfileCardDao
    abstract fun matchDao(): MatchDao
    abstract fun messageDao(): MessageDao
    abstract fun eventDao(): EventDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        /**
         * Issue #23: Proper migration from version 1 -> 2 instead of
         * fallbackToDestructiveMigration() which silently wipes all data.
         * Add new migrations here as the schema evolves.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // V2 added lastMessageTime to matches and status to messages.
                // These are safe ALTER TABLE operations that preserve data.
                db.execSQL(
                    "ALTER TABLE matches ADD COLUMN lastMessageTime INTEGER"
                )
                db.execSQL(
                    "ALTER TABLE messages ADD COLUMN status TEXT NOT NULL DEFAULT 'SENT'"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE users ADD COLUMN recoveryEmail TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS notifications (
                        id TEXT NOT NULL PRIMARY KEY,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        fromUserId TEXT NOT NULL DEFAULT '',
                        fromUserName TEXT NOT NULL DEFAULT '',
                        fromUserPhoto TEXT NOT NULL DEFAULT '',
                        relatedId TEXT NOT NULL DEFAULT '',
                        timestamp INTEGER NOT NULL,
                        isRead INTEGER NOT NULL DEFAULT 0
                    )"""
                )
            }
        }
    }
}
