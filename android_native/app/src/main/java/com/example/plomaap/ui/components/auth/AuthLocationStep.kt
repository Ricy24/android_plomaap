package com.example.plomaap.ui.components.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.plomaap.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun AuthLocationStep(
    currentAddress: String,
    onAddressSelected: (String, LatLng?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Default Bogotá
    val defaultLatLng = LatLng(4.7110, -74.0721)
    var selectedLatLng by remember { mutableStateOf(defaultLatLng) }
    var displayedAddress by remember { mutableStateOf(currentAddress.ifEmpty { "Selecciona o busca tu ubicación" }) }
    var isLocating by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchSuggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 15f)
    }

    // Places Client
    val placesClient: PlacesClient? = remember(context) {
        try {
            if (Places.isInitialized()) Places.createClient(context) else null
        } catch (_: Exception) {
            null
        }
    }
    val sessionToken = remember { AutocompleteSessionToken.newInstance() }

    // Geocoding helper
    fun reverseGeocode(latLng: LatLng) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val addr = addresses[0]
                            val fullAddress = addr.getAddressLine(0) ?: "${addr.thoroughfare ?: ""} ${addr.subThoroughfare ?: ""}".trim()
                            coroutineScope.launch(Dispatchers.Main) {
                                displayedAddress = if (fullAddress.isNotBlank()) fullAddress else "Ubicación seleccionada"
                                onAddressSelected(displayedAddress, latLng)
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val fullAddress = addr.getAddressLine(0) ?: "${addr.thoroughfare ?: ""} ${addr.subThoroughfare ?: ""}".trim()
                        withContext(Dispatchers.Main) {
                            displayedAddress = if (fullAddress.isNotBlank()) fullAddress else "Ubicación seleccionada"
                            onAddressSelected(displayedAddress, latLng)
                        }
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    displayedAddress = "Lat: %.4f, Lng: %.4f".format(latLng.latitude, latLng.longitude)
                    onAddressSelected(displayedAddress, latLng)
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isLocating = true
            val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocation.lastLocation.addOnSuccessListener { loc ->
                    isLocating = false
                    if (loc != null) {
                        val newLatLng = LatLng(loc.latitude, loc.longitude)
                        selectedLatLng = newLatLng
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(newLatLng, 16f))
                        reverseGeocode(newLatLng)
                    }
                }.addOnFailureListener {
                    isLocating = false
                }
            } catch (_: SecurityException) {
                isLocating = false
            }
        }
    }

    fun requestLocation() {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine) {
            isLocating = true
            val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocation.lastLocation.addOnSuccessListener { loc ->
                    isLocating = false
                    if (loc != null) {
                        val newLatLng = LatLng(loc.latitude, loc.longitude)
                        selectedLatLng = newLatLng
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(newLatLng, 16f))
                        reverseGeocode(newLatLng)
                    }
                }.addOnFailureListener {
                    isLocating = false
                }
            } catch (_: SecurityException) {
                isLocating = false
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Quick Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { requestLocation() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SurfaceElevatedLight,
                    contentColor = SapphireBlue
                )
            ) {
                if (isLocating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = SapphireBlue)
                } else {
                    Icon(Icons.Rounded.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mi ubicación", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = { isSearching = !isSearching },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SurfaceElevatedLight,
                    contentColor = TextPrimary
                )
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isSearching) "Ocultar" else "Buscar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Search Bar with Places Autocomplete
        AnimatedVisibility(visible = isSearching) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        if (query.length >= 3 && placesClient != null) {
                            val request = FindAutocompletePredictionsRequest.builder()
                                .setSessionToken(sessionToken)
                                .setQuery(query)
                                .build()
                            placesClient.findAutocompletePredictions(request)
                                .addOnSuccessListener { response ->
                                    searchSuggestions = response.autocompletePredictions
                                }
                        } else {
                            searchSuggestions = emptyList()
                        }
                    },
                    placeholder = { Text("Escribe una dirección...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null, tint = SapphireBlue) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = ""; searchSuggestions = emptyList() }) {
                                Icon(Icons.Rounded.Clear, null, tint = TextTertiary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceElevatedLight,
                        focusedContainerColor = SurfaceLight,
                        focusedBorderColor = SapphireBlue
                    ),
                    singleLine = true
                )

                // Autocomplete Suggestions List
                if (searchSuggestions.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        shadowElevation = 6.dp,
                        color = SurfaceLight
                    ) {
                        Column {
                            searchSuggestions.take(4).forEach { prediction ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val placeId = prediction.placeId
                                            displayedAddress = prediction.getPrimaryText(null).toString()
                                            searchQuery = ""
                                            searchSuggestions = emptyList()
                                            isSearching = false

                                            // Fetch Place Details
                                            placesClient?.fetchPlace(
                                                FetchPlaceRequest.newInstance(
                                                    placeId,
                                                    listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
                                                )
                                            )?.addOnSuccessListener { fetchResponse ->
                                                fetchResponse.place.latLng?.let { latLng ->
                                                    selectedLatLng = latLng
                                                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                                                    val addr = fetchResponse.place.address ?: displayedAddress
                                                    displayedAddress = addr
                                                    onAddressSelected(addr, latLng)
                                                }
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.LocationOn, null, tint = SapphireBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            prediction.getPrimaryText(null).toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            prediction.getSecondaryText(null).toString(),
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                HorizontalDivider(color = DividerColor)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mini Interactive Map Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = false,
                    mapToolbarEnabled = false
                ),
                onMapClick = { latLng ->
                    selectedLatLng = latLng
                    cameraPositionState.move(CameraUpdateFactory.newLatLng(latLng))
                    reverseGeocode(latLng)
                }
            ) {
                Marker(
                    state = MarkerState(position = selectedLatLng),
                    title = "Ubicación del Servicio"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Elegant Selected Address Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceElevatedLight,
            border = ButtonDefaults.outlinedButtonBorder(true).copy(
                brush = androidx.compose.ui.graphics.SolidColor(SapphireBlue.copy(alpha = 0.2f))
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(SapphireBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = SapphireBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tu dirección para servicios",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SapphireBlue
                    )
                    Text(
                        text = displayedAddress,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
