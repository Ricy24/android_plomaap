package com.example.plomaap.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.data.model.NluUnderstandResponse
import com.example.plomaap.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDiagnosisBottomSheet(
    initialQuery: String,
    diagnosis: NluUnderstandResponse?,
    isDiagnosing: Boolean,
    onDismiss: () -> Unit,
    onRunDiagnosis: (String) -> Unit,
    onBookService: (serviceId: Int, notes: String) -> Unit
) {
    var queryText by remember { mutableStateOf(initialQuery.ifBlank { "" }) }

    val quickPrompts = listOf(
        "Se sale agua debajo del lavamanos",
        "El inodoro se tapó y el agua se rebosa",
        "Instalar un calentador de gas nuevo",
        "Olor a gas cerca del calentador"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SapphireBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = SapphireBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Diagnóstico con IA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Gemini NLU • Análisis de fallas y urgencia",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Cerrar", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text Input
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                placeholder = { Text("Ej: Tengo una fuga en la tubería bajo el lavaplatos...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SapphireBlue,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickPrompts) { prompt ->
                    SuggestionChip(
                        onClick = {
                            queryText = prompt
                            onRunDiagnosis(prompt)
                        },
                        label = { Text(prompt, fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Análisis
            Button(
                onClick = { onRunDiagnosis(queryText) },
                enabled = queryText.isNotBlank() && !isDiagnosing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue)
            ) {
                if (isDiagnosing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analizando lenguaje con Gemini...", color = Color.White)
                } else {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Diagnosticar problema")
                }
            }

            // Resultado del Diagnóstico
            if (diagnosis != null && diagnosis.success) {
                Spacer(modifier = Modifier.height(20.dp))

                val urgencyColor = when (diagnosis.urgency?.lowercase()) {
                    "emergency" -> Color(0xFFD32F2F)
                    "high" -> Color(0xFFE65100)
                    "medium" -> Color(0xFF0288D1)
                    else -> Color(0xFF2E7D32)
                }

                val urgencyLabel = when (diagnosis.urgency?.lowercase()) {
                    "emergency" -> "EMERGENCIA CRÍTICA"
                    "high" -> "URGENCIA ALTA"
                    "medium" -> "URGENCIA MEDIA"
                    else -> "MANTENIMIENTO PROGRAMABLE"
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Badge de Urgencia
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = urgencyColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = urgencyLabel,
                                    color = urgencyColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "Confianza: ${(diagnosis.confidence * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Resumen de activo y problema
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Build, contentDescription = null, tint = SapphireBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Activo: ${diagnosis.asset_name ?: diagnosis.asset ?: "Plomería General"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        if (diagnosis.symptoms.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Síntomas: ${diagnosis.symptoms.joinToString(", ")}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        // Servicio recomendado
                        val topService = diagnosis.recommended_services.firstOrNull()
                        if (topService != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Servicio recomendado:",
                                fontSize = 11.sp,
                                color = TextTertiary,
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = topService.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = topService.reason,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }

                                val priceStr = try {
                                    val f = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                                    f.maximumFractionDigits = 0
                                    f.format(topService.base_price)
                                } catch (_: Exception) {
                                    "$${topService.base_price.toInt()}"
                                }

                                Text(
                                    text = priceStr,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SapphireBlue
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val notes = diagnosis.suggested_action?.prefill_notes ?: diagnosis.original_text ?: ""
                                    onBookService(topService.id, notes)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Agendar este servicio asistido", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
