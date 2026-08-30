package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.UserEntity
import com.example.data.remote.SupabaseAuthRequest
import com.example.data.remote.SupabaseClient
import com.example.data.remote.SupabaseProfileDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("calendar_cloud_session", Context.MODE_PRIVATE)

    private val _currentUserFlow = MutableStateFlow<UserEntity?>(null)
    val currentUserFlow: StateFlow<UserEntity?> = _currentUserFlow.asStateFlow()

    fun getStoredToken(): String? = prefs.getString("supabase_access_token", null)
    private fun getStoredUserId(): String? = prefs.getString("supabase_user_id", null)

    private fun saveSession(token: String?, userId: String?, email: String?) {
        prefs.edit()
            .putString("supabase_access_token", token)
            .putString("supabase_user_id", userId)
            .putString("supabase_email", email)
            .apply()
    }

    private fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun getActiveUser(): UserEntity? {
        return _currentUserFlow.value
    }

    suspend fun restoreSession(): UserEntity? {
        val token = getStoredToken()
        val userId = getStoredUserId()
        val email = prefs.getString("supabase_email", "") ?: ""

        if (token.isNullOrBlank() || userId.isNullOrBlank()) {
            _currentUserFlow.value = null
            return null
        }

        return try {
            val bearer = if (token.isNotBlank()) "Bearer $token" else "Bearer ${SupabaseClient.supabaseAnonKey}"
            val response = SupabaseClient.restApi.getProfile(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = bearer,
                idFilter = "eq.$userId"
            )

            val profile = if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                response.body()!!.first()
            } else null

            val user = UserEntity(
                id = userId,
                email = profile?.email ?: email,
                name = profile?.name ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                avatarUrl = profile?.avatarUrl ?: "",
                timezone = profile?.timezone ?: "Asia/Kolkata",
                birthDate = profile?.birthData,
                updatedAt = System.currentTimeMillis()
            )
            _currentUserFlow.value = user
            user
        } catch (e: Exception) {
            val user = UserEntity(
                id = userId,
                email = email,
                name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                timezone = "Asia/Kolkata",
                updatedAt = System.currentTimeMillis()
            )
            _currentUserFlow.value = user
            user
        }
    }

    suspend fun signUp(email: String, password: String, name: String, birthDate: String?): Result<UserEntity> {
        return try {
            val metadata = mutableMapOf<String, String>()
            if (name.isNotBlank()) metadata["name"] = name
            if (!birthDate.isNullOrBlank()) metadata["birth_data"] = birthDate

            val response = SupabaseClient.authApi.signup(
                apiKey = SupabaseClient.supabaseAnonKey,
                request = SupabaseAuthRequest(
                    email = email.trim(),
                    password = password,
                    data = metadata
                )
            )

            if (!response.isSuccessful) {
                val err = response.errorBody()?.string() ?: "Signup failed (${response.code()})"
                return Result.failure(Exception(err))
            }

            val authBody = response.body() ?: return Result.failure(Exception("Empty auth response from server"))
            val token = authBody.accessToken
            val userId = authBody.user?.id ?: UUID.randomUUID().toString()

            saveSession(token, userId, email)

            // Upsert profile into public.profiles
            val bearer = if (!token.isNullOrBlank()) "Bearer $token" else "Bearer ${SupabaseClient.supabaseAnonKey}"
            val profileDto = SupabaseProfileDto(
                id = userId,
                name = if (name.isNotBlank()) name else email.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = email,
                birthData = birthDate,
                timezone = "Asia/Kolkata"
            )

            try {
                SupabaseClient.restApi.upsertProfile(
                    apiKey = SupabaseClient.supabaseAnonKey,
                    bearerToken = bearer,
                    profile = profileDto
                )
            } catch (e: Exception) {
                // Profile table sync attempt
            }

            val userEntity = UserEntity(
                id = userId,
                email = email,
                name = profileDto.name ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                birthDate = birthDate,
                timezone = "Asia/Kolkata",
                updatedAt = System.currentTimeMillis()
            )

            _currentUserFlow.value = userEntity
            Result.success(userEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<UserEntity> {
        return try {
            val response = SupabaseClient.authApi.signin(
                apiKey = SupabaseClient.supabaseAnonKey,
                request = SupabaseAuthRequest(
                    email = email.trim(),
                    password = password
                )
            )

            if (!response.isSuccessful) {
                val err = response.errorBody()?.string() ?: "Invalid login credentials (${response.code()})"
                return Result.failure(Exception(err))
            }

            val authBody = response.body() ?: return Result.failure(Exception("Empty server response"))
            val token = authBody.accessToken
            val userId = authBody.user?.id ?: return Result.failure(Exception("No user id returned by server"))

            saveSession(token, userId, email)

            var userName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            var userBirthDate: String? = null
            var userTimezone = "Asia/Kolkata"

            val metadata = authBody.user.userMetadata
            if (metadata != null) {
                (metadata["name"] as? String)?.let { userName = it }
                (metadata["birth_data"] as? String)?.let { userBirthDate = it }
            }

            val bearer = if (!token.isNullOrBlank()) "Bearer $token" else "Bearer ${SupabaseClient.supabaseAnonKey}"
            try {
                val profileRes = SupabaseClient.restApi.getProfile(
                    apiKey = SupabaseClient.supabaseAnonKey,
                    bearerToken = bearer,
                    idFilter = "eq.$userId"
                )
                if (profileRes.isSuccessful && !profileRes.body().isNullOrEmpty()) {
                    val p = profileRes.body()!!.first()
                    p.name?.let { userName = it }
                    p.birthData?.let { userBirthDate = it }
                    p.timezone?.let { userTimezone = it }
                }
            } catch (e: Exception) {
                // Ignore profile lookup error
            }

            val userEntity = UserEntity(
                id = userId,
                email = email,
                name = userName,
                birthDate = userBirthDate,
                timezone = userTimezone,
                updatedAt = System.currentTimeMillis()
            )

            _currentUserFlow.value = userEntity
            Result.success(userEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<String> {
        return try {
            val response = SupabaseClient.authApi.recoverPassword(
                apiKey = SupabaseClient.supabaseAnonKey,
                request = SupabaseAuthRequest(email = email.trim())
            )
            if (response.isSuccessful) {
                Result.success("Password reset instructions sent to $email")
            } else {
                val err = response.errorBody()?.string() ?: "Failed to send reset link"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(name: String, birthDate: String?, timezone: String): Result<UserEntity> {
        val current = _currentUserFlow.value ?: return Result.failure(Exception("No active user session"))
        val updated = current.copy(
            name = name,
            birthDate = birthDate,
            timezone = timezone,
            updatedAt = System.currentTimeMillis()
        )

        val token = getStoredToken()
        val bearer = if (!token.isNullOrBlank()) "Bearer $token" else "Bearer ${SupabaseClient.supabaseAnonKey}"

        try {
            SupabaseClient.restApi.upsertProfile(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = bearer,
                profile = SupabaseProfileDto(
                    id = updated.id,
                    name = updated.name,
                    email = updated.email,
                    birthData = updated.birthDate,
                    timezone = updated.timezone
                )
            )
        } catch (e: Exception) {
            // Log update error if any
        }

        _currentUserFlow.value = updated
        return Result.success(updated)
    }

    suspend fun signOut() {
        val token = getStoredToken()
        if (token != null) {
            try {
                SupabaseClient.authApi.logout(
                    apiKey = SupabaseClient.supabaseAnonKey,
                    bearerToken = "Bearer $token"
                )
            } catch (ignored: Exception) {}
        }
        clearSession()
        _currentUserFlow.value = null
    }
}

