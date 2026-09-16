package com.example.plomaap.data.model

data class FavoritesResponse(
    val success: Boolean = false,
    val favorite_ids: List<Int> = emptyList(),
    val favorites: List<Service> = emptyList()
)

data class ToggleFavoriteResponse(
    val success: Boolean = false,
    val is_favorite: Boolean = false,
    val message: String? = null,
    val service_id: Int? = null
)

data class CategoriesResponse(
    val success: Boolean = false,
    val categories: List<String> = emptyList()
)
