package com.example.plomaap.data.model

data class OnboardingStatusWrapper(
    val success: Boolean = false,
    val user_id: Int? = null,
    val status: OnboardingStatusData? = null
)

data class OnboardingStatusData(
    val completed: Boolean = false,
    val completed_at: String? = null,
    val has_home: Boolean = false,
    val skipped: Boolean = false,
    val current_step: Int = 1
)

data class OnboardingQuestionsWrapper(
    val success: Boolean = false,
    val data: OnboardingQuestionsData? = null
)

data class OnboardingQuestionsData(
    val total_steps: Int = 5,
    val estimated_time_seconds: String? = "30-60",
    val questions: List<OnboardingQuestionItem> = emptyList()
)

data class OnboardingOptionItem(
    val code: String = "",
    val label: String = "",
    val icon: String? = null
)

data class OnboardingQuestionItem(
    val id: String = "",
    val step: Int = 1,
    val title: String = "",
    val subtitle: String? = null,
    val is_multi_select: Boolean = false,
    val options: List<OnboardingOptionItem> = emptyList()
)

data class OnboardingCompleteRequest(
    val answers: Map<String, Any>
)

data class OnboardingCompleteResponse(
    val success: Boolean = false,
    val message: String? = null,
    val home_id: Int? = null,
    val rooms_created: Int = 0,
    val assets_created: Int = 0
)
