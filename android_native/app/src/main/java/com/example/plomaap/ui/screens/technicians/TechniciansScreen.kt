package com.example.plomaap.ui.screens.technicians

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.data.model.Technician
import com.example.plomaap.ui.components.PremiumCard
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.MainViewModel

@Composable
fun TechniciansScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("Todos") }
    var searchQuery by remember { mutableStateOf("") }

    val filters = listOf("Todos", "Plomería", "Gas", "Instalación", "Top Rated")

    val filteredTechs = remember(uiState.technicians, selectedFilter, searchQuery) {
        uiState.technicians.filter { tech ->
            val matchesFilter = when (selectedFilter) {
                "Top Rated" -> (tech.profile?.rating ?: 0.0) >= 4.5
                "Todos" -> true
                else -> tech.profile?.specialties?.any {
                    it.contains(selectedFilter, ignoreCase = true)
                } ?: true
            }
            val matchesSearch = searchQuery.isEmpty() ||
                    tech.name.contains(searchQuery, ignoreCase = true) ||
                    tech.profile?.specialties?.any { it.contains(searchQuery, ignoreCase = true) } == true
            matchesFilter && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundLight)
    ) {
        // Header con gradiente
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(SapphireBlueDark, SapphireBlue)))
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        "Técnicos",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        "${uiState.technicians.size} profesionales disponibles",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Búsqueda
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar técnico...", color = TextTertiary) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Search, null, tint = SapphireBlue, modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Rounded.Close, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Filtros de especialidad
        item {
            Spacer(modifier = Modifier.height(20.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) SapphireBlue else SurfaceLight,
                        animationSpec = tween(200), label = "filterBg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else TextSecondary,
                        animationSpec = tween(200), label = "filterText"
                    )
                    Surface(
                        onClick = { selectedFilter = filter },
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        shadowElevation = if (isSelected) 6.dp else 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (filter == "Top Rated") {
                                Icon(Icons.Rounded.Star, null, tint = if (isSelected) Color.White else StarGold, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                filter,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Contador de resultados
        item {
            Text(
                "${filteredTechs.size} técnico${if (filteredTechs.size != 1) "s" else ""}${if (selectedFilter != "Todos") " • $selectedFilter" else ""}",
                fontSize = 13.sp,
                color = TextTertiary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Tarjetas de técnicos
        if (filteredTechs.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.Engineering, null, tint = TextTertiary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Sin técnicos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextSecondary)
                    Text("Intenta con otro filtro", fontSize = 14.sp, color = TextTertiary)
                }
            }
        } else {
            items(filteredTechs) { technician ->
                TechnicianCard(technician)
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
private fun TechnicianCard(technician: Technician) {
    val rating = technician.profile?.rating ?: 4.5
    val isTopRated = rating >= 4.5

    // Pulso animado para indicador "disponible"
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulseScale"
    )

    PremiumCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        elevation = 6.dp,
        cornerRadius = 24.dp
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar con iniciales + gradiente
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(SapphireBlueLight, SapphireBlueDark))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = technician.name.split(" ")
                            .take(2).joinToString("") { it.take(1) }.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                    // Badge disponible (animado)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .scale(pulseScale)
                                .background(JadeGreen.copy(alpha = 0.35f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.Center)
                                .background(JadeGreen, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            technician.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        if (isTopRated) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StarGold.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Rounded.Star, null, tint = StarGold, modifier = Modifier.size(12.dp))
                                    Text("TOP", fontSize = 10.sp, color = Color(0xFFF57F17), fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        technician.profile?.specialties?.joinToString(" • ") ?: "Plomero Experto",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Rating
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Rounded.Star, null, modifier = Modifier.size(16.dp), tint = StarGold)
                            Text(
                                "$rating",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                        // Disponible
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(JadeGreen, CircleShape))
                            Text("Disponible", fontSize = 12.sp, color = JadeGreen, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Botón contactar
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(46.dp)
                        .background(SapphireBlue, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Message,
                        contentDescription = "Contactar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Sección inferior: especialidades como chips
            if (technician.profile?.specialties?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    technician.profile.specialties.take(3).forEach { specialty ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SapphireBlue.copy(alpha = 0.08f)
                        ) {
                            Text(
                                specialty,
                                fontSize = 12.sp,
                                color = SapphireBlue,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
