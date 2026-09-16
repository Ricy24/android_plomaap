package com.example.plomaap.data.model

data class UserAddress(
    val id: Int = 0,
    val user_id: Int = 0,
    val label: String = "Casa",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val place_id: String? = null,
    val reference: String? = null,
    val is_default: Boolean = false,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class AddressesResponse(
    val success: Boolean = false,
    val addresses: List<UserAddress> = emptyList(),
    val message: String? = null
)

data class AddressActionResponse(
    val success: Boolean = false,
    val message: String? = null,
    val address: UserAddress? = null
)

data class CreateAddressRequest(
    val label: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val place_id: String? = null,
    val reference: String? = null,
    val is_default: Boolean = false
)
