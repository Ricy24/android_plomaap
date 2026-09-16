package com.example.plomaap.data.model

data class SemanticSearchRequest(
    val query: String,
    val entity_type: String? = "service",
    val top_k: Int = 5,
    val min_similarity: Double = 0.0
)

data class SemanticSearchResultItem(
    val embedding_id: Int,
    val entity_type: String,
    val entity_id: Int,
    val similarity: Double,
    val content_text: String,
    val model_name: String? = null,
    val entity: Service? = null
)

data class SemanticSearchResponse(
    val success: Boolean,
    val query: String,
    val total_results: Int,
    val results: List<SemanticSearchResultItem> = emptyList()
)

data class ProviderInfoResponse(
    val success: Boolean,
    val provider_class: String,
    val model_name: String,
    val dimension: Int
)

data class HybridSearchRequest(
    val query: String,
    val top_k: Int = 5,
    val category: String? = null
)

data class HybridSearchResultItem(
    val service_id: Int,
    val name: String,
    val category: String,
    val base_price: Double,
    val duration_minutes: Int? = null,
    val icon: String? = null,
    val color: String? = null,
    val description: String? = null,
    val hybrid_score: Double,
    val vector_score: Double = 0.0,
    val lexical_score: Double = 0.0,
    val match_reasons: List<String> = emptyList(),
    val service: Service? = null
)

data class HybridSearchResponse(
    val success: Boolean,
    val query: String,
    val total_results: Int,
    val detected_assets: List<String> = emptyList(),
    val detected_symptoms: List<String> = emptyList(),
    val results: List<HybridSearchResultItem> = emptyList()
)

