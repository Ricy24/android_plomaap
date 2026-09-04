package com.example.plomaap.data.model

data class Appointment(
    val id: Int = 0,
    val service_name: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "",
    val technician_name: String? = null,
    val notes: String? = null,
    val price: Double = 0.0
)
