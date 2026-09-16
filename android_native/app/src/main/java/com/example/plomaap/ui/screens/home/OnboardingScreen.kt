package com.example.plomaap.ui.screens.home

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.data.model.OnboardingQuestionItem
import com.example.plomaap.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    questions: List<OnboardingQuestionItem>,
    isLoading: Boolean,
    isSubmitting: Boolean,
    onComplete: (Map<String, Any>) -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    val selectedAnswers = remember { mutableStateMapOf<String, Any>() }
    var currentStep by remember { mutableIntStateOf(0) }

    val currentQuestion = questions.getOrNull(currentStep)
    val totalSteps = questions.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Configuración de Hogar Digital",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Atrás", tint = TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = onSkip) {
                        Text("Omitir", color = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                questions.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay preguntas disponibles", color = TextSecondary)
                    }
                }
                currentQuestion != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        // Progress bar
                        LinearProgressIndicator(
                            progress = { (currentStep + 1).toFloat() / totalSteps.coerceAtLeast(1) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PrimaryBlue,
                            trackColor = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Paso ${currentStep + 1} de $totalSteps",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Question Title
                        Text(
                            text = currentQuestion.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        currentQuestion.subtitle?.let { sub ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Options list
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(currentQuestion.options) { option ->
                                val isSelected = when (val ans = selectedAnswers[currentQuestion.id]) {
                                    is List<*> -> ans.contains(option.code)
                                    is String -> ans == option.code
                                    else -> false
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (currentQuestion.is_multi_select) {
                                                val currentList = (selectedAnswers[currentQuestion.id] as? List<String>)?.toMutableList()
                                                    ?: mutableListOf()
                                                if (currentList.contains(option.code)) {
                                                    currentList.remove(option.code)
                                                } else {
                                                    currentList.add(option.code)
                                                }
                                                selectedAnswers[currentQuestion.id] = currentList
                                            } else {
                                                selectedAnswers[currentQuestion.id] = option.code
                                            }
                                        },
                                    color = if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else SurfaceWhite,
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) PrimaryBlue else Color(0xFFCBD5E1)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) PrimaryBlue else TextPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isSelected) {
                                            Icon(
                                                Icons.Rounded.CheckCircle,
                                                contentDescription = "Seleccionado",
                                                tint = PrimaryBlue
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Navigation Buttons
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (currentStep > 0) {
                                OutlinedButton(
                                    onClick = { currentStep-- },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Anterior")
                                }
                            }

                            Button(
                                onClick = {
                                    if (currentStep < totalSteps - 1) {
                                        currentStep++
                                    } else {
                                        onComplete(selectedAnswers.toMap())
                                    }
                                },
                                modifier = Modifier.weight(if (currentStep > 0) 1.5f else 1f),
                                enabled = !isSubmitting,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(if (currentStep == totalSteps - 1) "Finalizar y Crear Hogar" else "Siguiente")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
