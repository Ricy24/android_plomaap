package com.example.plomaap.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.plomaap.R
import com.example.plomaap.ui.components.*
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.AuthViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            navController.navigate("main") { popUpTo("auth") { inclusive = true } }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        // Decorative background elements (bubbles)
        Box(modifier = Modifier.offset(x = (-50).dp, y = (-50).dp).size(200.dp).background(SapphireBlue.copy(alpha = 0.05f), shape = androidx.compose.foundation.shape.CircleShape))
        Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 50.dp, y = 100.dp).size(150.dp).background(JadeGreen.copy(alpha = 0.05f), shape = androidx.compose.foundation.shape.CircleShape))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // App Logo
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "PlomApp Logo",
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Bienvenido", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Inicia sesión para continuar", fontSize = 16.sp, color = TextTertiary)
            
            Spacer(modifier = Modifier.height(40.dp))

            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                if (uiState.errorMessage != null) {
                    Text(uiState.errorMessage!!, color = ErrorRed, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
                }

                PremiumTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo electrónico",
                    leadingIcon = Icons.Rounded.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
                Spacer(modifier = Modifier.height(16.dp))
                PremiumTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    leadingIcon = Icons.Rounded.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = TextTertiary)
                        }
                    },
                    imeAction = ImeAction.Done,
                    onImeAction = { viewModel.login(email, password) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = { navController.navigate("forgot_password") },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("¿Olvidaste tu contraseña?", color = SapphireBlue, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))
                GradientButton(
                    text = "Iniciar Sesión",
                    onClick = { viewModel.login(email, password) },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Divider(modifier = Modifier.weight(1f), color = DividerColor)
                    Text("O continua con", modifier = Modifier.padding(horizontal = 16.dp), color = TextTertiary, fontSize = 14.sp)
                    Divider(modifier = Modifier.weight(1f), color = DividerColor)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedPremiumButton(
                    text = "Google",
                    onClick = { /* Google Login */ },
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(painter = painterResource(id = R.mipmap.ic_launcher), contentDescription = "G", tint = Color.Unspecified, modifier = Modifier.size(24.dp))
                    }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
                Text("¿No tienes cuenta?", color = TextSecondary)
                TextButton(onClick = { navController.navigate("register") }) {
                    Text("Regístrate", color = SapphireBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
