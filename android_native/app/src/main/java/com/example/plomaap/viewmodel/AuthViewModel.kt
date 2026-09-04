package com.example.plomaap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plomaap.data.model.User
import com.example.plomaap.data.repository.AuthRepository
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
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true, user = response.user)
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Error de autenticacion")
            }
        }
    }

    fun googleLogin() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.googleLogin("mock_google_id_token_12345")
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
