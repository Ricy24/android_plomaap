package com.example.plomaap.data.model

data class ServicesResponse(
    val limit: Int,
    val offset: Int,
    val total: Int,
    val services: List<Service>
)

data class TechniciansResponse(
    val technicians: List<Technician>
)

data class AppointmentsResponse(
    val appointments: List<Appointment>
)
