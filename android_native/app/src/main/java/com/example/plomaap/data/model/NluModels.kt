package com.example.plomaap.data.model

data class NluUnderstandRequest(
    val text: String
)

data class NluEntityItem(
    val type: String,
    val value: String? = null,
    val entity_id: Int? = null
)

data class NluRecommendedServiceItem(
    val id: Int,
    val name: String,
    val category: String,
    val base_price: Double,
    val reason: String
)

data class NluSuggestedAction(
    val action_type: String = "book_service",
    val target_service_id: Int = 1,
    val prefill_notes: String = "",
    val urgency_level: String = "medium"
)

data class NluUnderstandResponse(
    val success: Boolean,
    val original_text: String? = null,
    val provider: String? = null,
    val intent: String? = null,
    val category: String? = null,
    val asset: String? = null,
    val asset_id: Int? = null,
    val asset_name: String? = null,
    val issue: String? = null,
    val urgency: String? = null,
    val confidence: Double = 0.0,
    val symptoms: List<String> = emptyList(),
    val entities: List<NluEntityItem?> = emptyList(),
    val recommended_service_ids: List<Int> = emptyList(),
    val recommended_services: List<NluRecommendedServiceItem> = emptyList(),
    val suggested_action: NluSuggestedAction? = null,
    val error: String? = null
)
