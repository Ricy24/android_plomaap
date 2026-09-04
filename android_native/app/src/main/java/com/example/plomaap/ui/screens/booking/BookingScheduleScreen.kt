package com.example.plomaap.ui.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import com.example.plomaap.ui.components.GradientButton
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.BookingViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScheduleScreen(
    viewModel: BookingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLocation: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    // Generate next 14 days
    val dates = remember { (0..13).map { LocalDate.now().plusDays(it.toLong()) } }
    
    // Generate times (9 AM to 6 PM)
    val times = remember {
        listOf("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00")
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Fecha y Hora", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        bottomBar = {
            Surface(color = SurfaceLight, shadowElevation = 24.dp, modifier = Modifier.fillMaxWidth()) {
                PaddingValues(horizontal = 24.dp, vertical = 16.dp).let { padding ->
                    GradientButton(
                        text = "Continuar",
                        onClick = {
                            if (selectedTime != null) {
                                viewModel.selectDateTime(selectedDate.toString(), selectedTime!!)
                                onNavigateToLocation()
                            }
                        },
                        enabled = selectedTime != null,
                        modifier = Modifier.padding(padding).fillMaxWidth()
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Selecciona un día", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(dates) { date ->
                    DateCard(
                        date = date,
                        isSelected = date == selectedDate,
                        onClick = { selectedDate = date; selectedTime = null }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text("Selecciona la hora", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(times) { time ->
                    TimeCard(
                        time = time,
                        isSelected = time == selectedTime,
                        onClick = { selectedTime = time }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateCard(date: LocalDate, isSelected: Boolean, onClick: () -> Unit) {
    val formatterDayOfWeek = DateTimeFormatter.ofPattern("EEE", Locale("es", "ES"))
    val formatterDayOfMonth = DateTimeFormatter.ofPattern("dd")
    val month = DateTimeFormatter.ofPattern("MMM", Locale("es", "ES")).format(date)

    val bgColor = if (isSelected) SapphireBlue else SurfaceElevatedLight
    val contentColor = if (isSelected) Color.White else TextPrimary
    val borderColor = if (isSelected) SapphireBlue else DividerColor

    Column(
        modifier = Modifier
            .width(70.dp)
            .height(90.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val formattedDay = formatterDayOfWeek.format(date).take(3)
        val capitalizedDay = formattedDay.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        Text(capitalizedDay, fontSize = 13.sp, color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(formatterDayOfMonth.format(date), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = contentColor)
        val capitalizedMonth = month.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        Text(capitalizedMonth, fontSize = 11.sp, color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextTertiary)
    }
}

@Composable
private fun TimeCard(time: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) SapphireBlue.copy(alpha = 0.15f) else SurfaceLight
    val textColor = if (isSelected) SapphireBlue else TextPrimary
    val borderColor = if (isSelected) SapphireBlue else DividerColor

    Box(
        modifier = Modifier
            .height(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(time, fontSize = 15.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = textColor, textAlign = TextAlign.Center)
    }
}
