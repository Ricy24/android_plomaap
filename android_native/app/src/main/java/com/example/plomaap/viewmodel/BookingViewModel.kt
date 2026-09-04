package com.example.plomaap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plomaap.data.model.Service
import com.example.plomaap.data.repository.DataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.android.gms.maps.model.LatLng

data class BookingState(
    val selectedService: Service? = null,
    val selectedDate: String = "",
    val selectedTime: String = "",
    val selectedLocation: LatLng? = null,
    val locationAddress: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class BookingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DataRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(BookingState())
    val uiState: StateFlow<BookingState> = _uiState.asStateFlow()

    fun selectService(serviceId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getService(serviceId)
            result.onSuccess { service ->
                _uiState.value = _uiState.value.copy(selectedService = service, isLoading = false)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message, isLoading = false)
            }
        }
    }

    fun selectDateTime(date: String, time: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date, selectedTime = time)
    }

    fun selectLocation(latLng: LatLng, address: String = "Ubicación Seleccionada") {
        _uiState.value = _uiState.value.copy(selectedLocation = latLng, locationAddress = address)
    }

    fun confirmBooking(userId: Int, notes: String = "") {
        val currentState = _uiState.value
        val serviceId = currentState.selectedService?.id ?: return
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            val result = repository.createAppointment(
                userId = userId,
                serviceId = serviceId,
                date = currentState.selectedDate,
                time = currentState.selectedTime,
                notes = notes
            )
            
            result.onSuccess {
                _uiState.value = currentState.copy(isLoading = false, isSuccess = true)
            }.onFailure { e ->
                _uiState.value = currentState.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun clearState() {
        _uiState.value = BookingState()
    }
}
