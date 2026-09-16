package com.example.plomaap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plomaap.data.model.*
import com.example.plomaap.data.repository.DataRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MainUiState(
    val user: User? = null,
    val activeAddress: UserAddress? = null,
    val savedAddresses: List<UserAddress> = emptyList(),
    val services: List<Service> = emptyList(),
    val filteredServices: List<Service> = emptyList(),
    val favoriteServiceIds: Set<Int> = emptySet(),
    val categories: List<String> = listOf("Todo", "Fugas", "Destapes", "Baños", "Instalaciones"),
    val selectedCategory: String = "Todo",
    val searchQuery: String = "",
    val appointments: List<Appointment> = emptyList(),
    val nextAppointment: Appointment? = null,
    val pastAppointments: List<Appointment> = emptyList(),
    val technicians: List<Technician> = emptyList(),
    val onboardingCompleted: Boolean = true,
    val onboardingQuestions: List<OnboardingQuestionItem> = emptyList(),
    val homes: List<HomeResponse> = emptyList(),
    val nluDiagnosis: NluUnderstandResponse? = null,
    val isDiagnosing: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingAddresses: Boolean = false,
    val isLoadingOnboarding: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val message: String? = null
) {
    val isLoadingAppointments: Boolean get() = isLoading
}

@OptIn(FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DataRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val searchDebounceFlow = MutableStateFlow("")

    init {
        // Debounced search (300ms) to avoid spamming backend on each keystroke
        viewModelScope.launch {
            searchDebounceFlow
                .debounce(300)
                .collectLatest { query ->
                    executeSearch(query, _uiState.value.selectedCategory)
                }
        }
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // 1. Load User & Active Address
            val user = repository.getSavedUser()
            val cachedActiveAddress = repository.getActiveAddress()

            // 2. Load Addresses from backend
            val addressesResult = repository.getUserAddresses()
            val addresses = addressesResult.getOrDefault(emptyList())

            val resolvedAddress = cachedActiveAddress
                ?: addresses.firstOrNull { it.is_default }
                ?: addresses.firstOrNull()
                ?: user?.address?.let {
                    UserAddress(
                        label = "Casa",
                        address = it,
                        is_default = true
                    )
                }

            // 3. Load Services & Categories
            val services = repository.getServices()
            val categories = repository.getCategories()

            // 4. Load Favorites
            val favResult = repository.getFavorites()
            val favSet = favResult.getOrDefault(emptyList()).toSet()

            // 5. Load Appointments & Technicians
            val appointments = repository.getAppointments()
            val technicians = repository.getTechnicians()

            // Split into upcoming vs past appointments
            val (upcoming, past) = splitAppointments(appointments)

            _uiState.value = _uiState.value.copy(
                user = user,
                activeAddress = resolvedAddress,
                savedAddresses = addresses,
                services = services,
                filteredServices = services,
                categories = categories,
                favoriteServiceIds = favSet,
                appointments = appointments,
                nextAppointment = upcoming.firstOrNull(),
                pastAppointments = past,
                technicians = technicians,
                isLoading = false
            )

            // 6. Check Onboarding & Homes in parallel
            checkOnboarding()
            loadHomes()
        }
    }

    fun checkOnboarding() {
        viewModelScope.launch {
            try {
                repository.getOnboardingStatus().onSuccess { status ->
                    _uiState.value = _uiState.value.copy(onboardingCompleted = status.completed)
                    if (!status.completed) {
                        loadOnboardingQuestions()
                    }
                }
            } catch (_: Exception) {
                // Prevenir cierres si el backend aún no está listo o token vencido
            }
        }
    }

    fun loadOnboardingQuestions() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingOnboarding = true)
                repository.getOnboardingQuestions().onSuccess { questionsList ->
                    _uiState.value = _uiState.value.copy(
                        onboardingQuestions = questionsList ?: emptyList(),
                        isLoadingOnboarding = false
                    )
                }.onFailure {
                    _uiState.value = _uiState.value.copy(
                        onboardingQuestions = emptyList(),
                        isLoadingOnboarding = false
                    )
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    onboardingQuestions = emptyList(),
                    isLoadingOnboarding = false
                )
            }
        }
    }

    fun submitOnboarding(answers: Map<String, Any>, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingOnboarding = true)
            repository.completeOnboarding(answers).onSuccess {
                _uiState.value = _uiState.value.copy(
                    onboardingCompleted = true,
                    isLoadingOnboarding = false,
                    message = "¡Hogar digital configurado con éxito!"
                )
                loadHomeData()
                onDone()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoadingOnboarding = false,
                    error = err.message ?: "Error al guardar onboarding"
                )
            }
        }
    }

    fun skipOnboarding(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.skipOnboarding()
            _uiState.value = _uiState.value.copy(onboardingCompleted = true)
            onDone()
        }
    }

    fun loadHomes() {
        viewModelScope.launch {
            repository.getHomes().onSuccess { list ->
                _uiState.value = _uiState.value.copy(homes = list)
            }
        }
    }

    private fun splitAppointments(appointments: List<Appointment>): Pair<List<Appointment>, List<Appointment>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        val upcoming = mutableListOf<Appointment>()
        val past = mutableListOf<Appointment>()

        appointments.forEach { appt ->
            val isFuture = appt.date >= todayStr && appt.status != "cancelled" && appt.status != "completed"
            if (isFuture) {
                upcoming.add(appt)
            } else {
                past.add(appt)
            }
        }
        return Pair(
            upcoming.sortedWith(compareBy({ it.date }, { it.time })),
            past.sortedWith(compareByDescending<Appointment> { it.date }.thenByDescending { it.time })
        )
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchDebounceFlow.value = query
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        executeSearch(_uiState.value.searchQuery, category)
    }

    private fun executeSearch(query: String, category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            val effectiveCategory = if (category.equals("Todo", ignoreCase = true)) "" else category
            val result = repository.getServices(search = query, category = effectiveCategory)
            _uiState.value = _uiState.value.copy(
                filteredServices = result,
                isSearching = false
            )
        }
    }

    fun toggleFavorite(serviceId: Int) {
        viewModelScope.launch {
            val currentFavs = _uiState.value.favoriteServiceIds
            val isFav = currentFavs.contains(serviceId)

            // Optimistic UI update
            val updatedFavs = if (isFav) currentFavs - serviceId else currentFavs + serviceId
            _uiState.value = _uiState.value.copy(favoriteServiceIds = updatedFavs)

            val result = repository.toggleFavorite(serviceId)
            if (result.isFailure) {
                // Revert on failure
                _uiState.value = _uiState.value.copy(favoriteServiceIds = currentFavs)
            }
        }
    }

    fun selectAddress(address: UserAddress) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(activeAddress = address)
            repository.saveActiveAddress(address)
            if (address.id > 0) {
                repository.setDefaultAddress(address.id)
            }
        }
    }

    fun saveNewAddress(
        label: String,
        address: String,
        latitude: Double? = null,
        longitude: Double? = null,
        placeId: String? = null,
        reference: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingAddresses = true)
            val result = repository.createAddress(
                label = label,
                address = address,
                latitude = latitude,
                longitude = longitude,
                placeId = placeId,
                reference = reference,
                isDefault = true
            )
            result.onSuccess { newAddr ->
                val updatedList = listOf(newAddr) + _uiState.value.savedAddresses.filter { it.id != newAddr.id }
                _uiState.value = _uiState.value.copy(
                    activeAddress = newAddr,
                    savedAddresses = updatedList,
                    isLoadingAddresses = false,
                    message = "Ubicación guardada"
                )
                onSuccess()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoadingAddresses = false,
                    error = err.message ?: "No se pudo guardar la ubicación"
                )
            }
        }
    }

    fun deleteAddress(id: Int) {
        viewModelScope.launch {
            val result = repository.deleteAddress(id)
            if (result.isSuccess) {
                val updatedList = _uiState.value.savedAddresses.filter { it.id != id }
                val newActive = if (_uiState.value.activeAddress?.id == id) {
                    updatedList.firstOrNull()
                } else {
                    _uiState.value.activeAddress
                }
                newActive?.let { repository.saveActiveAddress(it) }
                _uiState.value = _uiState.value.copy(
                    savedAddresses = updatedList,
                    activeAddress = newActive
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, message = null)
    }

    // Alias methods for backward compatibility with other screens
    fun loadServices(search: String = "") { onSearchQueryChanged(search) }
    fun loadAppointments(token: String = "") { loadHomeData() }
    fun loadTechnicians() {
        viewModelScope.launch {
            val techs = repository.getTechnicians()
            _uiState.value = _uiState.value.copy(technicians = techs)
        }
    }
    fun updateSearchQuery(query: String) { onSearchQueryChanged(query) }

    // FASE 11: NLU Diagnosis
    fun runAiDiagnosis(text: String, onComplete: (NluUnderstandResponse?) -> Unit = {}) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDiagnosing = true, error = null)
            val result = repository.understandText(cleanText)
            result.onSuccess { resp ->
                _uiState.value = _uiState.value.copy(
                    isDiagnosing = false,
                    nluDiagnosis = resp
                )
                onComplete(resp)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isDiagnosing = false,
                    error = err.message ?: "No se pudo procesar el diagnóstico"
                )
                onComplete(null)
            }
        }
    }

    fun clearAiDiagnosis() {
        _uiState.value = _uiState.value.copy(nluDiagnosis = null, isDiagnosing = false)
    }
}
