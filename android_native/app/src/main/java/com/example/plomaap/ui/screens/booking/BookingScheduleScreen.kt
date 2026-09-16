package com.example.plomaap.ui.screens.booking

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.plomaap.data.model.TimeSlot
import com.example.plomaap.ui.components.GradientButton
import com.example.plomaap.ui.components.booking.BookingProgressStepper
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.BookingViewModel
import java.text.SimpleDateFormat
import java.util.*

private data class QuickDate(
    val isoDate: String,
    val dayLabel: String,
    val dateLabel: String,
    val isToday: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScheduleScreen(
    viewModel: BookingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTechnician: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    // Generate upcoming 7 days
    val quickDates = remember {
        val list = mutableListOf<QuickDate>()
        val calendar = Calendar.getInstance()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE", Locale("es", "CO"))
        val dayNumFormat = SimpleDateFormat("d MMM", Locale("es", "CO"))

        for (i in 0 until 7) {
            val d = calendar.time
            val isToday = i == 0
            val isTomorrow = i == 1
            val dayLabel = when {
                isToday -> "HOY"
                isTomorrow -> "MAÑANA"
                else -> dayNameFormat.format(d).uppercase()
            }
            list.add(
                QuickDate(
                    isoDate = isoFormat.format(d),
                    dayLabel = dayLabel,
                    dateLabel = dayNumFormat.format(d).uppercase(),
                    isToday = isToday
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    // Auto-select first date if empty
    LaunchedEffect(Unit) {
        if (uiState.selectedDate.isBlank() && quickDates.isNotEmpty()) {
            val firstDate = quickDates.first().isoDate
            viewModel.selectDate(firstDate)
        } else if (uiState.selectedDate.isNotBlank()) {
            viewModel.loadAvailabilitySlots(uiState.selectedDate)
        }
    }

    // Material 3 Date Picker Modal
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    return utcTimeMillis >= cal.timeInMillis
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = millis
                        val iso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                        viewModel.selectDate(iso)
                    }
                    showDatePicker = false
                }) {
                    Text("Seleccionar", fontWeight = FontWeight.Bold, color = SapphireBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Fecha y Horario",
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
                            text = "Horario reservado",
                            fontSize = 11.sp,
                            color = TextTertiary,
                            fontWeight = FontWeight.Medium
                        )
                        val summaryText = if (uiState.selectedDate.isNotBlank() && uiState.selectedTime.isNotBlank()) {
                            "${uiState.selectedDate} · ${uiState.selectedTime}"
                        } else if (uiState.selectedDate.isNotBlank()) {
                            "${uiState.selectedDate} · Elige hora"
                        } else {
                            "Selecciona horario"
                        }
                        Text(
                            text = summaryText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.selectedTime.isNotBlank()) SapphireBlue else TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    GradientButton(
                        text = "Continuar a Técnico",
                        onClick = onNavigateToTechnician,
                        enabled = uiState.selectedDate.isNotBlank() && uiState.selectedTime.isNotBlank(),
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
            // Stepper: Paso 3
            item {
                BookingProgressStepper(currentStep = 3)
            }

            // Título de Fecha
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = "¿Cuándo necesitas el servicio?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Selecciona el día para verificar la disponibilidad de técnicos en tiempo real",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Carrusel de Días Cercanos
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickDates) { qd ->
                            val isSelected = uiState.selectedDate == qd.isoDate
                            DateChip(
                                dayLabel = qd.dayLabel,
                                dateLabel = qd.dateLabel,
                                isSelected = isSelected,
                                onClick = { viewModel.selectDate(qd.isoDate) }
                            )
                        }

                        // Botón de más fechas
                        item {
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(68.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SapphireBlue),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(SapphireBlue.copy(alpha = 0.5f))
                                )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Text("Más fechas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Horarios disponibles
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Horarios disponibles",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (uiState.isLoadingSlots) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = SapphireBlue)
                        }
                    }
                    Text(
                        text = "Calculados según la duración del servicio y disponibilidad de profesionales",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    if (uiState.isLoadingSlots) {
                        // Slots skeleton
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SurfaceElevatedLight)
                                )
                            }
                        }
                    } else if (uiState.availableSlots.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay horarios disponibles para la fecha seleccionada. Por favor elige otro día.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    } else {
                        // Group slots by period
                        val morningSlots = uiState.availableSlots.filter { it.period == "morning" }
                        val afternoonSlots = uiState.availableSlots.filter { it.period == "afternoon" }
                        val eveningSlots = uiState.availableSlots.filter { it.period == "evening" }

                        if (morningSlots.isNotEmpty()) {
                            PeriodSection(
                                icon = Icons.Rounded.WbSunny,
                                title = "Mañana",
                                slots = morningSlots,
                                selectedTime = uiState.selectedTime,
                                onSelectSlot = { viewModel.selectSlot(it) }
                            )
                        }

                        if (afternoonSlots.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            PeriodSection(
                                icon = Icons.Rounded.BrightnessMedium,
                                title = "Tarde",
                                slots = afternoonSlots,
                                selectedTime = uiState.selectedTime,
                                onSelectSlot = { viewModel.selectSlot(it) }
                            )
                        }

                        if (eveningSlots.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            PeriodSection(
                                icon = Icons.Rounded.NightsStay,
                                title = "Noche",
                                slots = eveningSlots,
                                selectedTime = uiState.selectedTime,
                                onSelectSlot = { viewModel.selectSlot(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateChip(
    dayLabel: String,
    dateLabel: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(76.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) SapphireBlue else SurfaceLight)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) SapphireBlue else DividerColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) Color.White else SapphireBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else TextPrimary
            )
        }
    }
}

@Composable
private fun PeriodSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    slots: List<TimeSlot>,
    selectedTime: String,
    onSelectSlot: (TimeSlot) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = SapphireBlue, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of 3 columns
        val chunked = slots.chunked(3)
        chunked.forEach { rowSlots ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSlots.forEach { slot ->
                    val isSelected = selectedTime == slot.start
                    Box(modifier = Modifier.weight(1f)) {
                        SlotItem(
                            slot = slot,
                            isSelected = isSelected,
                            onClick = { if (slot.available) onSelectSlot(slot) }
                        )
                    }
                }
                // Fill empty cells if row < 3
                repeat(3 - rowSlots.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SlotItem(
    slot: TimeSlot,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isEnabled = slot.available

    val bg = when {
        isSelected -> SapphireBlue
        !isEnabled -> SurfaceElevatedLight.copy(alpha = 0.6f)
        else -> SurfaceLight
    }

    val textColor = when {
        isSelected -> Color.White
        !isEnabled -> TextTertiary.copy(alpha = 0.5f)
        else -> TextPrimary
    }

    val borderColor = when {
        isSelected -> SapphireBlue
        !isEnabled -> Color.Transparent
        else -> DividerColor
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = slot.start,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = if (isEnabled) "Disponible" else "Ocupado",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    isSelected -> Color.White.copy(alpha = 0.85f)
                    isEnabled -> JadeGreen
                    else -> TextTertiary.copy(alpha = 0.6f)
                }
            )
        }
    }
}
