package com.example.plomaap.ui.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.ui.components.GradientButton
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.BookingViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingLocationScreen(
    viewModel: BookingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSummary: () -> Unit
) {
    // Default location (e.g. Santiago, Chile or a generic city)
    val defaultLocation = LatLng(-33.4489, -70.6693)
    var markerPosition by remember { mutableStateOf(defaultLocation) }
    var address by remember { mutableStateOf("Av. Providencia 1234, Santiago") }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Ubicación del Servicio", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.9f))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    coroutineScope.launch {
                        cameraPositionState.animate(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
                    }
                },
                containerColor = Color.White,
                modifier = Modifier.padding(bottom = 120.dp)
            ) {
                Icon(Icons.Rounded.MyLocation, contentDescription = "Mi Ubicación", tint = SapphireBlue)
            }
        },
        bottomBar = {
            Surface(
                color = SurfaceLight,
                shadowElevation = 24.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(SapphireBlueLight.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.LocationOn, null, tint = SapphireBlue)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enviar técnico a:", fontSize = 12.sp, color = TextTertiary)
                            Text(address, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    GradientButton(
                        text = "Confirmar Ubicación",
                        onClick = {
                            viewModel.selectLocation(markerPosition, address)
                            onNavigateToSummary()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    markerPosition = latLng
                    address = "Ubicación Seleccionada (${latLng.latitude.toString().take(6)}, ${latLng.longitude.toString().take(6)})"
                }
            ) {
                Marker(
                    state = MarkerState(position = markerPosition),
                    title = "Ubicación del Servicio",
                    snippet = address
                )
            }
        }
    }
}
