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

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Map<String, Any>>

    // Email Verification Endpoints
    @POST("api/auth/verify-email")
    suspend fun verifyEmail(@Body request: Map<String, String>): Response<Map<String, Any>>

    @POST("api/auth/resend-verification")
    suspend fun resendVerification(@Body request: Map<String, String>): Response<Map<String, Any>>

    // WebAuthn Passkeys Endpoints
    @POST("api/auth/passkey/login/options")
    suspend fun getPasskeyLoginOptions(@Body request: Map<String, String> = emptyMap()): Response<Map<String, Any>>

    @POST("api/auth/passkey/login/verify")
    suspend fun verifyPasskeyLogin(@Body credentialData: Map<String, Any>): Response<AuthResponse>

    @POST("api/auth/passkey/register/options")
    suspend fun getPasskeyRegisterOptions(
        @Header("Authorization") token: String,
        @Body request: Map<String, String> = emptyMap()
    ): Response<Map<String, Any>>

    @POST("api/auth/passkey/register/verify")
    suspend fun verifyPasskeyRegister(
        @Header("Authorization") token: String,
        @Body credentialData: Map<String, Any>
    ): Response<Map<String, Any>>

    @GET("api/auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>

    @PATCH("api/auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<User>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Map<String, String>>

    // User Saved Addresses Endpoints
    @GET("api/user/addresses")
    suspend fun getUserAddresses(@Header("Authorization") token: String): Response<AddressesResponse>

    @POST("api/user/addresses")
    suspend fun createAddress(
        @Header("Authorization") token: String,
        @Body request: CreateAddressRequest
    ): Response<AddressActionResponse>

    @PATCH("api/user/addresses/{id}")
    suspend fun updateAddress(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CreateAddressRequest
    ): Response<AddressActionResponse>

    @DELETE("api/user/addresses/{id}")
    suspend fun deleteAddress(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Map<String, Any>>

    @POST("api/user/addresses/{id}/set-default")
    suspend fun setDefaultAddress(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<AddressActionResponse>

    // Favorites Endpoints
    @GET("api/user/favorites")
    suspend fun getFavorites(@Header("Authorization") token: String): Response<FavoritesResponse>

    @POST("api/user/favorites/{serviceId}")
    suspend fun toggleFavorite(
        @Header("Authorization") token: String,
        @Path("serviceId") serviceId: Int
    ): Response<ToggleFavoriteResponse>

    // Categories
    @GET("api/services/categories")
    suspend fun getCategories(): Response<CategoriesResponse>

    // Services Endpoints
    @GET("api/services")
    suspend fun getServices(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("search") search: String = "",
        @Query("category") category: String = ""
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

    @GET("api/appointments/availability")
    suspend fun getAvailabilitySlots(
        @Query("service_id") serviceId: Int,
        @Query("date") date: String,
        @Query("technician_id") technicianId: Int? = null
    ): Response<AvailabilitySlotsResponse>

    @POST("api/appointments")
    suspend fun createAppointment(
        @Header("Authorization") token: String,
        @Body request: CreateAppointmentRequest
    ): Response<CreateAppointmentResponse>

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
    suspend fun getAvailableTechnicians(@Query("date") date: String, @Query("time") time: String? = null, @Query("service_id") serviceId: Int): Response<TechniciansResponse>

    @GET("api/technicians/slots")
    suspend fun getTechnicianSlots(@Query("technician_id") id: Int, @Query("date") date: String, @Query("service_id") serviceId: Int): Response<SlotsResponse>

    @GET("api/technicians/profile")
    suspend fun getTechnicianProfile(@Header("Authorization") token: String): Response<Technician>

    @GET("api/technicians/appointments")
    suspend fun getTechnicianAppointments(@Header("Authorization") token: String): Response<AppointmentsResponse>

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

    // FASE 7: Hogar Digital (Digital Home) Endpoints
    @GET("api/homes")
    suspend fun getHomes(
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @POST("api/homes")
    suspend fun createHome(
        @Header("Authorization") token: String,
        @Body request: CreateHomeRequest
    ): Response<Map<String, Any>>

    @GET("api/homes/{id}")
    suspend fun getHomeById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Map<String, Any>>

    @POST("api/homes/{id}/rooms")
    suspend fun createRoom(
        @Header("Authorization") token: String,
        @Path("id") homeId: Int,
        @Body request: CreateRoomRequest
    ): Response<Map<String, Any>>

    @POST("api/homes/{id}/rooms/{roomId}/assets")
    suspend fun createAsset(
        @Header("Authorization") token: String,
        @Path("id") homeId: Int,
        @Path("roomId") roomId: Int,
        @Body request: CreateAssetRequest
    ): Response<Map<String, Any>>

    @GET("api/homes/catalogs/room-types")
    suspend fun getRoomTypeCatalog(
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @GET("api/homes/catalogs/asset-types")
    suspend fun getAssetTypeCatalog(
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    // FASE 8: Smart Onboarding Endpoints
    @GET("api/onboarding/status")
    suspend fun getOnboardingStatus(
        @Header("Authorization") token: String
    ): Response<OnboardingStatusWrapper>

    @GET("api/onboarding/questions")
    suspend fun getOnboardingQuestions(
        @Header("Authorization") token: String
    ): Response<OnboardingQuestionsWrapper>

    @POST("api/onboarding/complete")
    suspend fun completeOnboarding(
        @Header("Authorization") token: String,
        @Body request: OnboardingCompleteRequest
    ): Response<OnboardingCompleteResponse>

    @POST("api/onboarding/skip")
    suspend fun skipOnboarding(
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    // FASE 9: Semantic Search & Embeddings Endpoints
    @POST("api/embeddings/search")
    suspend fun searchSemantic(
        @Body request: SemanticSearchRequest
    ): Response<SemanticSearchResponse>

    @GET("api/embeddings/provider-info")
    suspend fun getEmbeddingProviderInfo(): Response<ProviderInfoResponse>

    // FASE 10: Hybrid Search Endpoints
    @POST("api/search/hybrid")
    suspend fun searchHybrid(
        @Body request: HybridSearchRequest
    ): Response<HybridSearchResponse>

    // FASE 11: NLU & Intent Extraction Endpoints
    @POST("api/intelligence/understand")
    suspend fun understandText(
        @Body request: NluUnderstandRequest
    ): Response<NluUnderstandResponse>
}


