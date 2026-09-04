package com.example.plomaap.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.plomaap.ui.components.OutlinedPremiumButton
import com.example.plomaap.ui.components.PremiumCard
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(navController: NavController, viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user

    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SapphireBlue)
                .padding(vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = SapphireBlue)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(user?.name ?: "Usuario", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(user?.email ?: "correo@ejemplo.com", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                    Icon(Icons.Rounded.Email, null, tint = SapphireBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Correo Electrónico", color = TextTertiary, fontSize = 12.sp)
                        Text(user?.email ?: "correo@ejemplo.com", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Divider(color = DividerColor)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                    Icon(Icons.Rounded.Phone, null, tint = SapphireBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Teléfono", color = TextTertiary, fontSize = 12.sp)
                        Text(user?.phone ?: "No registrado", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            OutlinedPremiumButton(
                text = "Cerrar Sesión",
                onClick = { 
                    viewModel.logout()
                    navController.navigate("auth") { popUpTo(0) }
                },
                modifier = Modifier.fillMaxWidth(),
                icon = { Icon(Icons.Rounded.Logout, contentDescription = "Salir") }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
