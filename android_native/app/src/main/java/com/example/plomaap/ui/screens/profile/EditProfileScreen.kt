package com.example.plomaap.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.data.model.User
import com.example.plomaap.ui.components.GradientButton
import com.example.plomaap.ui.components.PremiumCard
import com.example.plomaap.ui.components.PremiumTextField
import com.example.plomaap.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User?,
    isLoading: Boolean,
    successMessage: String?,
    onSave: (name: String, phone: String, address: String) -> Unit,
    onNavigateBack: () -> Unit,
    onClearMessages: () -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var address by remember { mutableStateOf(user?.address ?: "") }
    var showSuccessBanner by remember { mutableStateOf(false) }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            showSuccessBanner = true
            delay(3000)
            showSuccessBanner = false
            onClearMessages()
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = TextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())) {
            if (showSuccessBanner) {
                Box(modifier = Modifier.fillMaxWidth().background(JadeGreen).padding(16.dp), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(successMessage ?: "Guardado", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Profile Picture Placeholder
                Box(modifier = Modifier.size(100.dp).background(SapphireBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Person, null, modifier = Modifier.size(50.dp), tint = SapphireBlue)
                }
                Spacer(modifier = Modifier.height(32.dp))
                
                PremiumCard(modifier = Modifier.fillMaxWidth()) {
                    PremiumTextField(
                        value = name, onValueChange = { name = it },
                        label = "Nombre completo", leadingIcon = Icons.Rounded.Person,
                        imeAction = ImeAction.Next
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
                        imeAction = ImeAction.Done, onImeAction = { onSave(name, phone, address) }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    GradientButton(
                        text = "Guardar Cambios",
                        onClick = { onSave(name, phone, address) },
                        isLoading = isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
