package com.example.plomaap.viewmodel

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plomaap.data.model.User
import com.example.plomaap.data.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Web Client ID del proyecto Google Cloud
    private val WEB_CLIENT_ID = "664888808352-c94rm998nrabopsd3chjgkrif2081448.apps.googleusercontent.com"

    init { checkSession() }

    private fun checkSession() {
        viewModelScope.launch {
            val token = repository.getToken()
            val user = repository.getSavedUser()
            if (token != null && user != null) {
                _uiState.value = _uiState.value.copy(isLoggedIn = true, user = user)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.login(email, password)
            result.onSuccess { response ->
                if (response.error != null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.error)
                } else if (response.user != null && response.resolvedToken() != null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true, user = response.user)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Credenciales incorrectas")
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Error de conexión")
            }
        }
    }

    /**
     * Lanza el Google Sign-In con Credential Manager y obtiene el ID token real.
     * El token se envía al backend para autenticación.
     */
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // false = muestra todas las cuentas, no solo las previamente autorizadas
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    // Enviar el token real al backend
                    val authResult = repository.googleLogin(idToken)
                    authResult.onSuccess { response ->
                        if (response.error != null) {
                            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.error)
                        } else if (response.user != null && response.resolvedToken() != null) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                user = response.user
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error al procesar cuenta Google")
                        }
                    }.onFailure { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Credencial de Google no válida")
                }
            } catch (e: GetCredentialCancellationException) {
                // El usuario canceló el selector — no mostramos error
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error con Google Sign-In: ${e.message}")
            }
        }
    }

    fun register(name: String, email: String, password: String, role: String, phone: String, address: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.register(name, email, password, role, phone, address)
            result.onSuccess { response ->
                if (response.error != null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.error)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true, user = response.user)
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    /**
     * Inicia sesión utilizando WebAuthn / Passkeys a través de Android Credential Manager.
     */
    fun signInWithPasskey(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // 1. Obtener opciones y challenge del backend
                val optionsResult = repository.getPasskeyLoginOptions()
                if (optionsResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No se pudo iniciar el servicio de Passkey. Intenta con correo o Google."
                    )
                    return@launch
                }
                val requestJson = optionsResult.getOrThrow()

                // 2. Solicitar credencial biométrica / passkey al Credential Manager
                val credentialManager = CredentialManager.create(context)
                val getPasskeyOption = androidx.credentials.GetPublicKeyCredentialOption(requestJson)
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(getPasskeyOption)
                    .build()

                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential

                if (credential is androidx.credentials.PublicKeyCredential) {
                    val responseJson = credential.authenticationResponseJson
                    // 3. Verificar la aserción con el backend
                    val verifyResult = repository.verifyPasskeyLogin(responseJson)
                    verifyResult.onSuccess { response ->
                        if (response.error != null) {
                            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.error)
                        } else if (response.user != null && response.resolvedToken() != null) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                user = response.user
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error en la verificación de Passkey")
                        }
                    }.onFailure { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Error al autenticar Passkey")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Credencial no reconocida")
                }
            } catch (e: GetCredentialCancellationException) {
                // El usuario canceló
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No hay Passkeys configuradas o tu dispositivo requiere configurar huella/PIN."
                )
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = repository.forgotPassword(email)
            result.onSuccess { message ->
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = message)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun resetPassword(email: String, token: String, newPassword: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = repository.resetPassword(email, token, newPassword)
            result.onSuccess { msg ->
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = msg)
                onSuccess()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Error al restablecer contraseña")
            }
        }
    }

    fun verifyEmail(email: String, token: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = repository.verifyEmail(email, token)
            result.onSuccess { msg ->
                val updatedUser = repository.getSavedUser()
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = msg, user = updatedUser)
                onSuccess()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Error al verificar correo")
            }
        }
    }

    fun resendVerification(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = repository.resendVerification(email)
            result.onSuccess { msg ->
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = msg)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Error al reenviar código")
            }
        }
    }

    fun updateProfile(name: String, phone: String, address: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = repository.updateProfile(name, phone, address)
            result.onSuccess { message ->
                val updatedUser = repository.getSavedUser()
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = message, user = updatedUser)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
