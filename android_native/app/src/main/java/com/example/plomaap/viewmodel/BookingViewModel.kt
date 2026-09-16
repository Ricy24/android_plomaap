package com.example.plomaap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plomaap.data.model.*
import com.example.plomaap.data.repository.DataRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BookingVerificationState {
    data object Idle : BookingVerificationState()
    data class Verifying(val stepMessage: String, val progress: Float) : BookingVerificationState()
    data class Success(val appointment: Appointment) : BookingVerificationState()
    data class SlotUnavailable(val message: String) : BookingVerificationState()
    data class Error(val message: String) : BookingVerificationState()
}

data class BookingState(
    val selectedService: Service? = null,
    val selectedAddress: UserAddress? = null,
    val customAddress: String = "",
    val addressReference: String = "",
    val selectedLocation: LatLng? = null,
    val savedAddresses: List<UserAddress> = emptyList(),
    val isLoadingAddresses: Boolean = false,
    
    val selectedDate: String = "",
    val selectedTime: String = "",
    val selectedSlot: TimeSlot? = null,
    val availableSlots: List<TimeSlot> = emptyList(),
    val isLoadingSlots: Boolean = false,
    val slotsError: String? = null,

    val isAutoAssignTechnician: Boolean = true,
    val selectedTechnician: Technician? = null,
    val availableTechnicians: List<Technician> = emptyList(),
    val isLoadingTechnicians: Boolean = false,
    val techniciansError: String? = null,

    val problemDescription: String = "",
    val additionalNotes: String = "",

    val verificationState: BookingVerificationState = BookingVerificationState.Idle,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val createdAppointment: Appointment? = null
) {
    // Computed display address
    val displayAddress: String
        get() = selectedAddress?.address ?: customAddress.ifBlank { "Ubicación acordada" }

    val displayLabel: String
        get() = selectedAddress?.label ?: "Dirección personalizada"
}

class BookingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DataRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(BookingState())
    val uiState: StateFlow<BookingState> = _uiState.asStateFlow()

    init {
        loadUserAddresses()
    }

    fun loadUserAddresses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingAddresses = true)
            val cachedActive = repository.getActiveAddress()
            val result = repository.getUserAddresses()
            val addresses = result.getOrDefault(emptyList())

            val defaultAddr = cachedActive
                ?: addresses.firstOrNull { it.is_default }
                ?: addresses.firstOrNull()

            _uiState.value = _uiState.value.copy(
                savedAddresses = addresses,
                selectedAddress = if (_uiState.value.selectedAddress == null) defaultAddr else _uiState.value.selectedAddress,
                addressReference = if (_uiState.value.addressReference.isBlank() && defaultAddr?.reference != null) defaultAddr.reference else _uiState.value.addressReference,
                isLoadingAddresses = false
            )
        }
    }

    fun selectService(serviceId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getService(serviceId)
            result.onSuccess { service ->
                _uiState.value = _uiState.value.copy(selectedService = service, isLoading = false)
                // If date was already selected, reload slots for this service duration
                if (_uiState.value.selectedDate.isNotBlank()) {
                    loadAvailabilitySlots(_uiState.value.selectedDate)
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message, isLoading = false)
            }
        }
    }

    fun selectSavedAddress(address: UserAddress) {
        _uiState.value = _uiState.value.copy(
            selectedAddress = address,
            customAddress = address.address,
            addressReference = address.reference ?: _uiState.value.addressReference,
            selectedLocation = if (address.latitude != null && address.longitude != null) LatLng(address.latitude, address.longitude) else null
        )
    }

    fun selectCustomLocation(latLng: LatLng, address: String, reference: String = "") {
        _uiState.value = _uiState.value.copy(
            selectedLocation = latLng,
            customAddress = address,
            addressReference = reference.ifBlank { _uiState.value.addressReference }
        )
    }

    fun updateAddressReference(reference: String) {
        _uiState.value = _uiState.value.copy(addressReference = reference)
    }

    fun selectDate(date: String) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            selectedTime = "",
            selectedSlot = null
        )
        loadAvailabilitySlots(date)
    }

    fun loadAvailabilitySlots(date: String) {
        val serviceId = _uiState.value.selectedService?.id ?: return
        val techId = if (!_uiState.value.isAutoAssignTechnician) _uiState.value.selectedTechnician?.id else null

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingSlots = true, slotsError = null)
            val result = repository.getAvailabilitySlots(serviceId = serviceId, date = date, technicianId = techId)
            result.onSuccess { resp ->
                _uiState.value = _uiState.value.copy(
                    availableSlots = resp.slots,
                    isLoadingSlots = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoadingSlots = false,
                    slotsError = err.message ?: "No se pudieron obtener horarios disponibles"
                )
            }
        }
    }

    fun selectSlot(slot: TimeSlot) {
        _uiState.value = _uiState.value.copy(
            selectedSlot = slot,
            selectedTime = slot.start
        )
        // If date and time are picked, load available technicians for review
        loadAvailableTechnicians()
    }

    fun setAutoAssignTechnician(auto: Boolean) {
        _uiState.value = _uiState.value.copy(
            isAutoAssignTechnician = auto,
            selectedTechnician = if (auto) null else _uiState.value.selectedTechnician
        )
        // Reload slots if user switches technician
        if (_uiState.value.selectedDate.isNotBlank()) {
            loadAvailabilitySlots(_uiState.value.selectedDate)
        }
    }

    fun selectTechnician(technician: Technician) {
        _uiState.value = _uiState.value.copy(
            selectedTechnician = technician,
            isAutoAssignTechnician = false,
            errorMessage = null
        )
        if (_uiState.value.selectedDate.isNotBlank()) {
            loadAvailabilitySlots(_uiState.value.selectedDate)
        }
    }

    fun loadAvailableTechnicians() {
        val state = _uiState.value
        val serviceId = state.selectedService?.id ?: return
        val date = state.selectedDate
        val time = state.selectedTime
        if (date.isEmpty() || time.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingTechnicians = true,
                techniciansError = null
            )
            val result = repository.getAvailableTechnicians(date = date, time = time, serviceId = serviceId)
            result.onSuccess { techs ->
                _uiState.value = _uiState.value.copy(
                    availableTechnicians = techs,
                    isLoadingTechnicians = false,
                    selectedTechnician = if (!state.isAutoAssignTechnician && techs.any { it.id == state.selectedTechnician?.id }) {
                        state.selectedTechnician
                    } else if (!state.isAutoAssignTechnician && techs.isNotEmpty()) {
                        techs.first()
                    } else {
                        null
                    }
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoadingTechnicians = false,
                    techniciansError = err.message ?: "No se pudieron obtener plomeros disponibles"
                )
            }
        }
    }

    fun updateProblemDescription(desc: String) {
        _uiState.value = _uiState.value.copy(problemDescription = desc)
    }

    fun updateAdditionalNotes(notes: String) {
        _uiState.value = _uiState.value.copy(additionalNotes = notes)
    }

    fun confirmBooking(userId: Int? = null) {
        val state = _uiState.value
        val service = state.selectedService ?: return
        val date = state.selectedDate
        val time = state.selectedTime

        if (date.isBlank() || time.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Por favor selecciona fecha y horario para continuar")
            return
        }

        viewModelScope.launch {
            // Progressive verification experience
            _uiState.value = state.copy(
                verificationState = BookingVerificationState.Verifying("Verificando servicio y cobertura...", 0.25f),
                isLoading = true,
                errorMessage = null
            )
            delay(280)

            _uiState.value = state.copy(
                verificationState = BookingVerificationState.Verifying("Confirmando dirección y acceso...", 0.50f)
            )
            delay(280)

            _uiState.value = state.copy(
                verificationState = BookingVerificationState.Verifying("Validando disponibilidad en tiempo real...", 0.75f)
            )
            delay(280)

            // Compile notes
            val fullNotes = buildString {
                if (state.problemDescription.isNotBlank()) {
                    append(state.problemDescription.trim())
                }
                if (state.additionalNotes.isNotBlank()) {
                    if (isNotEmpty()) append(" | ")
                    append("Instrucciones: ${state.additionalNotes.trim()}")
                }
            }

            val finalAddress = state.displayAddress
            val finalRef = state.addressReference.ifBlank { null }
            val userAddrId = state.selectedAddress?.id?.takeIf { it > 0 }
            val techId = if (!state.isAutoAssignTechnician) state.selectedTechnician?.id else null

            val result = repository.createAppointment(
                userId = userId ?: 0,
                serviceId = service.id,
                technicianId = techId,
                date = date,
                time = time,
                notes = fullNotes,
                address = finalAddress,
                addressReference = finalRef,
                userAddressId = userAddrId
            )

            result.onSuccess { appointment ->
                _uiState.value = state.copy(
                    verificationState = BookingVerificationState.Verifying("¡Profesional asignado y solicitud confirmada!", 1.0f)
                )
                delay(350)
                _uiState.value = state.copy(
                    isLoading = false,
                    isSuccess = true,
                    createdAppointment = appointment,
                    verificationState = BookingVerificationState.Success(appointment)
                )
            }.onFailure { err ->
                val msg = err.message ?: "Ocurrió un error al agendar tu cita."
                if (msg.contains("no esta disponible", ignoreCase = true) || msg.contains("ocupado", ignoreCase = true)) {
                    _uiState.value = state.copy(
                        isLoading = false,
                        verificationState = BookingVerificationState.SlotUnavailable(msg)
                    )
                } else {
                    _uiState.value = state.copy(
                        isLoading = false,
                        errorMessage = msg,
                        verificationState = BookingVerificationState.Error(msg)
                    )
                }
            }
        }
    }

    fun dismissVerificationDialog() {
        _uiState.value = _uiState.value.copy(verificationState = BookingVerificationState.Idle)
    }

    fun clearState() {
        _uiState.value = BookingState()
        loadUserAddresses()
    }

    // Alias methods for compatibility
    fun selectDateTime(date: String, time: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date, selectedTime = time)
    }

    fun selectLocation(latLng: LatLng, address: String = "Ubicación Seleccionada") {
        selectCustomLocation(latLng, address)
    }
}
