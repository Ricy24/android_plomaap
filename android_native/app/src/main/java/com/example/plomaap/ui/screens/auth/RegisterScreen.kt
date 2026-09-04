package com.example.plomaap.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.plomaap.ui.components.*
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("customer") }
    val roles = listOf("customer", "technician")

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            navController.navigate("main") { popUpTo("auth") { inclusive = true } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                if (uiState.errorMessage != null) {
                    Text(uiState.errorMessage!!, color = ErrorRed, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
                }

                PremiumTextField(
                    value = name, onValueChange = { name = it },
                    label = "Nombre completo", leadingIcon = Icons.Rounded.Person,
                    imeAction = ImeAction.Next
                )
                Spacer(modifier = Modifier.height(16.dp))
                PremiumTextField(
                    value = email, onValueChange = { email = it },
                    label = "Correo electrónico", leadingIcon = Icons.Rounded.Email,
                    keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
                )
                Spacer(modifier = Modifier.height(16.dp))
                PremiumTextField(
                    value = phone, onValueChange = { phone = it },
                    label = "Teléfono", leadingIcon = Icons.Rounded.Phone,
                    keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next
                )
                Spacer(modifier = Modifier.height(16.dp))
                PremiumTextField(
                    value = address, onValueChange = { address = it },
                    label = "Dirección", leadingIcon = Icons.Rounded.LocationOn,
                    imeAction = ImeAction.Next
                )
                Spacer(modifier = Modifier.height(16.dp))
                PremiumTextField(
                    value = password, onValueChange = { password = it },
                    label = "Contraseña", leadingIcon = Icons.Rounded.Lock,
                    isPassword = true, passwordVisible = passwordVisible,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = TextTertiary)
                        }
                    },
                    imeAction = ImeAction.Done
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = if (selectedRole == "customer") "Cliente" else "Técnico",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = SurfaceElevatedLight,
                            focusedContainerColor = SurfaceLight,
                            unfocusedBorderColor = DividerColor,
                            focusedBorderColor = SapphireBlue
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(SurfaceLight)
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(if (role == "customer") "Cliente" else "Técnico", color = TextPrimary) },
                                onClick = {
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                GradientButton(
                    text = "Registrarse",
                    onClick = { viewModel.register(name, email, password, selectedRole, phone, address) },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
