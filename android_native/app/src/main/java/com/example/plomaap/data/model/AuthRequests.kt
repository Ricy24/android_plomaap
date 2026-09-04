package com.example.plomaap.data.model

data class LoginRequest(val email: String, val password: String)
data class GoogleLoginRequest(val credential: String)
data class RegisterRequest(val name: String, val email: String, val password: String, val role: String, val phone: String, val address: String)
data class ForgotPasswordRequest(val email: String)
data class UpdateProfileRequest(val name: String, val phone: String, val address: String, val avatar: String? = null)
data class CreateAppointmentRequest(val service_id: Int, val date: String, val time: String, val notes: String)
