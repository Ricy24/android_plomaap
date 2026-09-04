package com.example.plomaap.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.data.model.Service
import com.example.plomaap.ui.components.PremiumCard
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.MainViewModel

@Composable
fun ServicesScreen(
    viewModel: MainViewModel,
    onServiceClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf("Todo") }

    val categories = listOf(
        Pair("Todo", Icons.Rounded.Dashboard),
        Pair("Agua", Icons.Rounded.WaterDrop),
        Pair("Gas", Icons.Rounded.LocalFireDepartment),
        Pair("Reparación", Icons.Rounded.Handyman),
        Pair("Instalación", Icons.Rounded.Construction)
    )

    LazyColumn(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        // Hero Header Area
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(SapphireBlue, SapphireBlueLight)))
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Ubicación Actual", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.LocationOn, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Av. Principal 123", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Rounded.KeyboardArrowDown, null, tint = Color.White)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Notifications, null, tint = Color.White)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Hola, ¿Qué necesitas arreglar hoy?", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Banners Promocionales (Carrusel)
        item {
            Spacer(modifier = Modifier.height(24.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(3) { index ->
                    PromoBanner(index)
                }
            }
        }

        // Categorías Rápidas
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Categorías", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(categories) { category ->
                    CategoryItem(
                        name = category.first,
                        icon = category.second,
                        isSelected = selectedCategory == category.first,
                        onClick = { selectedCategory = category.first }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Servicios Destacados
        item {
            Text("Servicios Populares", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(uiState.services) { service ->
            ServiceCardPremium(service = service, onClick = { onServiceClick(service.id) })
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
private fun PromoBanner(index: Int) {
    val gradientColors = if (index % 2 == 0) listOf(SapphireBlue, SapphireBlueLight) else listOf(JadeGreen, Color(0xFF4CAF50))
    val title = if (index % 2 == 0) "Mantenimiento Preventivo" else "Reparaciones de Gas"
    val subtitle = if (index % 2 == 0) "20% de Descuento hoy" else "Certificados por la SEC"

    Box(
        modifier = Modifier
            .width(280.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(gradientColors))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("PROMO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        }
    }
}

@Composable
private fun CategoryItem(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) SapphireBlue else SurfaceLight
    val contentColor = if (isSelected) Color.White else TextSecondary
    val elevation = if (isSelected) 8.dp else 2.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Surface(
            shape = CircleShape,
            color = bgColor,
            shadowElevation = elevation,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = name, tint = contentColor, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) TextPrimary else TextSecondary)
    }
}

@Composable
private fun ServiceCardPremium(service: Service, onClick: () -> Unit) {
    val iconForCategory = when (service.icon) {
        "fa-droplet" -> Icons.Rounded.WaterDrop
        "fa-wrench" -> Icons.Rounded.Build
        "fa-toilet" -> Icons.Rounded.Plumbing
        "fa-shower" -> Icons.Rounded.Bathtub
        else -> Icons.Rounded.Handyman
    }
    
    PremiumCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).clickable { onClick() }, 
        elevation = 6.dp, 
        cornerRadius = 24.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(SapphireBlueLight.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconForCategory, contentDescription = null, tint = SapphireBlue, modifier = Modifier.size(36.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(service.description, maxLines = 2, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("$${String.format("%,.0f", service.base_price)}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = SapphireBlue)
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = SapphireBlue, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
