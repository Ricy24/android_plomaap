package com.example.plomaap.ui.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.plomaap.data.model.Technician
import com.example.plomaap.ui.components.GradientButton
import com.example.plomaap.ui.components.booking.BookingProgressStepper
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTechnicianScreen(
    viewModel: BookingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateChangeDateTime: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadAvailableTechnicians()
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profesional Asignado",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceLight,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Profesional",
                            fontSize = 11.sp,
                            color = TextTertiary,
                            fontWeight = FontWeight.Medium
                        )
                        val techName = if (uiState.isAutoAssignTechnician) {
                            "⚡ Asignación inteligente"
                        } else {
                            uiState.selectedTechnician?.name ?: "Por seleccionar"
                        }
                        Text(
                            text = techName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SapphireBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    GradientButton(
                        text = "Continuar a Resumen",
                        onClick = onNavigateToSummary,
                        modifier = Modifier.width(180.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Stepper: Paso 4
            item {
                BookingProgressStepper(currentStep = 4)
            }

            // Título
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = "¿Quién realizará tu servicio?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Puedes dejar que PlomApp coordine al profesional más capacitado o elegirlo manualmente",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Opción 1: Asignación Inteligente (Recomendada)
                    val isAuto = uiState.isAutoAssignTechnician
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAuto) SapphireBlue.copy(alpha = 0.08f) else SurfaceLight
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isAuto) 1.5.dp else 1.dp,
                            color = if (isAuto) SapphireBlue else DividerColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setAutoAssignTechnician(true) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(if (isAuto) SapphireBlue else SurfaceElevatedLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = null,
                                    tint = if (isAuto) Color.White else SapphireBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Asignación inteligente",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "RECOMENDADO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = JadeGreen,
                                        modifier = Modifier
                                            .background(JadeGreen.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Coordinamos al mejor técnico disponible y más cercano a tu ubicación para garantizar puntualidad.",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }

                            RadioButton(
                                selected = isAuto,
                                onClick = { viewModel.setAutoAssignTechnician(true) },
                                colors = RadioButtonDefaults.colors(selectedColor = SapphireBlue)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "O selecciona un profesional específico",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            }

            // Lista de técnicos específicos
            if (uiState.isLoadingTechnicians) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SapphireBlue)
                    }
                }
            } else if (uiState.availableTechnicians.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay técnicos individuales listados para este horario. La asignación inteligente se encargará de coordinar tu visita.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(uiState.availableTechnicians, key = { it.id }) { tech ->
                    val isSelected = !uiState.isAutoAssignTechnician && uiState.selectedTechnician?.id == tech.id
                    TechnicianBookingCard(
                        technician = tech,
                        isSelected = isSelected,
                        onClick = { viewModel.selectTechnician(tech) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TechnicianBookingCard(
    technician: Technician,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SapphireBlue.copy(alpha = 0.08f) else SurfaceLight
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) SapphireBlue else DividerColor
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(SapphireBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = technician.name.firstOrNull()?.uppercase() ?: "T",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SapphireBlue
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = technician.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(StarGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Rounded.Star, null, tint = Color(0xFFF57F17), modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", technician.profile?.rating ?: 4.8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }

                Text(
                    text = technician.profile?.specialties?.joinToString(", ")?.ifBlank { "Plomería general" } ?: "Plomería general",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Técnico Verificado PlomApp",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = JadeGreen,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = SapphireBlue)
            )
        }
    }
}
