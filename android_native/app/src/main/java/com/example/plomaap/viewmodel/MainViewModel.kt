package com.example.plomaap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plomaap.data.model.Appointment
import com.example.plomaap.data.model.Service
import com.example.plomaap.data.model.Technician
import com.example.plomaap.data.repository.DataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val services: List<Service> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    val technicians: List<Technician> = emptyList(),
    val isLoadingServices: Boolean = false,
    val isLoadingAppointments: Boolean = false,
    val isLoadingTechnicians: Boolean = false,
    val searchQuery: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DataRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun loadServices(search: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingServices = true, searchQuery = search)
            val services = repository.getServices(search)
            _uiState.value = _uiState.value.copy(services = services, isLoadingServices = false)
        }
    }

    fun loadAppointments(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingAppointments = true)
            val appointments = repository.getAppointments(token)
            _uiState.value = _uiState.value.copy(appointments = appointments, isLoadingAppointments = false)
        }
    }

    fun loadTechnicians() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingTechnicians = true)
            val technicians = repository.getTechnicians()
            _uiState.value = _uiState.value.copy(technicians = technicians, isLoadingTechnicians = false)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadServices(query)
    }
}
