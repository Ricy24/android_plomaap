package com.example.plomaap.data.model

data class TimeSlot(
    val start: String = "",
    val end: String = "",
    val time: String = "",
    val available: Boolean = false,
    val period: String = "morning", // morning, afternoon, evening
    val available_technicians_count: Int = 0
)

data class AvailabilitySlotsResponse(
    val date: String = "",
    val service_id: Int = 0,
    val duration_minutes: Int = 60,
    val slots: List<TimeSlot> = emptyList()
)
