package com.dbuconnect.di

import android.content.Context
import androidx.room.Room
import com.dbuconnect.data.api.DBUApiService
import com.dbuconnect.data.api.MockApiService
import com.dbuconnect.data.api.SupabaseApiService
import com.dbuconnect.data.api.SupabaseConfig
import com.dbuconnect.data.api.SupabaseRestApi
import com.dbuconnect.data.datastore.AppDataStore
import com.dbuconnect.data.db.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Provider
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
        )
            // Issue #23: Use explicit migrations instead of destructive fallback
            .addMigrations(DBUDatabase.MIGRATION_1_2)
            .build()
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

    @Volatile
    private var cachedToken: String? = null

    @Provides
    @Singleton
    fun provideSupabaseOkHttpClient(dataStore: AppDataStore): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                // Only refresh the token if we don't have one cached
                val token = cachedToken ?: runBlocking {
                    dataStore.authToken.first().also { cachedToken = it }
                }
                val bearer = token ?: SupabaseConfig.anonKey
                val request = chain.request().newBuilder()
                    .addHeader("apikey", SupabaseConfig.anonKey)
                    .addHeader("Authorization", "Bearer $bearer")
                    .build()
                val response = chain.proceed(request)
                // If we get 401, invalidate cache so next request fetches fresh token
                if (response.code == 401) {
                    cachedToken = null
                }
                response
            }
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideSupabaseRestApi(client: OkHttpClient): SupabaseRestApi {
        val baseUrl = if (SupabaseConfig.isConfigured) SupabaseConfig.url else "https://example.supabase.co"
        return Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseRestApi::class.java)
    }

    @Provides
    @Singleton
    fun provideApiService(
        mockApiService: MockApiService,
        supabaseApiService: Provider<SupabaseApiService>
    ): DBUApiService {
        return if (SupabaseConfig.isConfigured) supabaseApiService.get() else mockApiService
    }
}
