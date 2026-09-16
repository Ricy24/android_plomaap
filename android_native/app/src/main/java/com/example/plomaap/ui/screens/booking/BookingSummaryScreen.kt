package com.example.plomaap.ui.screens.booking

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.plomaap.data.model.Appointment
import com.example.plomaap.ui.components.GradientButton
import com.example.plomaap.ui.components.booking.BookingProgressStepper
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.BookingVerificationState
import com.example.plomaap.viewmodel.BookingViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSummaryScreen(
    userId: Int?,
    viewModel: BookingViewModel,
    onNavigateBack: () -> Unit,
    onViewAppointments: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateChangeTechnician: () -> Unit,
    onNavigateChangeService: () -> Unit = {},
    onNavigateChangeLocation: () -> Unit = {},
    onNavigateChangeSchedule: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var problemText by remember { mutableStateOf(uiState.problemDescription) }

    val formattedPrice = remember(uiState.selectedService?.base_price) {
        try {
            val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
            format.maximumFractionDigits = 0
            format.format(uiState.selectedService?.base_price ?: 0.0)
        } catch (e: Exception) {
            "$${uiState.selectedService?.base_price?.toInt() ?: 0} COP"
        }
    }

    // Modal de Verificación en Vivo
    when (val vState = uiState.verificationState) {
        is BookingVerificationState.Verifying -> {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = SapphireBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Estamos verificando tu solicitud",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = vState.stepMessage,
                            fontSize = 13.sp,
                            color = SapphireBlue,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { vState.progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = SapphireBlue,
                            trackColor = SurfaceElevatedLight
                        )
                    }
                }
            }
        }

        is BookingVerificationState.SlotUnavailable -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissVerificationDialog() },
                icon = { Icon(Icons.Rounded.Schedule, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(32.dp)) },
                title = { Text("Horario no disponible", fontWeight = FontWeight.Bold) },
                text = { Text(vState.message, fontSize = 13.sp, color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissVerificationDialog()
                            onNavigateChangeSchedule()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue)
                    ) {
                        Text("Elegir otro horario")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissVerificationDialog() }) {
                        Text("Cerrar")
                    }
                }
            )
        }

        is BookingVerificationState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissVerificationDialog() },
                icon = { Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(32.dp)) },
                title = { Text("No pudimos completar la reserva", fontWeight = FontWeight.Bold) },
                text = { Text(vState.message, fontSize = 13.sp, color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissVerificationDialog()
                            viewModel.confirmBooking(userId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue)
                    ) {
                        Text("Intentar nuevamente")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissVerificationDialog() }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        is BookingVerificationState.Success -> {
            // Pantalla de Éxito de Reserva
            BookingSuccessContent(
                appointment = vState.appointment,
                onViewAppointments = onViewAppointments,
                onNavigateHome = onNavigateHome
            )
            return
        }

        else -> {}
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Resumen de Reserva",
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
                    Column {
                        Text(
                            text = "Total a pagar al técnico",
                            fontSize = 11.sp,
                            color = TextTertiary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formattedPrice,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SapphireBlue
                        )
                    }

                    GradientButton(
                        text = "Confirmar cita",
                        onClick = {
                            viewModel.updateProblemDescription(problemText)
                            viewModel.confirmBooking(userId)
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.width(180.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Stepper: Paso 5
            BookingProgressStepper(currentStep = 5)

            Spacer(modifier = Modifier.height(10.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Revisa los detalles de tu servicio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Verifica la información antes de confirmar la solicitud técnica",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                // 1. Tarjeta Servicio
                SummaryItemCard(
                    icon = Icons.Rounded.Plumbing,
                    title = "Servicio",
                    value = uiState.selectedService?.name ?: "Servicio no especificado",
                    subValue = "${uiState.selectedService?.category ?: "Plomería"} · Tarifa base: $formattedPrice",
                    onEdit = onNavigateChangeService
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Tarjeta Ubicación
                SummaryItemCard(
                    icon = Icons.Rounded.LocationOn,
                    title = "Ubicación",
                    value = uiState.displayAddress,
                    subValue = if (uiState.addressReference.isNotBlank()) "Ref: ${uiState.addressReference}" else "Sin referencia adicional",
                    onEdit = onNavigateChangeLocation
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Tarjeta Fecha y Horario
                val timeLabel = uiState.selectedSlot?.let { "${it.start} – ${it.end}" } ?: uiState.selectedTime
                SummaryItemCard(
                    icon = Icons.Rounded.CalendarMonth,
                    title = "Fecha y Horario",
                    value = "${uiState.selectedDate} · $timeLabel",
                    subValue = "Duración estimada: 60 - 90 minutos",
                    onEdit = onNavigateChangeSchedule
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Tarjeta Profesional
                val techName = if (uiState.isAutoAssignTechnician) {
                    "⚡ Asignación inteligente PlomApp"
                } else {
                    uiState.selectedTechnician?.name ?: "Por asignar"
                }
                val techSub = if (uiState.isAutoAssignTechnician) {
                    "Coordinaremos al técnico más capacitado y cercano para tu hora elegida."
                } else {
                    "Técnico Verificado PlomApp · ⭐ ${uiState.selectedTechnician?.profile?.rating ?: 4.8}"
                }
                SummaryItemCard(
                    icon = Icons.Rounded.Engineering,
                    title = "Profesional",
                    value = techName,
                    subValue = techSub,
                    onEdit = onNavigateChangeTechnician
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo: ¿Qué está pasando en casa?
                Text(
                    text = "Cuéntale al plomero qué sucede (Opcional)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = problemText,
                    onValueChange = {
                        problemText = it
                        viewModel.updateProblemDescription(it)
                    },
                    placeholder = {
                        Text(
                            "Ej: Fuga continua bajo el lavamanos, la llave no cierra completamente...",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 3,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tarjeta de Garantía y Pago Seguro
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(JadeGreen.copy(alpha = 0.08f))
                        .border(1.dp, JadeGreen.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.VerifiedUser, null, tint = JadeGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Garantía PlomApp 100%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = JadeGreenDark
                            )
                            Text(
                                text = "Solo pagas una vez completado el servicio a entera satisfacción.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subValue: String,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(SapphireBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = SapphireBlue, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextTertiary)
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subValue,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TextButton(
                onClick = onEdit,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("Editar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SapphireBlue)
            }
        }
    }
}

@Composable
private fun BookingSuccessContent(
    appointment: Appointment,
    onViewAppointments: () -> Unit,
    onNavigateHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Checkmark animado
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(JadeGreen.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(JadeGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "¡Servicio agendado!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tu solicitud quedó confirmada. Hemos enviado un correo con todos los detalles.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Card Resumen de la Cita Confirmada
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RESERVA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextTertiary
                        )
                        Text(
                            text = "#PLM-${String.format("%05d", appointment.id)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SapphireBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = appointment.service?.name ?: "Servicio de Plomería",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CalendarMonth, null, tint = SapphireBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${appointment.date} · ${appointment.time}", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationOn, null, tint = SapphireBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = appointment.address ?: "Dirección acordada",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (appointment.technician != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Engineering, null, tint = JadeGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Técnico: ${appointment.technician.name}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // CTAs
            GradientButton(
                text = "Ver mi cita",
                onClick = onViewAppointments,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onNavigateHome,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DividerColor)
                )
            ) {
                Text("Volver al inicio", fontWeight = FontWeight.Bold)
            }
        }
    }
}
