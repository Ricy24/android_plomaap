package com.example.plomaap.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.ui.components.auth.PlomAppButton
import com.example.plomaap.ui.components.auth.PlomAppPasswordStrengthMeter
import com.example.plomaap.ui.components.auth.PlomAppTextField
import com.example.plomaap.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    isLoading: Boolean,
    successMessage: String?,
    errorMessage: String? = null,
    onSendReset: (String) -> Unit,
    onResetPassword: (email: String, token: String, newPass: String) -> Unit = { _, _, _ -> },
    onNavigateBack: () -> Unit,
    onClearMessages: () -> Unit
) {
    // 1 = Solicitar código por correo, 2 = Ingresar código y nueva contraseña, 3 = Éxito total
    var step by remember { mutableIntStateOf(1) }
    var email by remember { mutableStateOf("") }
    var resetToken by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isTokenValid = resetToken.trim().length >= 6
    val isNewPassValid = newPassword.length >= 6
    val isPassMatch = newPassword.isNotEmpty() && newPassword == confirmPassword

    LaunchedEffect(successMessage) {
        if (successMessage != null && step == 1) {
            step = 2 // Pasar a ingresar el código
        } else if (successMessage != null && step == 2 && successMessage.contains("exitosamente", ignoreCase = true)) {
            step = 3 // Completado
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Barra superior
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (step == 2) {
                            step = 1
                            onClearMessages()
                        } else {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceLight)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Atrás",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Recuperar Contraseña",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Icono animado del candado
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SapphireBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (step) {
                        3 -> Icons.Rounded.CheckCircle
                        2 -> Icons.Rounded.Key
                        else -> Icons.Rounded.LockReset
                    },
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = if (step == 3) JadeGreen else SapphireBlue
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error banner
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                errorMessage?.let { errorText ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = ErrorRed.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(errorText, color = ErrorRed, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "forgotPassSteps"
            ) { currentStep ->
                when (currentStep) {
                    // PASO 1: Ingresar email
                    1 -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = SurfaceLight,
                            shadowElevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "¿Olvidaste tu contraseña?",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ingresa tu correo registrado. Te enviaremos un código de seguridad para restaurar tu acceso.",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                PlomAppTextField(
                                    value = email,
                                    onValueChange = { email = it.trim() },
                                    label = "Correo electrónico",
                                    placeholder = "ejemplo@correo.com",
                                    leadingIcon = Icons.Rounded.Email,
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done,
                                    isError = email.isNotEmpty() && !isEmailValid,
                                    errorMessage = if (email.isNotEmpty() && !isEmailValid) "Correo no válido" else null
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                PlomAppButton(
                                    text = "Enviar Código",
                                    onClick = { onSendReset(email) },
                                    isLoading = isLoading,
                                    enabled = isEmailValid,
                                    icon = Icons.Rounded.Send,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // PASO 2: Ingresar código y nueva contraseña
                    2 -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = SurfaceLight,
                            shadowElevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Ingresa el código recibido",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Hemos enviado un código a $email. Escríbelo junto con tu nueva contraseña.",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                PlomAppTextField(
                                    value = resetToken,
                                    onValueChange = { resetToken = it.trim() },
                                    label = "Código de 6 caracteres",
                                    placeholder = "ABC123",
                                    leadingIcon = Icons.Rounded.Key,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                PlomAppTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = "Nueva contraseña",
                                    placeholder = "••••••••",
                                    leadingIcon = Icons.Rounded.Lock,
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next,
                                    isPassword = true,
                                    passwordVisible = passwordVisible,
                                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                PlomAppPasswordStrengthMeter(
                                    password = newPassword,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                PlomAppTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = "Confirmar contraseña",
                                    placeholder = "••••••••",
                                    leadingIcon = Icons.Rounded.LockReset,
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done,
                                    isPassword = true,
                                    passwordVisible = passwordVisible,
                                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                                    isError = confirmPassword.isNotEmpty() && !isPassMatch,
                                    errorMessage = if (confirmPassword.isNotEmpty() && !isPassMatch) "Las contraseñas no coinciden" else null
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                PlomAppButton(
                                    text = "Restablecer Contraseña",
                                    onClick = { onResetPassword(email, resetToken, newPassword) },
                                    isLoading = isLoading,
                                    enabled = isTokenValid && isNewPassValid && isPassMatch,
                                    icon = Icons.Rounded.CheckCircle,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // PASO 3: Éxito
                    3 -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = SurfaceLight,
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "¡Contraseña actualizada!",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tu contraseña ha sido restablecida de forma segura. Ya puedes ingresar a PlomApp.",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(28.dp))

                                PlomAppButton(
                                    text = "Iniciar Sesión",
                                    onClick = onNavigateBack,
                                    icon = Icons.Rounded.Login,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
