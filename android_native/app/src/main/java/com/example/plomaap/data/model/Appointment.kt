package com.example.plomaap.data.model

data class Appointment(
    val id: Int = 0,
    val user_id: Int = 0,
    val technician_id: Int? = null,
    val service_id: Int = 0,
    val service_name: String? = null,
    val date: String = "",
    val time: String = "",
    val status: String = "",
    val address: String? = null,
    val address_reference: String? = null,
    val user_address_id: Int? = null,
    val technician_name: String? = null,
    val technician: Technician? = null,
    val service: Service? = null,
    val notes: String? = null,
    val price: Double = 0.0
)
