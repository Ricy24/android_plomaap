package com.example.plomaap.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.plomaap.data.api.ApiClient
import com.example.plomaap.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.Response

class DataRepository(private val context: Context) {
    private val api = ApiClient.apiService
    private val gson = Gson()
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val USER_KEY = stringPreferencesKey("user_json")
    private val ACTIVE_ADDRESS_KEY = stringPreferencesKey("active_address_json")

    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[TOKEN_KEY] }.first()
    }

    suspend fun getSavedUser(): User? {
        val json = context.dataStore.data.map { it[USER_KEY] }.first()
        return json?.let { try { gson.fromJson(it, User::class.java) } catch (_: Exception) { null } }
    }

    suspend fun getActiveAddress(): UserAddress? {
        val json = context.dataStore.data.map { it[ACTIVE_ADDRESS_KEY] }.first()
        return json?.let { try { gson.fromJson(it, UserAddress::class.java) } catch (_: Exception) { null } }
    }

    suspend fun saveActiveAddress(address: UserAddress) {
        context.dataStore.edit { it[ACTIVE_ADDRESS_KEY] = gson.toJson(address) }
    }

    suspend fun getUserAddresses(): Result<List<UserAddress>> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("No autenticado"))
            val response = api.getUserAddresses("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.addresses
                Result.success(list)
            } else {
                Result.failure(Exception("Error al cargar ubicaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAddress(
        label: String,
        address: String,
        latitude: Double? = null,
        longitude: Double? = null,
        placeId: String? = null,
        reference: String? = null,
        isDefault: Boolean = false
    ): Result<UserAddress> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("No autenticado"))
            val req = CreateAddressRequest(label, address, latitude, longitude, placeId, reference, isDefault)
            val response = api.createAddress("Bearer $token", req)
            if (response.isSuccessful && response.body()?.address != null) {
                val saved = response.body()!!.address!!
                if (isDefault) {
                    saveActiveAddress(saved)
                }
                Result.success(saved)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Error al guardar ubicación"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAddress(
        id: Int,
        label: String,
        address: String,
        latitude: Double? = null,
        longitude: Double? = null,
        placeId: String? = null,
        reference: String? = null,
        isDefault: Boolean = false
    ): Result<UserAddress> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("No autenticado"))
            val req = CreateAddressRequest(label, address, latitude, longitude, placeId, reference, isDefault)
            val response = api.updateAddress("Bearer $token", id, req)
            if (response.isSuccessful && response.body()?.address != null) {
                val updated = response.body()!!.address!!
                if (isDefault) {
                    saveActiveAddress(updated)
                }
                Result.success(updated)
            } else {
                Result.failure(Exception("Error al actualizar ubicación"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAddress(id: Int): Result<Boolean> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("No autenticado"))
            val response = api.deleteAddress("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar ubicación"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setDefaultAddress(id: Int): Result<UserAddress> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("No autenticado"))
            val response = api.setDefaultAddress("Bearer $token", id)
            if (response.isSuccessful && response.body()?.address != null) {
                val addr = response.body()!!.address!!
                saveActiveAddress(addr)
                Result.success(addr)
            } else {
                Result.failure(Exception("Error al establecer dirección predeterminada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavorites(): Result<List<Int>> {
        return try {
            val token = getToken() ?: return Result.success(emptyList())
            val response = api.getFavorites("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.favorite_ids)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    suspend fun toggleFavorite(serviceId: Int): Result<Boolean> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Inicia sesión para guardar favoritos"))
            val response = api.toggleFavorite("Bearer $token", serviceId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.is_favorite)
            } else {
                Result.failure(Exception("Error al actualizar favoritos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): List<String> {
        return try {
            val response = api.getCategories()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.categories
            } else {
                listOf("Todo", "Fugas", "Destapes", "Baños", "Instalaciones")
            }
        } catch (_: Exception) {
            listOf("Todo", "Fugas", "Destapes", "Baños", "Instalaciones")
        }
    }

    suspend fun getServices(search: String = "", category: String = ""): List<Service> {
        val cleanSearch = search.trim()
        val cleanCategory = category.trim()
        
        // FASE 10: Búsqueda Híbrida Inteligente (Léxica + Semántica Vectorial + Afinidad de Activos)
        if (cleanSearch.length >= 2) {
            try {
                val catFilter = if (cleanCategory.isNotEmpty() && !cleanCategory.equals("Todo", ignoreCase = true)) cleanCategory else null
                val hybridResp = api.searchHybrid(
                    HybridSearchRequest(
                        query = cleanSearch,
                        top_k = 10,
                        category = catFilter
                    )
                )
                if (hybridResp.isSuccessful && hybridResp.body() != null) {
                    val hybridItems = hybridResp.body()!!.results
                    if (hybridItems.isNotEmpty()) {
                        return hybridItems.map { item ->
                            val s = item.service ?: Service(
                                id = item.service_id,
                                name = item.name,
                                category = item.category,
                                base_price = item.base_price,
                                icon = item.icon ?: "wrench",
                                color = item.color ?: "blue",
                                description = item.description ?: ""
                            )
                            s.copy(
                                hybrid_score = item.hybrid_score,
                                match_reasons = item.match_reasons
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                // Si falla la búsqueda híbrida directa, continuar con getServices
            }
        }

        return try {
            val response = api.getServices(search = cleanSearch, category = cleanCategory)
            if (response.isSuccessful) {
                response.body()?.services ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getService(id: Int): Result<Service> {
        return try {
            val response = api.getServiceById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar el servicio"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAppointment(
        userId: Int = 0,
        serviceId: Int,
        technicianId: Int? = null,
        date: String,
        time: String,
        notes: String = "",
        address: String? = null,
        addressReference: String? = null,
        userAddressId: Int? = null
    ): Result<Appointment> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("No autenticado"))
            val request = CreateAppointmentRequest(
                service_id = serviceId,
                date = date,
                time = time,
                notes = notes,
                technician_id = technicianId,
                address = address,
                address_reference = addressReference,
                user_address_id = userAddressId
            )
            val response = api.createAppointment("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.appointment != null) {
                    Result.success(body.appointment)
                } else {
                    Result.failure(Exception(body.message ?: "Error al crear reserva"))
                }
            } else {
                val errorMsg = try {
                    val errJson = response.errorBody()?.string()
                    val gson = com.google.gson.Gson()
                    val map = gson.fromJson(errJson, Map::class.java)
                    map["message"]?.toString() ?: map["error"]?.toString() ?: "Error al confirmar la cita"
                } catch (e: Exception) {
                    "Error al confirmar la cita (${response.code()})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailabilitySlots(serviceId: Int, date: String, technicianId: Int? = null): Result<AvailabilitySlotsResponse> {
        return try {
            val response = api.getAvailabilitySlots(serviceId = serviceId, date = date, technicianId = technicianId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("No se pudieron cargar los horarios disponibles"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableTechnicians(date: String, time: String, serviceId: Int): Result<List<Technician>> {
        return try {
            val response = api.getAvailableTechnicians(date = date, time = time, serviceId = serviceId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.technicians)
            } else {
                Result.failure(Exception("No se pudieron cargar los plomeros disponibles"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppointments(token: String? = null): List<Appointment> {
        return try {
            val realToken = if (!token.isNullOrEmpty() && token.contains(".")) token else (getToken() ?: token ?: "")
            if (realToken.isEmpty()) return emptyList()
            val response = api.getAppointments("Bearer $realToken")
            if (response.isSuccessful) {
                response.body()?.appointments ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTechnicians(): List<Technician> {
        return try {
            val response = api.getTechnicians()
            if (response.isSuccessful) {
                response.body()?.technicians ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // FASE 8: Smart Onboarding Methods
    suspend fun getOnboardingStatus(): Result<OnboardingStatusData> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("No autenticado"))
            val response = api.getOnboardingStatus("Bearer $token")
            if (response.isSuccessful && response.body()?.status != null) {
                Result.success(response.body()!!.status!!)
            } else {
                Result.failure(Exception("Error al consultar estado de onboarding"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOnboardingQuestions(): Result<List<OnboardingQuestionItem>> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("No autenticado"))
            val response = api.getOnboardingQuestions("Bearer $token")
            if (response.isSuccessful && response.body()?.data != null) {
                val questions = response.body()!!.data?.questions ?: emptyList()
                Result.success(questions)
            } else {
                Result.failure(Exception("Error al obtener preguntas de onboarding"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeOnboarding(answers: Map<String, Any>): Result<OnboardingCompleteResponse> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("No autenticado"))
            val response = api.completeOnboarding("Bearer $token", OnboardingCompleteRequest(answers))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al completar onboarding"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun skipOnboarding(): Result<Boolean> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("No autenticado"))
            val response = api.skipOnboarding("Bearer $token")
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al omitir onboarding"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // FASE 7: Digital Home Methods
    suspend fun getHomes(): Result<List<HomeResponse>> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("No autenticado"))
            val response = api.getHomes("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val rawList = response.body()!!["homes"] as? List<Map<String, Any>> ?: emptyList()
                val homes = rawList.map {
                    HomeResponse(
                        id = (it["id"] as? Number)?.toInt() ?: 0,
                        name = it["name"] as? String ?: "Mi Hogar",
                        address = it["address"] as? String,
                        property_type = it["property_type"] as? String ?: "house",
                        is_primary = it["is_primary"] as? Boolean ?: true,
                        rooms_count = (it["rooms_count"] as? Number)?.toInt() ?: 0,
                        assets_count = (it["assets_count"] as? Number)?.toInt() ?: 0,
                        created_at = it["created_at"] as? String
                    )
                }
                Result.success(homes)
            } else {
                Result.failure(Exception("Error al consultar hogares"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // FASE 11: NLU & Intent Extraction
    suspend fun understandText(text: String): Result<NluUnderstandResponse> {
        return try {
            val response = api.understandText(NluUnderstandRequest(text = text.trim()))
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.body()?.error ?: "No pudimos procesar el diagnóstico inteligente (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

