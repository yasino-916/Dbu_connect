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

    @POST("auth/v1/recover")
    suspend fun recoverPassword(@Body request: RecoverRequest)

    @GET("rest/v1/profiles")
    suspend fun getProfiles(
        @Query("select") select: String = "*",
        @Query("id") idFilter: String? = null,
        @Query("email") emailFilter: String? = null,
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

    @POST("rest/v1/rpc/get_events_for_current_user")
    suspend fun getEventsForCurrentUser(@Body request: EmptyRequest = EmptyRequest()): List<EventDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/events")
    suspend fun createEvent(@Body event: EventDto): List<EventDto>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/events")
    suspend fun updateEvent(
        @Query("id") idFilter: String,
        @Body event: EventDto
    ): List<EventDto>

    @POST("rest/v1/rpc/get_matches_for_current_user")
    suspend fun getMatchesForCurrentUser(@Body request: EmptyRequest = EmptyRequest()): List<MatchDto>

    @POST("rest/v1/rpc/like_profile")
    suspend fun likeProfile(@Body request: ProfileActionRequest): List<MatchDto>

    @POST("rest/v1/rpc/pass_profile")
    suspend fun passProfile(@Body request: ProfileActionRequest)

    @POST("rest/v1/rpc/update_privacy_settings")
    suspend fun updatePrivacy(@Body request: UpdatePrivacyRequest)

    @POST("rest/v1/rpc/rsvp_event")
    suspend fun rsvpEvent(@Body request: RsvpEventRequest): List<EventDto>

    @POST("rest/v1/rpc/block_user")
    suspend fun blockUser(@Body request: ProfileActionRequest)

    @POST("rest/v1/rpc/report_user")
    suspend fun reportUser(@Body request: ReportUserRequest)

    @POST("rest/v1/rpc/get_recovery_email")
    suspend fun getRecoveryEmail(@Body request: GetRecoveryEmailRequest): String?

    @POST("rest/v1/rpc/get_university_email_by_recovery")
    suspend fun getUniversityEmailByRecovery(@Body request: GetUniversityEmailRequest): String?

    @POST("rest/v1/rpc/reset_user_password")
    suspend fun resetUserPassword(@Body request: ResetUserPasswordRequest): Boolean

    @GET("rest/v1/messages")
    suspend fun getMessages(
        @Query("select") select: String = "*",
        @Query("chat_id") chatFilter: String,
        @Query("order") order: String = "timestamp.asc"
    ): List<MessageDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/messages")
    suspend fun createMessage(@Body message: MessageDto): List<MessageDto>

    @PATCH("rest/v1/messages")
    suspend fun updateMessagesStatus(
        @Query("chat_id") chatFilter: String,
        @Query("sender_id") senderFilter: String,
        @Body updates: Map<String, String>
    )

    @POST("rest/v1/rpc/get_notifications_for_current_user")
    suspend fun getNotifications(): List<NotificationDto>

    @PATCH("rest/v1/notifications")
    suspend fun updateNotification(
        @Query("id") idFilter: String,
        @Body updates: Map<String, Boolean>
    )

    @PATCH("rest/v1/notifications")
    suspend fun markAllNotificationsAsRead(
        @Query("user_id") userIdFilter: String,
        @Body updates: Map<String, Boolean>
    )
}
