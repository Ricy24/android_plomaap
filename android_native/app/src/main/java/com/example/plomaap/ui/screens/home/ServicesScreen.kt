package com.example.plomaap.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.plomaap.data.model.Service
import com.example.plomaap.ui.components.home.*
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.MainViewModel

@Composable
fun ServicesScreen(
    viewModel: MainViewModel,
    onServiceClick: (Int) -> Unit,
    onNavigateToAppointments: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToDigitalHome: () -> Unit = {},
    onBookServiceWithNotes: (Int, String) -> Unit = { id, _ -> onServiceClick(id) },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLocationSheet by remember { mutableStateOf(false) }
    var showDiagnosisSheet by remember { mutableStateOf(false) }

    // Bottom Sheet de Diagnóstico Inteligente NLU (FASE 11)
    if (showDiagnosisSheet) {
        AiDiagnosisBottomSheet(
            initialQuery = uiState.searchQuery,
            diagnosis = uiState.nluDiagnosis,
            isDiagnosing = uiState.isDiagnosing,
            onDismiss = { showDiagnosisSheet = false },
            onRunDiagnosis = { query ->
                viewModel.runAiDiagnosis(query)
            },
            onBookService = { serviceId, notes ->
                showDiagnosisSheet = false
                onBookServiceWithNotes(serviceId, notes)
            }
        )
    }

    // Bottom Sheet de Selección y Creación de Ubicación
    if (showLocationSheet) {
        LocationBottomSheet(
            activeAddress = uiState.activeAddress,
            savedAddresses = uiState.savedAddresses,
            isLoading = uiState.isLoadingAddresses,
            onDismiss = { showLocationSheet = false },
            onSelectAddress = { address ->
                viewModel.selectAddress(address)
                showLocationSheet = false
            },
            onSaveNewAddress = { label, address, lat, lng, placeId, ref ->
                viewModel.saveNewAddress(
                    label = label,
                    address = address,
                    latitude = lat,
                    longitude = lng,
                    placeId = placeId,
                    reference = ref,
                    onSuccess = { showLocationSheet = false }
                )
            },
            onDeleteAddress = { id ->
                viewModel.deleteAddress(id)
            }
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        snackbarHost = {
            uiState.message?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearMessages() }) {
                            Text("OK", color = ElectricCyan)
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    HomeLoadingSkeleton()
                }

                uiState.error != null && uiState.services.isEmpty() -> {
                    HomeErrorState(
                        errorMessage = uiState.error ?: "Error de conexión",
                        onRetry = { viewModel.loadHomeData() }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 28.dp)
                    ) {
                        // 1. Header con Saludo y Selector de Ubicación
                        item {
                            HomeHeader(
                                user = uiState.user,
                                activeAddress = uiState.activeAddress,
                                onLocationClick = { showLocationSheet = true },
                                onNotificationsClick = {},
                                onProfileClick = onNavigateToProfile
                            )
                        }

                        // 2. Buscador Prominente con Debounce
                        item {
                            HomeSearchBar(
                                query = uiState.searchQuery,
                                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                                isSearching = uiState.isSearching
                            )
                        }

                        // Botón de Diagnóstico Inteligente con IA (FASE 11 NLU)
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 4.dp)
                                    .clickable { showDiagnosisSheet = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = SapphireBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "¿Falla difícil de explicar? Diagnostica con IA",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SapphireBlue,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = SapphireBlue, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Banner de Onboarding de Hogar Digital si no está completado
                        if (!uiState.onboardingCompleted) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 8.dp)
                                        .clickable { onNavigateToOnboarding() },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Configura tu Hogar Digital con IA",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = TextPrimary
                                            )
                                            Text(
                                                "Responde 5 preguntas rápidas para mapear tus habitaciones.",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = PrimaryBlue)
                                    }
                                }
                            }
                        } else if (uiState.homes.isNotEmpty()) {
                            // Acceso directo a Mi Hogar Digital
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 6.dp)
                                        .clickable { onNavigateToDigitalHome() },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Rounded.Home,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            val primaryHome = uiState.homes.first()
                                            Text(
                                                primaryHome.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = TextPrimary
                                            )
                                            Text(
                                                "${primaryHome.rooms_count} habitaciones • ${primaryHome.assets_count} activos técnicos",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextSecondary)
                                    }
                                }
                            }
                        }

                        // Vista principal cuando no se está buscando texto específico
                        if (uiState.searchQuery.isBlank()) {
                            // 3. Hero Visual
                            item {
                                HomeHeroBanner(
                                    onExploreClick = {
                                        viewModel.selectCategory("Todo")
                                    }
                                )
                            }

                            // 4. Categorías reales de Base de Datos
                            item {
                                HomeCategories(
                                    categories = uiState.categories,
                                    selectedCategory = uiState.selectedCategory,
                                    onSelectCategory = { viewModel.selectCategory(it) }
                                )
                            }

                            // 5. Próxima Cita (Prioritaria)
                            item {
                                NextAppointmentCard(
                                    appointment = uiState.nextAppointment,
                                    onViewAppointmentClick = { onNavigateToAppointments() },
                                    onBookServiceClick = {
                                        // Scroll o focus a servicios
                                    }
                                )
                            }

                            // 6. Repetir Servicios Solicitados Anteriormente
                            if (uiState.pastAppointments.isNotEmpty()) {
                                item {
                                    RepeatServicesSection(
                                        pastAppointments = uiState.pastAppointments,
                                        onRepeatServiceClick = { serviceId ->
                                            onServiceClick(serviceId)
                                        }
                                    )
                                }
                            }

                            // Título de la sección de servicios
                            item {
                                SectionTitle(
                                    title = if (uiState.selectedCategory.equals("Todo", ignoreCase = true)) {
                                        "Servicios populares"
                                    } else {
                                        "Servicios en ${uiState.selectedCategory}"
                                    },
                                    count = uiState.filteredServices.size
                                )
                            }
                        } else {
                            // Vista de Resultados de Búsqueda Activa (Semántica con IA)
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Búsqueda IA: \"${uiState.searchQuery}\"",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }

                                    Surface(
                                        onClick = {
                                            viewModel.runAiDiagnosis(uiState.searchQuery)
                                            showDiagnosisSheet = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = SapphireBlue.copy(alpha = 0.1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = SapphireBlue, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Diagnóstico NLU", fontSize = 11.sp, color = SapphireBlue, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // 7. Lista de Servicios Filtrados / Destacados
                        if (uiState.filteredServices.isEmpty()) {
                            item {
                                ServicesEmptyState(
                                    query = uiState.searchQuery,
                                    onClearQuery = {
                                        viewModel.onSearchQueryChanged("")
                                        viewModel.selectCategory("Todo")
                                    }
                                )
                            }
                        } else {
                            items(
                                items = uiState.filteredServices,
                                key = { it.id }
                            ) { service ->
                                val isFav = uiState.favoriteServiceIds.contains(service.id)
                                HomeServiceCard(
                                    service = service,
                                    isFavorite = isFav,
                                    onServiceClick = onServiceClick,
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = "$count disponibles",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

@Composable
private fun ServicesEmptyState(
    query: String,
    onClearQuery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(SurfaceElevatedLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SearchOff,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (query.isNotBlank()) "No encontramos servicios para \"$query\"" else "No hay servicios disponibles en esta categoría",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Intenta buscar con otras palabras clave o revisa todas nuestras categorías.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClearQuery,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue)
        ) {
            Text("Ver todos los servicios")
        }
    }
}

@Composable
private fun HomeErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFFFEBEE), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "No pudimos conectar con los servicios",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Verifica tu conexión o que el servidor esté activo.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue)
        ) {
            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

@Composable
private fun HomeLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevatedLight)
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevatedLight)
            )
        }

        // Title
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceElevatedLight)
        )

        // Search
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceElevatedLight)
        )

        // Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceElevatedLight)
        )

        // Cards
        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceElevatedLight)
            )
        }
    }
}
