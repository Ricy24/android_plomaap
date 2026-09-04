package com.example.plomaap.data.api

import com.example.plomaap.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth Endpoints
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/google-login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Map<String, String>>

    @GET("api/auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>

    @PATCH("api/auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<User>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Map<String, String>>

    // Services Endpoints
    @GET("api/services")
    suspend fun getServices(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("search") search: String = ""
    ): Response<ServicesResponse>

    @GET("api/services/{id}")
    suspend fun getServiceById(@Path("id") id: Int): Response<Service>

    // Appointments Endpoints
    @GET("api/appointments")
    suspend fun getAppointments(
        @Header("Authorization") token: String,
        @Query("date") date: String? = null,
        @Query("status") status: String? = null
    ): Response<AppointmentsResponse>

    @POST("api/appointments")
    suspend fun createAppointment(
        @Header("Authorization") token: String,
        @Body request: CreateAppointmentRequest
    ): Response<Appointment>

    @GET("api/appointments/{id}")
    suspend fun getAppointmentById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Appointment>

    @PATCH("api/appointments/{id}")
    suspend fun updateAppointment(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body statusMap: Map<String, String>
    ): Response<Appointment>

    // Technicians Endpoints
    @GET("api/technicians")
    suspend fun getTechnicians(
        @Query("specialty") specialty: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<TechniciansResponse>

    @GET("api/technicians/available")
    suspend fun getAvailableTechnicians(@Query("date") date: String, @Query("time") time: String): Response<List<Technician>>

    @GET("api/technicians/slots")
    suspend fun getTechnicianSlots(@Query("technician_id") id: Int, @Query("date") date: String): Response<List<String>>

    @GET("api/technicians/profile")
    suspend fun getTechnicianProfile(@Header("Authorization") token: String): Response<Technician>

    @GET("api/technicians/appointments")
    suspend fun getTechnicianAppointments(@Header("Authorization") token: String): Response<List<Appointment>>

    @PATCH("api/technicians/appointments/{id}")
    suspend fun updateTechnicianAppointment(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body statusMap: Map<String, String>
    ): Response<Appointment>

    // Admin Endpoints
    @GET("api/admin/dashboard")
    suspend fun getAdminDashboard(@Header("Authorization") token: String): Response<Any>

    @GET("api/admin/users")
    suspend fun getAdminUsers(@Header("Authorization") token: String): Response<List<User>>

    @POST("api/admin/users")
    suspend fun createAdminUser(@Header("Authorization") token: String, @Body request: RegisterRequest): Response<User>

    @PATCH("api/admin/users/{id}")
    suspend fun updateAdminUser(@Header("Authorization") token: String, @Path("id") id: Int, @Body request: Map<String, Any>): Response<User>

    @PATCH("api/admin/appointments/{id}")
    suspend fun updateAdminAppointment(@Header("Authorization") token: String, @Path("id") id: Int, @Body request: Map<String, Any>): Response<Appointment>

    @GET("api/health")
    suspend fun getHealth(): Response<Map<String, String>>
}
