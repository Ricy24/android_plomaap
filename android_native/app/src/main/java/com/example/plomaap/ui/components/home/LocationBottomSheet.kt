package com.example.plomaap.ui.components.home

import android.Manifest
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.plomaap.data.model.UserAddress
import com.example.plomaap.ui.components.GradientButton
import com.example.plomaap.ui.theme.*
import com.example.plomaap.utils.LocationUtils
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationBottomSheet(
    activeAddress: UserAddress?,
    savedAddresses: List<UserAddress>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSelectAddress: (UserAddress) -> Unit,
    onSaveNewAddress: (label: String, address: String, lat: Double?, lng: Double?, placeId: String?, ref: String?) -> Unit,
    onDeleteAddress: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Mode: Selection mode vs Add Location mode
    var isAddingNewAddress by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = TextSecondary.copy(alpha = 0.3f),
                width = 36.dp,
                height = 4.dp
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            AnimatedContent(
                targetState = isAddingNewAddress,
                transitionSpec = {
                    if (targetState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "addressSheetContent"
            ) { adding ->
                if (!adding) {
                    AddressListContent(
                        activeAddress = activeAddress,
                        savedAddresses = savedAddresses,
                        onAddNewClick = { isAddingNewAddress = true },
                        onSelectAddress = {
                            onSelectAddress(it)
                            coroutineScope.launch { sheetState.hide(); onDismiss() }
                        },
                        onUseCurrentLocation = { latLng, addressStr ->
                            onSaveNewAddress("Ubicación actual", addressStr, latLng.latitude, latLng.longitude, null, null)
                            coroutineScope.launch { sheetState.hide(); onDismiss() }
                        },
                        onDeleteAddress = onDeleteAddress
                    )
                } else {
                    AddNewAddressContent(
                        onBack = { isAddingNewAddress = false },
                        onSave = { label, address, lat, lng, placeId, ref ->
                            onSaveNewAddress(label, address, lat, lng, placeId, ref)
                            coroutineScope.launch { sheetState.hide(); onDismiss() }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressListContent(
    activeAddress: UserAddress?,
    savedAddresses: List<UserAddress>,
    onAddNewClick: () -> Unit,
    onSelectAddress: (UserAddress) -> Unit,
    onUseCurrentLocation: (LatLng, String) -> Unit,
    onDeleteAddress: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLocatingCurrent by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isLocatingCurrent = true
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        val latLng = LatLng(loc.latitude, loc.longitude)
                        coroutineScope.launch {
                            val addr = LocationUtils.getAddressFromCoordinates(context, latLng)
                            isLocatingCurrent = false
                            onUseCurrentLocation(latLng, addr)
                        }
                    } else {
                        isLocatingCurrent = false
                    }
                }.addOnFailureListener {
                    isLocatingCurrent = false
                }
            } catch (e: SecurityException) {
                isLocatingCurrent = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "¿Dónde necesitas el servicio?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Selecciona una dirección guardada o agrega una nueva",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Botón: Usar mi ubicación actual
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SapphireBlue.copy(alpha = 0.08f))
                .clickable {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        isLocatingCurrent = true
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                                if (loc != null) {
                                    val latLng = LatLng(loc.latitude, loc.longitude)
                                    coroutineScope.launch {
                                        val addr = LocationUtils.getAddressFromCoordinates(context, latLng)
                                        isLocatingCurrent = false
                                        onUseCurrentLocation(latLng, addr)
                                    }
                                } else {
                                    isLocatingCurrent = false
                                }
                            }.addOnFailureListener {
                                isLocatingCurrent = false
                            }
                        } catch (e: SecurityException) {
                            isLocatingCurrent = false
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(SapphireBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isLocatingCurrent) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Usar mi ubicación actual",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SapphireBlue
                )
                Text(
                    text = if (isLocatingCurrent) "Detectando vía GPS..." else "Detectar con GPS del dispositivo",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Tus ubicaciones",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (savedAddresses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOff,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aún no tienes ubicaciones guardadas",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedAddresses, key = { it.id }) { address ->
                    val isSelected = address.id == activeAddress?.id
                    AddressItemRow(
                        address = address,
                        isSelected = isSelected,
                        onClick = { onSelectAddress(address) },
                        onDelete = { onDeleteAddress(address.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón: + Agregar nueva ubicación
        OutlinedButton(
            onClick = onAddNewClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SapphireBlue
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = androidx.compose.ui.graphics.SolidColor(SapphireBlue.copy(alpha = 0.5f))
            ),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Agregar nueva ubicación", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AddressItemRow(
    address: UserAddress,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
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
            .background(if (isSelected) SapphireBlue.copy(alpha = 0.08f) else SurfaceElevatedLight)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) SapphireBlue else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (isSelected) SapphireBlue else SurfaceLight,
                    CircleShape
                ),
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
                            .padding(horizontal = 5.dp, vertical = 2.dp)
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
                modifier = Modifier.size(20.dp)
            )
        } else {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Eliminar",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AddNewAddressContent(
    onBack: () -> Unit,
    onSave: (label: String, address: String, lat: Double?, lng: Double?, placeId: String?, ref: String?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val presetLabels = listOf("Casa", "Trabajo", "Abuelita", "Mi amor", "Oficina", "Otra")
    var selectedPreset by remember { mutableStateOf("Casa") }
    var customLabel by remember { mutableStateOf("") }

    var addressText by remember { mutableStateOf("") }
    var referenceText by remember { mutableStateOf("") }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var selectedPlaceId by remember { mutableStateOf<String?>(null) }

    // Autocomplete predictions state
    var suggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isSearchingPlaces by remember { mutableStateOf(false) }
    var sessionToken by remember { mutableStateOf(AutocompleteSessionToken.newInstance()) }

    val placesClient = remember(context) { LocationUtils.getPlacesClient(context) }

    // Map camera state
    val defaultCenter = LatLng(4.7110, -74.0721) // Bogotá fallback
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 14f)
    }

    fun searchAddressPredictions(query: String) {
        if (query.length < 3 || placesClient == null) {
            suggestions = emptyList()
            return
        }
        isSearchingPlaces = true
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(sessionToken)
            .setQuery(query)
            .setCountries(listOf("CO"))
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { resp ->
                suggestions = resp.autocompletePredictions
                isSearchingPlaces = false
            }
            .addOnFailureListener {
                isSearchingPlaces = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Header con botón atrás
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Nueva ubicación",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selector de etiquetas rápidas
        Text(text = "Nombre de la ubicación", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presetLabels) { label ->
                val isSelected = selectedPreset == label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) SapphireBlue else SurfaceElevatedLight)
                        .clickable {
                            selectedPreset = label
                            if (label != "Otra") customLabel = ""
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        if (selectedPreset == "Otra") {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customLabel,
                onValueChange = { customLabel = it },
                placeholder = { Text("Ej: Casa de mamá, Finca...", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Buscador de dirección
        Text(text = "Dirección", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = addressText,
            onValueChange = {
                addressText = it
                searchAddressPredictions(it)
            },
            placeholder = { Text("Buscar calle, carrera, barrio...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = SapphireBlue)
            },
            trailingIcon = {
                if (isSearchingPlaces) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else if (addressText.isNotEmpty()) {
                    IconButton(onClick = { addressText = ""; suggestions = emptyList() }) {
                        Icon(Icons.Rounded.Clear, contentDescription = null, tint = TextTertiary)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Lista de sugerencias de Places
        if (suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevatedLight)
            ) {
                Column {
                    suggestions.take(3).forEach { prediction ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val placeId = prediction.placeId
                                    selectedPlaceId = placeId
                                    addressText = prediction.getFullText(null).toString()
                                    suggestions = emptyList()

                                    // Fetch place coordinates
                                    placesClient?.let { client ->
                                        val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
                                        val fetchRequest = FetchPlaceRequest.builder(placeId, placeFields)
                                            .setSessionToken(sessionToken)
                                            .build()
                                        client.fetchPlace(fetchRequest)
                                            .addOnSuccessListener { fetchResp ->
                                                fetchResp.place.latLng?.let { latLng ->
                                                    selectedLatLng = latLng
                                                    coroutineScope.launch {
                                                        cameraPositionState.animate(
                                                            CameraUpdateFactory.newLatLngZoom(latLng, 16f)
                                                        )
                                                    }
                                                }
                                            }
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.LocationOn, null, tint = SapphireBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = prediction.getFullText(null).toString(),
                                fontSize = 12.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mapa de confirmación
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = false
                ),
                onMapClick = { latLng ->
                    selectedLatLng = latLng
                    coroutineScope.launch {
                        val resolved = LocationUtils.getAddressFromCoordinates(context, latLng)
                        addressText = resolved
                    }
                }
            ) {
                selectedLatLng?.let { pos ->
                    Marker(
                        state = MarkerState(position = pos),
                        title = if (selectedPreset == "Otra") customLabel else selectedPreset
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Campo de referencia adicional
        Text(text = "Apartamento / Referencia adicional", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = referenceText,
            onValueChange = { referenceText = it },
            placeholder = { Text("Ej: Apto 302, Torre 4, frente al parque", fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Guardar
        val finalLabel = if (selectedPreset == "Otra" && customLabel.isNotBlank()) customLabel.trim() else selectedPreset
        GradientButton(
            text = "Guardar ubicación",
            onClick = {
                if (addressText.isNotBlank()) {
                    onSave(
                        finalLabel,
                        addressText.trim(),
                        selectedLatLng?.latitude,
                        selectedLatLng?.longitude,
                        selectedPlaceId,
                        referenceText.trim().ifEmpty { null }
                    )
                }
            },
            enabled = addressText.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
