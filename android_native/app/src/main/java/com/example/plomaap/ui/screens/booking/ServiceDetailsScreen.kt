package com.example.plomaap.ui.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.ui.components.GradientButton
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailsScreen(
    serviceId: Int,
    viewModel: BookingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSchedule: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(serviceId) {
        viewModel.selectService(serviceId)
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceLight,
                shadowElevation = 24.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                PaddingValues(horizontal = 24.dp, vertical = 16.dp).let { padding ->
                    Row(
                        modifier = Modifier.padding(padding).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Precio Base", fontSize = 12.sp, color = TextTertiary)
                            Text(
                                "$${String.format("%,.0f", uiState.selectedService?.base_price ?: 0.0)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SapphireBlue
                            )
                        }
                        GradientButton(
                            text = "Agendar Ahora",
                            onClick = onNavigateToSchedule,
                            modifier = Modifier.width(200.dp),
                            enabled = uiState.selectedService != null
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SapphireBlue)
            }
        } else if (uiState.selectedService != null) {
            val service = uiState.selectedService!!
            val iconForCategory = when (service.icon) {
                "fa-droplet" -> Icons.Rounded.WaterDrop
                "fa-wrench" -> Icons.Rounded.Build
                "fa-toilet" -> Icons.Rounded.Plumbing
                "fa-shower" -> Icons.Rounded.Bathtub
                else -> Icons.Rounded.Handyman
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Image Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(SapphireBlueLight.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(iconForCategory, contentDescription = null, tint = SapphireBlue, modifier = Modifier.size(100.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.background(SapphireBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("Servicio Premium", color = SapphireBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, null, tint = StarGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("4.9 (120 Reseñas)", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(service.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary, lineHeight = 34.sp)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Acerca del Servicio", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(service.description, fontSize = 15.sp, color = TextSecondary, lineHeight = 24.sp)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("¿Qué incluye?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val features = listOf("Técnico certificado", "Garantía de 30 días", "Diagnóstico inicial", "Herramientas especializadas")
                    features.forEach { feature ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = JadeGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(feature, fontSize = 15.sp, color = TextPrimary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.errorMessage ?: "Servicio no encontrado", color = ErrorRed)
            }
        }
    }
}
