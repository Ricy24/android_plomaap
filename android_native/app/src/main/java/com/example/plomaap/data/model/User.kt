package com.example.plomaap.data.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val phone: String,
    val address: String,
    val avatar: String = "https://i.pravatar.cc/300?img=68"
)

data class AuthResponse(
    val access_token: String?,
    val user: User?,
    val error: String? = null
)
