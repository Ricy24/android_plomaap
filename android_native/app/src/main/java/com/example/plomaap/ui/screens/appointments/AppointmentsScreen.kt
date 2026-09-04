package com.example.plomaap.ui.screens.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.data.model.Appointment
import com.example.plomaap.ui.components.PremiumCard
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.MainViewModel

@Composable
fun AppointmentsScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        Text(
            "Mis Citas",
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = TextPrimary,
            modifier = Modifier.padding(start = 24.dp, top = 48.dp, bottom = 16.dp)
        )
        
        if (uiState.appointments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.CalendarToday, null, modifier = Modifier.size(64.dp), tint = TextTertiary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tienes citas programadas", color = TextSecondary, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.appointments) { appointment ->
                    AppointmentCard(appointment)
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(appointment: Appointment) {
    val statusColor = when (appointment.status.lowercase()) {
        "scheduled" -> StatusScheduled
        "in_progress" -> StatusInProgress
        "completed" -> StatusCompleted
        "cancelled" -> StatusCancelled
        else -> TextTertiary
    }
    val statusText = when (appointment.status.lowercase()) {
        "scheduled" -> "Programada"
        "in_progress" -> "En Curso"
        "completed" -> "Completada"
        "cancelled" -> "Cancelada"
        else -> "Pendiente"
    }

    PremiumCard(modifier = Modifier.fillMaxWidth(), elevation = 6.dp, cornerRadius = 20.dp) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(appointment.service_name ?: "Servicio General", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Box(modifier = Modifier.background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.CalendarToday, null, modifier = Modifier.size(16.dp), tint = TextTertiary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("${appointment.date} a las ${appointment.time}", fontSize = 14.sp, color = TextSecondary)
        }
        if (appointment.technician_name != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Técnico: ${appointment.technician_name}", fontSize = 14.sp, color = TextSecondary)
        }
    }
}
