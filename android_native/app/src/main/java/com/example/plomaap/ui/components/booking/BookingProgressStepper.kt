package com.example.plomaap.ui.components.booking

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.ui.theme.*

@Composable
fun BookingProgressStepper(
    currentStep: Int, // 1 to 5
    modifier: Modifier = Modifier
) {
    val steps = listOf("Servicio", "Ubicación", "Horario", "Técnico", "Resumen")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, title ->
                val stepNum = index + 1
                val isCompleted = stepNum < currentStep
                val isCurrent = stepNum == currentStep

                val circleBg by animateColorAsState(
                    targetValue = when {
                        isCompleted -> JadeGreen
                        isCurrent -> SapphireBlue
                        else -> SurfaceElevatedLight
                    },
                    label = "circleBg"
                )

                val textColor = when {
                    isCompleted -> JadeGreen
                    isCurrent -> SapphireBlue
                    else -> TextTertiary
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(circleBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "$stepNum",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.White else TextTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = title,
                        fontSize = 9.sp,
                        fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Normal,
                        color = textColor,
                        maxLines = 1
                    )
                }

                // Connector line between steps
                if (index < steps.size - 1) {
                    val lineBg by animateColorAsState(
                        targetValue = if (stepNum < currentStep) JadeGreen else DividerColor,
                        label = "lineBg"
                    )
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .weight(0.7f)
                            .padding(bottom = 14.dp)
                            .background(lineBg)
                    )
                }
            }
        }
    }
}
