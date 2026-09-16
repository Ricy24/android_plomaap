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

data class SlotsResponse(
    val slots: List<String>
)

data class CreateAppointmentResponse(
    val success: Boolean,
    val message: String?,
    val appointment: Appointment?
)
