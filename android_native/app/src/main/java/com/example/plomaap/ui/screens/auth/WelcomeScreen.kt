package com.example.plomaap.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.plomaap.ui.components.auth.PlomAppButton
import com.example.plomaap.ui.navigation.Routes
import com.example.plomaap.ui.theme.*

@Composable
fun WelcomeScreen(navController: NavController) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Pulsing animation for badge / brand element
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, easing = EaseOut),
        label = "contentAlpha"
    )

    val contentOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 30f,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "contentOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SapphireBlueDark,
                        SapphireBlue,
                        Color(0xFF0C2444),
                        Color(0xFF081729)
                    )
                )
            )
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        // Decorative glowing circles
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = (-40).dp)
                .size(280.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x334E80EE), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 40.dp)
                .size(320.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x2210B981), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .alpha(contentAlpha)
                .offset(y = contentOffset.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header with Modern Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(104.dp)
                        .shadow(24.dp, CircleShape, ambientColor = Color.Black, spotColor = SapphireBlue)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.White, Color(0xFFF1F5F9))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Plumbing,
                        contentDescription = "PlomApp Logo",
                        tint = SapphireBlue,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(JadeGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Servicios de Plomería On-Demand",
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "PlomApp",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Expertos certificados en minutos,\nsoluciones garantizadas a tu puerta.",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // Highlights feature cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeaturePill(
                    icon = Icons.Rounded.Bolt,
                    title = "Atención Inmediata",
                    subtitle = "Técnicos disponibles cerca de ti en tiempo real"
                )
                FeaturePill(
                    icon = Icons.Rounded.VerifiedUser,
                    title = "Seguridad y Garantía",
                    subtitle = "Autenticación segura con Passkeys y Google"
                )
                FeaturePill(
                    icon = Icons.Rounded.LocationOn,
                    title = "Geolocalización Exacta",
                    subtitle = "Rastreo y asignación automática con Google Maps"
                )
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlomAppButton(
                    text = "Comenzar Ahora",
                    onClick = { navController.navigate(Routes.REGISTER) },
                    icon = Icons.Rounded.ArrowForward,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { navController.navigate(Routes.LOGIN) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = Brush.horizontalGradient(
                            listOf(Color.White.copy(alpha = 0.4f), Color.White.copy(alpha = 0.2f))
                        )
                    )
                ) {
                    Text(
                        "Ya tengo una cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun FeaturePill(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
