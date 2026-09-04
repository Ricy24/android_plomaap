package com.example.plomaap.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.ui.components.GradientButton
import com.example.plomaap.ui.components.PremiumCard
import com.example.plomaap.ui.components.PremiumTextField
import com.example.plomaap.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    isLoading: Boolean,
    successMessage: String?,
    onSendReset: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onClearMessages: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var contentVisible by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(100); contentVisible = true }
    LaunchedEffect(successMessage) { if (successMessage != null) { showSuccess = true } }

    Scaffold(
        containerColor = BackgroundLight, 
        topBar = { 
            TopAppBar(
                title = { }, 
                navigationIcon = { 
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = TextPrimary) } 
                }, 
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            ) 
        }
    ) { paddingValues ->
        AnimatedVisibility(visible = contentVisible, enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { 60 }, animationSpec = tween(500, easing = EaseOutCubic))) {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(modifier = Modifier.size(80.dp).background(SapphireBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.LockReset, null, modifier = Modifier.size(40.dp), tint = SapphireBlue)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Recuperar Contraseña", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Ingresa tu correo electrónico y te enviaremos las instrucciones para restablecer tu contraseña.", fontSize = 15.sp, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 22.sp)
                
                Spacer(modifier = Modifier.height(40.dp))

                AnimatedVisibility(visible = !showSuccess, exit = fadeOut() + shrinkVertically()) {
                    PremiumCard(modifier = Modifier.fillMaxWidth()) {
                        PremiumTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Correo electrónico",
                            leadingIcon = Icons.Rounded.Email,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done,
                            onImeAction = { if (email.isNotEmpty()) onSendReset(email) }
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        GradientButton(
                            text = "Enviar Enlace",
                            onClick = { onSendReset(email) },
                            isLoading = isLoading,
                            enabled = email.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                AnimatedVisibility(visible = showSuccess, enter = fadeIn(tween(600)) + expandVertically()) {
                    PremiumCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                            Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(60.dp), tint = JadeGreen)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("¡Revisa tu correo!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(successMessage ?: "Hemos enviado las instrucciones.", fontSize = 15.sp, color = TextSecondary, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue), shape = CircleShape) {
                                Text("Volver al Inicio", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
