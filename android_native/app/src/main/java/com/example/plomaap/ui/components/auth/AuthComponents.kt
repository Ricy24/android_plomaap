package com.example.plomaap.ui.components.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.ui.theme.*

/**
 * Cabecera moderna de autenticación con logo, badge animado y jerarquía visual.
 */
@Composable
fun PlomAppAuthHeader(
    title: String,
    subtitle: String,
    badgeText: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevatedLight)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Regresar",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Badge con isotipo de PlomApp
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(listOf(SapphireBlue, ElectricCyan))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Plumbing,
                    contentDescription = "PlomApp",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "PlomApp",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = SapphireBlue
                )
                Text(
                    text = badgeText ?: "Servicios Profesionales",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )
    }
}

/**
 * Campo de texto premium con diseño de tarjeta, animación de foco y validación inline.
 */
@Composable
fun PlomAppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    isValid: Boolean = false,
    isPassword: Boolean = false,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    imeAction: androidx.compose.ui.text.input.ImeAction = androidx.compose.ui.text.input.ImeAction.Default,
    keyboardOptions: KeyboardOptions? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    passwordVisible: Boolean? = null,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    var internalPasswordVisible by remember { mutableStateOf(false) }
    val effectivePasswordVisible = passwordVisible ?: internalPasswordVisible
    val effectiveToggle = onTogglePasswordVisibility ?: { internalPasswordVisible = !internalPasswordVisible }
    val effectiveKeyboardOptions = keyboardOptions ?: KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) ErrorRed else TextPrimary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextTertiary, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isError) ErrorRed else SapphireBlue,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (trailingIcon != null) {
                    trailingIcon()
                } else if (isPassword) {
                    IconButton(onClick = effectiveToggle) {
                        Icon(
                            imageVector = if (effectivePasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (effectivePasswordVisible) "Ocultar" else "Mostrar",
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else if (isValid && value.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Válido",
                        tint = JadeGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            visualTransformation = if (isPassword && !effectivePasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = effectiveKeyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceLight,
                disabledContainerColor = SurfaceElevatedLight,
                focusedBorderColor = SapphireBlue,
                unfocusedBorderColor = DividerColor,
                errorBorderColor = ErrorRed,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = SapphireBlue.copy(alpha = 0.05f))
        )

        if (isError && !errorMessage.isNullOrEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 6.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage,
                    color = ErrorRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Botón principal premium con gradiente, elevación y estado de carga.
 */
@Composable
fun PlomAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String = "Procesando...",
    icon: ImageVector? = null
) {
    Button(
        onClick = { if (enabled && !isLoading) onClick() },
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SapphireBlue,
            disabledContainerColor = SapphireBlue.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(if (enabled && !isLoading) 6.dp else 0.dp, RoundedCornerShape(16.dp), ambientColor = SapphireBlue.copy(alpha = 0.3f))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = loadingText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * Botón secundario para Google Sign-In o Passkeys / Biometría.
 */
@Composable
fun PlomAppSocialButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector,
    iconTint: Color = SapphireBlue,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    PlomAppSocialButton(
        text = text,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = modifier,
        enabled = enabled
    )
}

@Composable
fun PlomAppSocialButton(
    text: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, DividerColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = SurfaceLight,
            contentColor = TextPrimary
        ),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = SapphireBlue.copy(alpha = 0.04f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            icon()
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary
            )
        }
    }
}

/**
 * Indicador de progreso de pasos para registro progresivo (Paso X de 4).
 */
@Composable
fun PlomAppStepIndicator(
    currentStep: Int,
    totalSteps: Int = 4,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Paso $currentStep de $totalSteps",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SapphireBlue
            )
            Text(
                text = when (currentStep) {
                    1 -> "Identidad"
                    2 -> "Contacto"
                    3 -> "Seguridad"
                    else -> "Ubicación"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (step in 1..totalSteps) {
                val isCompleted = step <= currentStep
                val animatedColor by animateColorAsState(
                    targetValue = if (isCompleted) SapphireBlue else DividerColor,
                    animationSpec = tween(300),
                    label = "stepColor$step"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(animatedColor)
                )
            }
        }
    }
}

/**
 * Medidor de fortaleza de contraseña con feedback visual en tiempo real.
 */
@Composable
fun PlomAppPasswordStrengthMeter(
    password: String,
    modifier: Modifier = Modifier
) {
    val lengthOk = password.length >= 8
    val hasNumber = password.any { it.isDigit() }
    val hasUpper = password.any { it.isUpperCase() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }

    val score = listOf(lengthOk, hasNumber, hasUpper, hasSpecial).count { it }

    val (label, color) = when {
        password.isEmpty() -> "" to Color.Transparent
        score <= 1 -> "Débil" to ErrorRed
        score == 2 -> "Regular" to AmberWarning
        score == 3 -> "Buena" to SapphireBlueLight
        else -> "Excelente y Segura" to JadeGreen
    }

    if (password.isNotEmpty()) {
        Column(modifier = modifier.fillMaxWidth().padding(top = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Seguridad: $label",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(4) { idx ->
                    val active = idx < score
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (active) color else DividerColor)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (lengthOk) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (lengthOk) JadeGreen else TextTertiary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mínimo 8 caracteres", fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = if (hasNumber && (hasUpper || hasSpecial)) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (hasNumber && (hasUpper || hasSpecial)) JadeGreen else TextTertiary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Números y letras", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}
