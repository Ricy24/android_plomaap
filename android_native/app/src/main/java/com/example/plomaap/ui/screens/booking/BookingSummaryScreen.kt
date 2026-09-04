package com.example.plomaap.ui.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.ui.components.GradientButton
import com.example.plomaap.ui.components.PremiumCard
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.BookingViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSummaryScreen(
    userId: Int?,
    viewModel: BookingViewModel,
    onNavigateBack: () -> Unit,
    onBookingSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            delay(1500)
            onBookingSuccess()
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Resumen de Reserva", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    if (!uiState.isSuccess && !uiState.isLoading) {
                        IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        bottomBar = {
            if (!uiState.isSuccess) {
                Surface(color = SurfaceLight, shadowElevation = 24.dp, modifier = Modifier.fillMaxWidth()) {
                    PaddingValues(horizontal = 24.dp, vertical = 16.dp).let { padding ->
                        GradientButton(
                            text = "Confirmar y Agendar",
                            onClick = { userId?.let { viewModel.confirmBooking(it) } },
                            isLoading = uiState.isLoading,
                            modifier = Modifier.padding(padding).fillMaxWidth()
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isSuccess) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(100.dp), tint = JadeGreen)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("¡Reserva Confirmada!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("El técnico ha sido notificado y está en camino.", fontSize = 16.sp, color = TextSecondary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (uiState.errorMessage != null) {
                        Text(uiState.errorMessage!!, color = ErrorRed, modifier = Modifier.padding(bottom = 16.dp))
                    }

                    Text("Detalles del Servicio", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(SapphireBlueLight.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Build, null, tint = SapphireBlue)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(uiState.selectedService?.name ?: "Servicio", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Premium", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Cuándo y Dónde", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CalendarToday, null, tint = SapphireBlue)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Fecha y Hora", fontSize = 12.sp, color = TextTertiary)
                                Text("${uiState.selectedDate} a las ${uiState.selectedTime}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DividerColor)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LocationOn, null, tint = SapphireBlue)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Dirección", fontSize = 12.sp, color = TextTertiary)
                                Text(uiState.locationAddress, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Resumen de Pago", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumCard(modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Subtotal", color = TextSecondary)
                            Text("$${String.format("%,.0f", uiState.selectedService?.base_price ?: 0.0)}", fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Tarifa de Servicio", color = TextSecondary)
                            Text("$1,500", fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DividerColor)
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Total Estimado", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("$${String.format("%,.0f", (uiState.selectedService?.base_price ?: 0.0) + 1500.0)}", fontWeight = FontWeight.ExtraBold, color = SapphireBlue, fontSize = 18.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
