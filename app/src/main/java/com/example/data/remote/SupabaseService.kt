package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface SupabaseAuthApi {
    @Headers("Content-Type: application/json")
    @POST("auth/v1/signup")
    suspend fun signup(
        @Header("apikey") apiKey: String,
        @Body request: SupabaseAuthRequest
    ): Response<SupabaseAuthResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    suspend fun signin(
        @Header("apikey") apiKey: String,
        @Body request: SupabaseAuthRequest
    ): Response<SupabaseAuthResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/v1/recover")
    suspend fun recoverPassword(
        @Header("apikey") apiKey: String,
        @Body request: SupabaseAuthRequest
    ): Response<Map<String, Any>>

    @Headers("Content-Type: application/json")
    @PUT("auth/v1/user")
    suspend fun updateUser(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body request: Map<String, Any?>
    ): Response<SupabaseUserDto>

    @POST("auth/v1/logout")
    suspend fun logout(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String
    ): Response<Unit>
}

interface SupabaseRestApi {
    // Profiles
    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String = "eq.",
        @Query("select") select: String = "*"
    ): Response<List<SupabaseProfileDto>>

    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    @POST("rest/v1/profiles")
    suspend fun upsertProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body profile: SupabaseProfileDto
    ): Response<List<SupabaseProfileDto>>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/profiles")
    suspend fun updateProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String,
        @Body profile: Map<String, Any?>
    ): Response<List<SupabaseProfileDto>>

    // Calendars
    @GET("rest/v1/calendars")
    suspend fun getCalendars(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("user_id") userIdFilter: String,
        @Query("select") select: String = "*"
    ): Response<List<SupabaseCalendarDto>>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/calendars")
    suspend fun createCalendar(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body calendar: SupabaseCalendarDto
    ): Response<List<SupabaseCalendarDto>>

    // Events
    @GET("rest/v1/events")
    suspend fun getEvents(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("user_id") userIdFilter: String,
        @Query("order") order: String = "start_time.asc",
        @Query("select") select: String = "*"
    ): Response<List<SupabaseEventDto>>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/events")
    suspend fun createEvent(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body event: SupabaseEventDto
    ): Response<List<SupabaseEventDto>>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/events")
    suspend fun updateEvent(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String,
        @Body event: Map<String, Any?>
    ): Response<List<SupabaseEventDto>>

    @DELETE("rest/v1/events")
    suspend fun deleteEvent(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String
    ): Response<Unit>

    // Event Reminders
    @GET("rest/v1/event_reminders")
    suspend fun getReminders(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("user_id") userIdFilter: String,
        @Query("select") select: String = "*"
    ): Response<List<SupabaseReminderDto>>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/event_reminders")
    suspend fun createReminder(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body reminder: SupabaseReminderDto
    ): Response<List<SupabaseReminderDto>>

    @DELETE("rest/v1/event_reminders")
    suspend fun deleteReminder(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("event_id") eventIdFilter: String
    ): Response<Unit>

    // Notes
    @GET("rest/v1/notes")
    suspend fun getNotes(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("user_id") userIdFilter: String,
        @Query("order") order: String = "created_at.desc",
        @Query("select") select: String = "*"
    ): Response<List<SupabaseNoteDto>>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/notes")
    suspend fun createNote(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body note: SupabaseNoteDto
    ): Response<List<SupabaseNoteDto>>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/notes")
    suspend fun updateNote(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String,
        @Body note: Map<String, Any?>
    ): Response<List<SupabaseNoteDto>>

    @DELETE("rest/v1/notes")
    suspend fun deleteNote(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String
    ): Response<Unit>
}

object SupabaseClient {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    val supabaseUrl: String
        get() {
            val url = try {
                BuildConfig.SUPABASE_URL
            } catch (e: Exception) {
                ""
            }
            return if (url.isNullOrBlank() || url.contains("your-project")) {
                "https://placeholder.supabase.co/"
            } else if (!url.endsWith("/")) {
                "$url/"
            } else {
                url
            }
        }

    val supabaseAnonKey: String
        get() {
            return try {
                BuildConfig.SUPABASE_ANON_KEY
            } catch (e: Exception) {
                ""
            }
        }

    val supabaseServiceRoleKey: String
        get() {
            return try {
                BuildConfig.SUPBASE_SERVICE_ROLE_KEY
            } catch (e: Exception) {
                ""
            }
        }

    fun isConfigured(): Boolean {
        val url = try { BuildConfig.SUPABASE_URL } catch (e: Exception) { "" }
        val key = try { BuildConfig.SUPABASE_ANON_KEY } catch (e: Exception) { "" }
        return !url.isNullOrBlank() && !url.contains("your-project") && !key.isNullOrBlank() && !key.contains("your-anon-key")
    }

    val authApi: SupabaseAuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(supabaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseAuthApi::class.java)
    }

    val restApi: SupabaseRestApi by lazy {
        Retrofit.Builder()
            .baseUrl(supabaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseRestApi::class.java)
    }
}
