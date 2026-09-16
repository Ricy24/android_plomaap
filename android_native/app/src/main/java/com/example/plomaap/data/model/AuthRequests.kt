package com.example.plomaap.data.model

data class LoginRequest(val email: String, val password: String)
data class GoogleLoginRequest(val credential: String)
data class RegisterRequest(val name: String, val email: String, val password: String, val role: String, val phone: String, val address: String)
data class ForgotPasswordRequest(val email: String)
data class UpdateProfileRequest(val name: String, val phone: String, val address: String, val avatar: String? = null)
data class CreateAppointmentRequest(
    val service_id: Int, 
    val date: String, 
    val time: String, 
    val notes: String = "",
    val technician_id: Int? = null,
    val address: String? = null,
    val address_reference: String? = null,
    val user_address_id: Int? = null
)
data class ResetPasswordRequest(
    val email: String,
    val token: String,
    val new_password: String
)
