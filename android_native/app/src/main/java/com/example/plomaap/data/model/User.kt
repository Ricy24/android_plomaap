package com.example.plomaap.data.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val phone: String?,
    val address: String?,
    val avatar: String? = null,
    val email_verified: Boolean? = false
)

/**
 * AuthResponse acepta tanto "token" (backend Google login y login normal)
 * como "access_token" para compatibilidad futura.
 * El campo "message" captura mensajes de éxito/error del servidor Flask.
 */
data class AuthResponse(
    val token: String? = null,           // Backend Flask retorna "token"
    val access_token: String? = null,    // Alias por compatibilidad
    val user: User? = null,
    val error: String? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    /** Devuelve el token sin importar en qué campo venga */
    fun resolvedToken(): String? = token ?: access_token
}
