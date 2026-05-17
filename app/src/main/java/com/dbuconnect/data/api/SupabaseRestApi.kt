package com.dbuconnect.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseRestApi {
    @POST("auth/v1/token?grant_type=password")
    suspend fun login(@Body request: EmailPasswordRequest): SupabaseAuthResponse

    @POST("auth/v1/signup")
    suspend fun signUp(@Body request: SignUpRequest): SupabaseAuthResponse

    @GET("rest/v1/profiles")
    suspend fun getProfiles(
        @Query("select") select: String = "*",
        @Query("id") idFilter: String? = null,
        @Query("is_profile_complete") completeFilter: String? = null
    ): List<ProfileDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/profiles")
    suspend fun createProfile(@Body profile: ProfileDto): List<ProfileDto>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/profiles")
    suspend fun updateProfile(
        @Query("id") idFilter: String,
        @Body profile: ProfileDto
    ): List<ProfileDto>

    @GET("rest/v1/events")
    suspend fun getEvents(
        @Query("select") select: String = "*",
        @Query("order") order: String = "date_time.asc"
    ): List<EventDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/events")
    suspend fun createEvent(@Body event: EventDto): List<EventDto>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/events")
    suspend fun updateEvent(
        @Query("id") idFilter: String,
        @Body event: EventDto
    ): List<EventDto>

    @GET("rest/v1/matches")
    suspend fun getMatches(
        @Query("select") select: String = "*",
        @Query("or") userFilter: String,
        @Query("order") order: String = "created_at.desc"
    ): List<MatchDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/matches")
    suspend fun createMatch(@Body match: MatchDto): List<MatchDto>

    @GET("rest/v1/messages")
    suspend fun getMessages(
        @Query("select") select: String = "*",
        @Query("chat_id") chatFilter: String,
        @Query("order") order: String = "timestamp.asc"
    ): List<MessageDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/messages")
    suspend fun createMessage(@Body message: MessageDto): List<MessageDto>
}
