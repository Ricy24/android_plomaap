package com.example.plomaap.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.plomaap.ui.components.auth.*
import com.example.plomaap.ui.navigation.Routes
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.AuthViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Validación inline rápida
    val isEmailValid = email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPasswordValid = password.isEmpty() || password.length >= 6
    val canSubmit = email.isNotBlank() && password.isNotBlank() && isEmailValid && isPasswordValid

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            navController.navigate(Routes.MAIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Header PlomApp
            PlomAppAuthHeader(
                title = "Bienvenido de nuevo",
                subtitle = "Inicia sesión con tu método preferido",
                badgeText = "Acceso Seguro"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Human-friendly Error Message Banner
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                uiState.errorMessage?.let { errorText ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = ErrorRed.copy(alpha = 0.08f),
                        border = ButtonDefaults.outlinedButtonBorder(true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(ErrorRed.copy(alpha = 0.3f))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = errorText,
                                color = ErrorRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearMessages() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Cerrar",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Opción 1: Passkeys / Biometría Credential Manager (PRIORIDAD 2)
            PlomAppSocialButton(
                text = "Continuar con Passkey",
                onClick = { viewModel.signInWithPasskey(context) },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(SapphireBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Fingerprint,
                            contentDescription = "Passkey",
                            tint = SapphireBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Opción 2: Google Sign-In
            PlomAppSocialButton(
                text = "Continuar con Google",
                onClick = { viewModel.signInWithGoogle(context) },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4285F4)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Separador con etiqueta "o con correo"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
                Text(
                    text = "  o con tu correo  ",
                    color = TextTertiary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card con campos clásicos de correo y contraseña
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceLight,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    PlomAppTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        label = "Correo electrónico",
                        placeholder = "ejemplo@correo.com",
                        leadingIcon = Icons.Rounded.Email,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        isError = !isEmailValid,
                        errorMessage = if (!isEmailValid) "Ingresa un correo electrónico válido" else null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PlomAppTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        placeholder = "••••••••",
                        leadingIcon = Icons.Rounded.Lock,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                        isError = !isPasswordValid,
                        errorMessage = if (!isPasswordValid) "Debe tener al menos 6 caracteres" else null
                    )

                    // Olvidó su contraseña
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(
                            onClick = { navController.navigate(Routes.FORGOT_PASSWORD) }
                        ) {
                            Text(
                                text = "¿Olvidaste tu contraseña?",
                                color = SapphireBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PlomAppButton(
                        text = "Iniciar Sesión",
                        onClick = { viewModel.login(email, password) },
                        isLoading = uiState.isLoading,
                        enabled = canSubmit,
                        icon = Icons.Rounded.Login,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navegación a Registro
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "¿No tienes cuenta?",
                    color = TextSecondary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = { navController.navigate(Routes.REGISTER) }
                ) {
                    Text(
                        text = "Regístrate aquí",
                        color = SapphireBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
