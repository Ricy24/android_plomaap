package com.example.plomaap.data.repository

import com.example.plomaap.data.api.ApiClient
import com.example.plomaap.data.model.*
import retrofit2.Response

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore by preferencesDataStore(name = "plomaap_prefs")

class DataRepository(private val context: Context) {
    private val api = ApiClient.apiService
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")

    private suspend fun getToken(): String? {
        return context.dataStore.data.map { it[TOKEN_KEY] }.first()
    }

    suspend fun getServices(search: String = ""): List<Service> {
        return try {
            val response = api.getServices(search = search)
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

    suspend fun createAppointment(userId: Int, serviceId: Int, date: String, time: String, notes: String): Result<Appointment> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("No autenticado"))
            val request = CreateAppointmentRequest(serviceId, date, time, notes)
            val response = api.createAppointment("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear reserva"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppointments(token: String): List<Appointment> {
        return try {
            val response = api.getAppointments("Bearer $token")
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
}
