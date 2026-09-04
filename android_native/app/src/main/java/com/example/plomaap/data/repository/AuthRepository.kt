package com.example.plomaap.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.plomaap.data.api.ApiClient
import com.example.plomaap.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.Response

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "plomaap_prefs")

class AuthRepository(private val context: Context) {
    private val api = ApiClient.apiService
    private val gson = Gson()

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_KEY = stringPreferencesKey("user_json")
    }

    suspend fun saveToken(token: String) { context.dataStore.edit { it[TOKEN_KEY] = token } }
    suspend fun getToken(): String? { return context.dataStore.data.map { it[TOKEN_KEY] }.first() }
    suspend fun saveUser(user: User) { context.dataStore.edit { it[USER_KEY] = gson.toJson(user) } }
    suspend fun getSavedUser(): User? {
        val json = context.dataStore.data.map { it[USER_KEY] }.first()
        return json?.let { gson.fromJson(it, User::class.java) }
    }
    suspend fun clearSession() { context.dataStore.edit { it.clear() } }

    private fun <T> handleResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val errorBody = response.errorBody()?.string()
            Result.failure(Exception(errorBody ?: "Error en la conexion (${response.code()})"))
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            val result = handleResponse(response)
            if (result.isSuccess) {
                val body = result.getOrNull()!!
                if (body.error == null) {
                    body.access_token?.let { saveToken(it) }
                    body.user?.let { saveUser(it) }
                }
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun googleLogin(credential: String): Result<AuthResponse> {
        return try {
            val response = api.googleLogin(GoogleLoginRequest(credential))
            val result = handleResponse(response)
            if (result.isSuccess) {
                val body = result.getOrNull()!!
                if (body.error == null) {
                    body.access_token?.let { saveToken(it) }
                    body.user?.let { saveUser(it) }
                }
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String, email: String, password: String,
        role: String, phone: String, address: String
    ): Result<AuthResponse> {
        return try {
            val response = api.register(RegisterRequest(name, email, password, role, phone, address))
            val result = handleResponse(response)
            if (result.isSuccess) {
                val body = result.getOrNull()!!
                if (body.error == null) {
                    body.access_token?.let { saveToken(it) }
                    body.user?.let { saveUser(it) }
                }
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response = api.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                Result.success("Instrucciones enviadas a $email")
            } else {
                Result.failure(Exception("Error al solicitar recuperacion"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(name: String, phone: String, address: String, avatar: String? = null): Result<String> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("No autenticado"))
            val response = api.updateProfile("Bearer $token", UpdateProfileRequest(name, phone, address, avatar))
            if (response.isSuccessful && response.body() != null) {
                saveUser(response.body()!!)
                Result.success("Perfil actualizado correctamente")
            } else {
                Result.failure(Exception("Error al actualizar perfil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try { getToken()?.let { api.logout("Bearer $it") } } catch (_: Exception) {}
        clearSession()
    }
}
