package com.example.plomaap.data.model

data class TechnicianProfile(
    val available: Boolean = true,
    val bio: String = "",
    val rating: Double = 0.0,
    val specialties: List<String> = emptyList()
)

data class Technician(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val avatar: String? = null,
    val role: String = "",
    val address: String = "",
    val status: String = "",
    val profile: TechnicianProfile? = null
)
