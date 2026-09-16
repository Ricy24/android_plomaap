package com.example.plomaap.data.model

data class Service(
    val id: Int = 0,
    val name: String = "",
    val category: String? = "General",
    val base_price: Double = 0.0,
    val icon: String = "",
    val color: String = "",
    val description: String = "",
    val image_url: String? = null,
    val hybrid_score: Double? = null,
    val match_reasons: List<String>? = null
) {
    val price: Double get() = base_price
}
