package com.example.plomaap.data.model

import com.google.gson.annotations.SerializedName

data class HomeResponse(
    val id: Int,
    val name: String,
    val address: String? = null,
    val property_type: String? = "house",
    val is_primary: Boolean = true,
    val rooms_count: Int = 0,
    val assets_count: Int = 0,
    val created_at: String? = null
)

data class HomeDetailResponse(
    val id: Int,
    val name: String,
    val address: String? = null,
    val property_type: String? = "house",
    val is_primary: Boolean = true,
    val rooms: List<RoomResponse> = emptyList(),
    val created_at: String? = null
)

data class RoomResponse(
    val id: Int,
    val home_id: Int,
    val name: String,
    val room_type_id: Int? = null,
    val floor_number: Int? = 1,
    val assets: List<AssetResponse> = emptyList()
)

data class AssetResponse(
    val id: Int,
    val room_id: Int,
    val name: String,
    val asset_type_id: Int? = null,
    val brand: String? = null,
    val model: String? = null,
    val status: String? = "operational",
    val notes: String? = null
)

data class CreateHomeRequest(
    val name: String,
    val address: String? = null,
    val property_type: String? = "house",
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class CreateRoomRequest(
    val name: String,
    val room_type_id: Int? = null,
    val floor_number: Int? = 1
)

data class CreateAssetRequest(
    val name: String,
    val asset_type_id: Int? = null,
    val brand: String? = null,
    val model: String? = null,
    val status: String? = "operational",
    val notes: String? = null
)

data class CatalogItem(
    val id: Int,
    val code: String,
    val name: String,
    val category: String? = null,
    val icon: String? = null
)
