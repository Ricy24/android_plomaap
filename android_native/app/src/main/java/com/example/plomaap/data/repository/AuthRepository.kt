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

class AuthRepository(private val context: Context) {
    private val api = ApiClient.apiService
    private val gson = Gson()

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_KEY = stringPreferencesKey("user_json")
        private val BIOMETRIC_ENABLED_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("biometric_enabled")
    }

    suspend fun saveToken(token: String) { context.dataStore.edit { it[TOKEN_KEY] = token } }
    suspend fun getToken(): String? = context.dataStore.data.map { it[TOKEN_KEY] }.first()
    suspend fun saveUser(user: User) { context.dataStore.edit { it[USER_KEY] = gson.toJson(user) } }
    suspend fun getSavedUser(): User? {
        val json = context.dataStore.data.map { it[USER_KEY] }.first()
        return json?.let { gson.fromJson(it, User::class.java) }
    }
    suspend fun setBiometricEnabled(enabled: Boolean) { context.dataStore.edit { it[BIOMETRIC_ENABLED_KEY] = enabled } }
    suspend fun isBiometricEnabled(): Boolean = context.dataStore.data.map { it[BIOMETRIC_ENABLED_KEY] ?: false }.first()
    suspend fun clearSession() { context.dataStore.edit { it.clear() } }

    private fun <T> handleResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val errorBody = response.errorBody()?.string()
            Result.failure(Exception(errorBody ?: "Error en la conexión (${response.code()})"))
        }
    }

    /** Guarda el token y usuario desde la respuesta del backend Flask */
    private suspend fun saveAuthResponse(body: AuthResponse) {
        // El backend Flask devuelve "token", no "access_token"
        val resolvedToken = body.resolvedToken()
        resolvedToken?.let { saveToken(it) }
        body.user?.let { saveUser(it) }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            val result = handleResponse(response)
            if (result.isSuccess) {
                val body = result.getOrNull()!!
                // Flask retorna success=true cuando va bien, error cuando falla
                if (body.success == false) {
                    return Result.success(body.copy(error = body.message ?: "Error de autenticación"))
                }
                saveAuthResponse(body)
            }
            result
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor. Verifica la IP y que el backend esté encendido."))
        }
    }

    suspend fun googleLogin(credential: String): Result<AuthResponse> {
        return try {
            val response = api.googleLogin(GoogleLoginRequest(credential))
            val result = handleResponse(response)
            if (result.isSuccess) {
                val body = result.getOrNull()!!
                if (body.error != null) return result
                saveAuthResponse(body)
            }
            result
        } catch (e: Exception) {
            Result.failure(Exception("Error en Google Login: ${e.message}"))
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
                if (body.success == false) {
                    return Result.success(body.copy(error = body.message ?: "Error en registro"))
                }
                saveAuthResponse(body)
            }
            result
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor. Verifica la IP del backend."))
        }
    }

    suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response = api.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                Result.success("Instrucciones enviadas a $email")
            } else {
                Result.failure(Exception("Error al solicitar recuperación"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String, token: String, newPassword: String): Result<String> {
        return try {
            val response = api.resetPassword(ResetPasswordRequest(email, token, newPassword))
            if (response.isSuccessful) {
                Result.success("Contraseña actualizada exitosamente")
            } else {
                val errorMsg = try {
                    val errJson = response.errorBody()?.string()
                    val map = gson.fromJson(errJson, Map::class.java)
                    map["message"]?.toString() ?: "Error al restablecer contraseña"
                } catch (e: Exception) {
                    "Error al restablecer contraseña"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyEmail(email: String, token: String): Result<String> {
        return try {
            val response = api.verifyEmail(mapOf("email" to email, "token" to token))
            if (response.isSuccessful) {
                // Update local cached user if available
                val savedUser = getSavedUser()
                if (savedUser != null && savedUser.email == email) {
                    saveUser(savedUser.copy(email_verified = true))
                }
                Result.success("Correo verificado exitosamente")
            } else {
                val errorMsg = try {
                    val errJson = response.errorBody()?.string()
                    val map = gson.fromJson(errJson, Map::class.java)
                    map["message"]?.toString() ?: "Error al verificar correo"
                } catch (_: Exception) {
                    "Error al verificar correo"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendVerification(email: String): Result<String> {
        return try {
            val response = api.resendVerification(mapOf("email" to email))
            if (response.isSuccessful) {
                val map = response.body()
                Result.success(map?.get("message")?.toString() ?: "Código de verificación enviado")
            } else {
                Result.failure(Exception("Error al reenviar código de verificación"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // WebAuthn Passkeys
    suspend fun getPasskeyLoginOptions(): Result<String> {
        return try {
            val response = api.getPasskeyLoginOptions()
            if (response.isSuccessful && response.body() != null) {
                Result.success(gson.toJson(response.body()))
            } else {
                Result.failure(Exception("No se pudieron obtener opciones de Passkey"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPasskeyLogin(credentialJson: String): Result<AuthResponse> {
        return try {
            val map = gson.fromJson(credentialJson, Map::class.java) as Map<String, Any>
            val response = api.verifyPasskeyLogin(map)
            val result = handleResponse(response)
            if (result.isSuccess) {
                val body = result.getOrNull()!!
                if (body.error != null) return result
                saveAuthResponse(body)
            }
            result
        } catch (e: Exception) {
            Result.failure(Exception("Error al verificar Passkey: ${e.message}"))
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
