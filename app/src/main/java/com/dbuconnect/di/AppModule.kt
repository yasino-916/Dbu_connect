package com.dbuconnect.di

import android.content.Context
import androidx.room.Room
import com.dbuconnect.data.api.DBUApiService
import com.dbuconnect.data.api.MockApiService
import com.dbuconnect.data.db.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DBUDatabase {
        return Room.databaseBuilder(
            context,
            DBUDatabase::class.java,
            "dbu_connect_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(db: DBUDatabase): UserDao = db.userDao()

    @Provides
    fun provideProfileCardDao(db: DBUDatabase): ProfileCardDao = db.profileCardDao()

    @Provides
    fun provideMatchDao(db: DBUDatabase): MatchDao = db.matchDao()

    @Provides
    fun provideMessageDao(db: DBUDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideEventDao(db: DBUDatabase): EventDao = db.eventDao()

    @Provides
    @Singleton
    fun provideApiService(mockApiService: MockApiService): DBUApiService = mockApiService
}
