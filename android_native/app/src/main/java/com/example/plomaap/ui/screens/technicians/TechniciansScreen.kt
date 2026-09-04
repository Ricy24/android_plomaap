package com.example.plomaap.ui.screens.technicians

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.data.model.Technician
import com.example.plomaap.ui.components.PremiumCard
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.MainViewModel

@Composable
fun TechniciansScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        Text(
            "Técnicos Premium",
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = TextPrimary,
            modifier = Modifier.padding(start = 24.dp, top = 48.dp, bottom = 16.dp)
        )
        
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.technicians) { technician ->
                TechnicianCard(technician)
            }
        }
    }
}

@Composable
private fun TechnicianCard(technician: Technician) {
    PremiumCard(modifier = Modifier.fillMaxWidth(), elevation = 6.dp, cornerRadius = 24.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar Placeholder (Initials)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SapphireBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = technician.name.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(technician.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(technician.profile?.specialties?.joinToString() ?: "Plomero Experto", fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, null, modifier = Modifier.size(18.dp), tint = StarGold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${technician.profile?.rating ?: 5.0}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                }
            }
            
            // Action Button
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(48.dp)
                    .background(SapphireBlue.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Rounded.Message, contentDescription = "Contactar", tint = SapphireBlue)
            }
        }
    }
}
