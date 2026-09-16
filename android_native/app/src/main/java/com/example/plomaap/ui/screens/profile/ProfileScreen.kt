package com.example.plomaap.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Plumbing
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(navController: NavController, viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500), label = "profileAlpha"
    )

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = ErrorRed) },
            title = { Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text("¿Estás seguro que deseas cerrar sesión?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Sí, cerrar sesión", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = SurfaceLight,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        // Header con gradiente + avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(SapphireBlueDark, SapphireBlue, SapphireBlueLight))
                )
                .padding(bottom = 40.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .shadow(16.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.3f))
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(Color.White, Color(0xFFE3F2FD)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.name ?: "U").take(1).uppercase(),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SapphireBlue
                        )
                    }
                    // Badge de rol
                    val isCustomer = (user?.role ?: "customer") == "customer"
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp),
                        shape = CircleShape,
                        color = if (isCustomer) JadeGreen else StarGold,
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            if (isCustomer) Icons.Rounded.Person else Icons.Rounded.Engineering,
                            null,
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp).size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    user?.name ?: "Usuario",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    user?.email ?: "correo@ejemplo.com",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Badge de rol
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        if ((user?.role ?: "customer") == "customer") "👤 Cliente" else "🔧 Técnico",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Stats flotantes (sobresalen del header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-30).dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatCard("0", "Servicios", Icons.Rounded.Plumbing, SapphireBlue, Modifier.weight(1f))
            ProfileStatCard("0", "Citas", Icons.Rounded.CalendarMonth, JadeGreen, Modifier.weight(1f))
            ProfileStatCard("5.0", "Rating", Icons.Rounded.Star, StarGold, Modifier.weight(1f))
        }

        // Información de cuenta
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-20).dp)
        ) {
            Text(
                "Información de Cuenta",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceLight,
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    ProfileInfoRow(Icons.Rounded.Email, "Correo Electrónico", user?.email ?: "No registrado")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                    ProfileInfoRow(Icons.Rounded.Phone, "Teléfono", user?.phone ?: "No registrado")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                    ProfileInfoRow(
                        Icons.Rounded.LocationOn,
                        "Dirección",
                        user?.address?.takeIf { it.isNotEmpty() } ?: "No registrada"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Acciones
            Text(
                "Acciones",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceLight,
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    ProfileActionRow(
                        icon = Icons.Rounded.Edit,
                        label = "Editar Perfil",
                        iconColor = SapphireBlue,
                        onClick = { navController.navigate("edit_profile") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                    ProfileActionRow(
                        icon = Icons.Rounded.Lock,
                        label = "Cambiar Contraseña",
                        iconColor = Color(0xFF9C27B0),
                        onClick = { navController.navigate("forgot_password") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                    ProfileActionRow(
                        icon = Icons.Rounded.Notifications,
                        label = "Notificaciones",
                        iconColor = Color(0xFFFF7043),
                        onClick = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Cerrar Sesión
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed.copy(alpha = 0.1f),
                    contentColor = ErrorRed
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun ProfileStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.shadow(10.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceLight
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
            Text(label, fontSize = 11.sp, color = TextTertiary)
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(SapphireBlue.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = SapphireBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextTertiary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProfileActionRow(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                label,
                modifier = Modifier.weight(1f),
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(Icons.Rounded.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(20.dp))
        }
    }
}
