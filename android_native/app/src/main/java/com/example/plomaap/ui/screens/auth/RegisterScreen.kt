package com.example.plomaap.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Estados del formulario progresivo
    var currentStep by remember { mutableIntStateOf(1) } // 1 a 4 + 5 (confirmación)
    val totalSteps = 4

    var fullName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("customer") } // "customer" | "technician"

    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var address by remember { mutableStateOf("") }

    // Validaciones por paso
    val isNameValid = fullName.trim().length >= 3
    val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isPhoneValid = phone.trim().length >= 7
    val isPasswordValid = password.length >= 6
    val isPasswordMatch = password.isNotEmpty() && password == confirmPassword
    val isAddressValid = address.isNotBlank()

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
            Spacer(modifier = Modifier.height(16.dp))

            // Barra superior con botón volver y título
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentStep > 1) {
                            currentStep--
                        } else {
                            navController.popBackStack()
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
                    text = if (currentStep <= totalSteps) "Crear tu cuenta" else "Confirmar datos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Indicador de pasos animado
            PlomAppStepIndicator(
                currentStep = currentStep.coerceAtMost(totalSteps),
                totalSteps = totalSteps,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error banner si existe
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

            // Contenedor animado de pasos
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "registrationSteps"
            ) { step ->
                when (step) {
                    // PASO 1: Identidad
                    1 -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = SurfaceLight,
                            shadowElevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "¡Vamos a conocernos!",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "¿Cómo te llamas y qué tipo de cuenta necesitas?",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                                )

                                PlomAppTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = "Nombre completo",
                                    placeholder = "Juan Pérez",
                                    leadingIcon = Icons.Rounded.Person,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done,
                                    isError = fullName.isNotEmpty() && !isNameValid,
                                    errorMessage = if (fullName.isNotEmpty() && !isNameValid) "Mínimo 3 letras" else null
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "¿Cómo usarás PlomApp?",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RoleCard(
                                        title = "Cliente",
                                        subtitle = "Busco servicios",
                                        icon = Icons.Rounded.Home,
                                        isSelected = selectedRole == "customer",
                                        onClick = { selectedRole = "customer" },
                                        modifier = Modifier.weight(1f)
                                    )
                                    RoleCard(
                                        title = "Técnico",
                                        subtitle = "Ofrezco servicios",
                                        icon = Icons.Rounded.Build,
                                        isSelected = selectedRole == "technician",
                                        onClick = { selectedRole = "technician" },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                PlomAppButton(
                                    text = "Continuar",
                                    onClick = { currentStep = 2 },
                                    enabled = isNameValid,
                                    icon = Icons.Rounded.ArrowForward,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // PASO 2: Contacto
                    2 -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = SurfaceLight,
                            shadowElevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "¿Dónde podemos contactarte?",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Usaremos estos datos para enviarte actualizaciones de tus servicios.",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                                )

                                PlomAppTextField(
                                    value = email,
                                    onValueChange = { email = it.trim() },
                                    label = "Correo electrónico",
                                    placeholder = "correo@ejemplo.com",
                                    leadingIcon = Icons.Rounded.Email,
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next,
                                    isError = email.isNotEmpty() && !isEmailValid,
                                    errorMessage = if (email.isNotEmpty() && !isEmailValid) "Correo no válido" else null
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                PlomAppTextField(
                                    value = phone,
                                    onValueChange = { phone = it.filter { ch -> ch.isDigit() || ch == '+' } },
                                    label = "Teléfono celular",
                                    placeholder = "300 123 4567",
                                    leadingIcon = Icons.Rounded.Phone,
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Done,
                                    isError = phone.isNotEmpty() && !isPhoneValid,
                                    errorMessage = if (phone.isNotEmpty() && !isPhoneValid) "Mínimo 7 dígitos" else null
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                PlomAppButton(
                                    text = "Continuar",
                                    onClick = { currentStep = 3 },
                                    enabled = isEmailValid && isPhoneValid,
                                    icon = Icons.Rounded.ArrowForward,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // PASO 3: Seguridad
                    3 -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = SurfaceLight,
                            shadowElevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Protejamos tu cuenta",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Crea una contraseña segura para acceder a tus reservas.",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                                )

                                PlomAppTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = "Contraseña",
                                    placeholder = "••••••••",
                                    leadingIcon = Icons.Rounded.Lock,
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next,
                                    isPassword = true,
                                    passwordVisible = passwordVisible,
                                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Medidor de fuerza de la contraseña
                                PlomAppPasswordStrengthMeter(
                                    password = password,
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
                                    isError = confirmPassword.isNotEmpty() && !isPasswordMatch,
                                    errorMessage = if (confirmPassword.isNotEmpty() && !isPasswordMatch) "Las contraseñas no coinciden" else null
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                PlomAppButton(
                                    text = "Continuar a Ubicación",
                                    onClick = { currentStep = 4 },
                                    enabled = isPasswordValid && isPasswordMatch,
                                    icon = Icons.Rounded.ArrowForward,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // PASO 4: Ubicación Inicial
                    4 -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = SurfaceLight,
                            shadowElevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "¿Dónde necesitas el servicio?",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Usa tu ubicación actual o busca la dirección de tu hogar u oficina.",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                                )

                                AuthLocationStep(
                                    currentAddress = address,
                                    onAddressSelected = { selectedAddr, _ ->
                                        address = selectedAddr
                                    }
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                PlomAppButton(
                                    text = "Revisar y Confirmar",
                                    onClick = { currentStep = 5 },
                                    enabled = isAddressValid,
                                    icon = Icons.Rounded.CheckCircle,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // CONFIRMAR (Paso 5)
                    5 -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = SurfaceLight,
                            shadowElevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "¡Todo listo para empezar!",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Confirma que los datos sean correctos para crear tu perfil.",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                                )

                                SummaryItem(icon = Icons.Rounded.Person, title = "Nombre", value = fullName)
                                SummaryItem(icon = Icons.Rounded.Badge, title = "Tipo de cuenta", value = if (selectedRole == "customer") "Cliente" else "Técnico Profesional")
                                SummaryItem(icon = Icons.Rounded.Email, title = "Correo", value = email)
                                SummaryItem(icon = Icons.Rounded.Phone, title = "Teléfono", value = phone)
                                SummaryItem(icon = Icons.Rounded.LocationOn, title = "Ubicación inicial", value = address)

                                Spacer(modifier = Modifier.height(24.dp))

                                PlomAppButton(
                                    text = "Crear Mi Cuenta",
                                    onClick = {
                                        viewModel.register(
                                            name = fullName,
                                            email = email,
                                            password = password,
                                            role = selectedRole,
                                            phone = phone,
                                            address = address
                                        )
                                    },
                                    isLoading = uiState.isLoading,
                                    icon = Icons.Rounded.RocketLaunch,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Link para usuarios existentes
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "¿Ya tienes cuenta?",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = { navController.navigate(Routes.LOGIN) }
                ) {
                    Text(
                        text = "Inicia sesión",
                        color = SapphireBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) SapphireBlue.copy(alpha = 0.08f) else SurfaceElevatedLight,
        border = ButtonDefaults.outlinedButtonBorder(true).copy(
            brush = if (isSelected) Brush.horizontalGradient(listOf(SapphireBlue, SapphireBlueDark))
            else Brush.horizontalGradient(listOf(DividerColor, DividerColor))
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) SapphireBlue else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) SapphireBlue else TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(SapphireBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = SapphireBlue, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 11.sp, color = TextTertiary)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}
