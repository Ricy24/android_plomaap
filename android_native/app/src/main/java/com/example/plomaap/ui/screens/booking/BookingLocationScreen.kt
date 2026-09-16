package com.example.plomaap.ui.screens.booking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.plomaap.data.model.UserAddress
import com.example.plomaap.ui.components.GradientButton
import com.example.plomaap.ui.components.booking.BookingProgressStepper
import com.example.plomaap.ui.navigation.Routes
import com.example.plomaap.ui.theme.*
import com.example.plomaap.utils.LocationUtils
import com.example.plomaap.viewmodel.BookingViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingLocationScreen(
    navController: NavController,
    viewModel: BookingViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isAddingNewLocation by remember { mutableStateOf(false) }

    // Map and places state
    val defaultLocation = LatLng(4.7110, -74.0721)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.selectedLocation ?: defaultLocation, 15f)
    }

    var customSearchText by remember { mutableStateOf("") }
    var referenceText by remember { mutableStateOf(uiState.addressReference) }
    var suggestions by remember { mutableStateOf(listOf<AutocompletePrediction>()) }
    var isSearchingPlaces by remember { mutableStateOf(false) }
    var sessionToken by remember { mutableStateOf(AutocompleteSessionToken.newInstance()) }

    val placesClient = remember(context) { LocationUtils.getPlacesClient(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        val latLng = LatLng(loc.latitude, loc.longitude)
                        coroutineScope.launch {
                            val addr = LocationUtils.getAddressFromCoordinates(context, latLng)
                            viewModel.selectCustomLocation(latLng, addr, referenceText)
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                        }
                    }
                }
            } catch (_: SecurityException) {}
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ubicación del Servicio",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceLight,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dirección seleccionada",
                            fontSize = 11.sp,
                            color = TextTertiary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = uiState.displayAddress,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    val canContinue = uiState.selectedAddress != null || uiState.customAddress.isNotBlank()

                    GradientButton(
                        text = "Continuar a Horario",
                        onClick = {
                            viewModel.updateAddressReference(referenceText)
                            navController.navigate(Routes.BOOKING_SCHEDULE)
                        },
                        enabled = canContinue,
                        modifier = Modifier.width(180.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Stepper: Paso 2
            item {
                BookingProgressStepper(currentStep = 2)
            }

            // Sección: Tus direcciones guardadas
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text(
                        text = "¿Dónde realizaremos el servicio?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Elige una de tus ubicaciones guardadas o especifica una nueva",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    if (uiState.savedAddresses.isNotEmpty()) {
                        Text(
                            text = "Tus ubicaciones",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.savedAddresses.forEach { addr ->
                                val isSelected = !isAddingNewLocation && uiState.selectedAddress?.id == addr.id
                                SavedAddressBookingCard(
                                    address = addr,
                                    isSelected = isSelected,
                                    onClick = {
                                        isAddingNewLocation = false
                                        viewModel.selectSavedAddress(addr)
                                        addr.reference?.let { referenceText = it }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Botón para ingresar nueva ubicación
                    OutlinedButton(
                        onClick = {
                            isAddingNewLocation = !isAddingNewLocation
                            if (isAddingNewLocation) {
                                customSearchText = ""
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SapphireBlue
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(SapphireBlue.copy(alpha = 0.5f))
                        )
                    ) {
                        Icon(
                            imageVector = if (isAddingNewLocation) Icons.Rounded.Close else Icons.Rounded.AddLocationAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAddingNewLocation) "Cerrar buscador de ubicación" else "+ Agregar / Buscar otra ubicación",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Sección desplegable de Nueva Ubicación con Google Places + Mapa
            if (isAddingNewLocation) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SapphireBlue.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Buscar dirección",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Buscador de Places
                            OutlinedTextField(
                                value = customSearchText,
                                onValueChange = { query ->
                                    customSearchText = query
                                    if (query.length >= 3 && placesClient != null) {
                                        isSearchingPlaces = true
                                        val req = FindAutocompletePredictionsRequest.builder()
                                            .setSessionToken(sessionToken)
                                            .setQuery(query)
                                            .setCountries(listOf("CO"))
                                            .build()
                                        placesClient.findAutocompletePredictions(req)
                                            .addOnSuccessListener { resp ->
                                                suggestions = resp.autocompletePredictions
                                                isSearchingPlaces = false
                                            }
                                            .addOnFailureListener { isSearchingPlaces = false }
                                    } else {
                                        suggestions = emptyList()
                                    }
                                },
                                placeholder = { Text("Ej: Calle 72 # 10-34", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = SapphireBlue) },
                                trailingIcon = {
                                    if (isSearchingPlaces) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Sugerencias de Places
                            if (suggestions.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceElevatedLight)
                                ) {
                                    Column {
                                        suggestions.take(3).forEach { prediction ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        val placeId = prediction.placeId
                                                        val fullText = prediction.getFullText(null).toString()
                                                        customSearchText = fullText
                                                        suggestions = emptyList()

                                                        placesClient?.let { client ->
                                                            val fetchReq = FetchPlaceRequest.builder(placeId, listOf(Place.Field.LAT_LNG))
                                                                .setSessionToken(sessionToken)
                                                                .build()
                                                            client.fetchPlace(fetchReq).addOnSuccessListener { resp ->
                                                                resp.place.latLng?.let { latLng ->
                                                                    viewModel.selectCustomLocation(latLng, fullText, referenceText)
                                                                    coroutineScope.launch {
                                                                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Rounded.LocationOn, null, tint = SapphireBlue, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = prediction.getFullText(null).toString(),
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Botón GPS actual
                            Button(
                                onClick = {
                                    val hasPerm = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasPerm) {
                                        try {
                                            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                                                if (loc != null) {
                                                    val latLng = LatLng(loc.latitude, loc.longitude)
                                                    coroutineScope.launch {
                                                        val addr = LocationUtils.getAddressFromCoordinates(context, latLng)
                                                        customSearchText = addr
                                                        viewModel.selectCustomLocation(latLng, addr, referenceText)
                                                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                                                    }
                                                }
                                            }
                                        } catch (_: SecurityException) {}
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue.copy(alpha = 0.1f), contentColor = SapphireBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.MyLocation, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Usar mi ubicación GPS actual", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Mapa interactivo de confirmación
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Text(
                        text = "Confirmación en mapa",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, DividerColor, RoundedCornerShape(18.dp))
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                            onMapClick = { latLng ->
                                coroutineScope.launch {
                                    val addr = LocationUtils.getAddressFromCoordinates(context, latLng)
                                    viewModel.selectCustomLocation(latLng, addr, referenceText)
                                }
                            }
                        ) {
                            uiState.selectedLocation?.let { loc ->
                                Marker(
                                    state = MarkerState(position = loc),
                                    title = uiState.displayAddress
                                )
                            }
                        }
                    }
                }
            }

            // Información complementaria (Apartamento / interior / instrucciones)
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text(
                        text = "Detalles de llegada (Apartamento / Casa / Torre)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = referenceText,
                        onValueChange = {
                            referenceText = it
                            viewModel.updateAddressReference(it)
                        },
                        placeholder = { Text("Ej: Apto 402, Torre 3, dejar pasar en portería", fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedAddressBookingCard(
    address: UserAddress,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = remember(address.label) {
        when {
            address.label.contains("Casa", ignoreCase = true) -> Icons.Rounded.Home
            address.label.contains("Trabajo", ignoreCase = true) || address.label.contains("Oficina", ignoreCase = true) -> Icons.Rounded.Work
            address.label.contains("Abuelita", ignoreCase = true) || address.label.contains("Amor", ignoreCase = true) -> Icons.Rounded.Favorite
            else -> Icons.Rounded.LocationOn
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) SapphireBlue.copy(alpha = 0.08f) else SurfaceLight)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) SapphireBlue else DividerColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(if (isSelected) SapphireBlue else SurfaceElevatedLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = address.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                if (address.is_default) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Predeterminada",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = JadeGreen,
                        modifier = Modifier
                            .background(JadeGreen.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = address.address,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!address.reference.isNullOrBlank()) {
                Text(
                    text = "Ref: ${address.reference}",
                    fontSize = 11.sp,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Seleccionada",
                tint = SapphireBlue,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
