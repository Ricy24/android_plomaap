package com.example.plomaap.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plomaap.ui.theme.*

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    gradientColors: List<Color> = listOf(GradientPrimaryStart, GradientPrimaryEnd)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Spring animation for bouncy premium feel
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f, 
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "buttonScale"
    )
    val shadowElevation by animateFloatAsState(
        targetValue = if (isPressed) 4f else 20f, 
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "buttonShadow"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .height(58.dp)
            .scale(scale)
            .shadow(
                elevation = shadowElevation.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = gradientColors.first().copy(alpha = 0.4f),
                spotColor = gradientColors.last().copy(alpha = 0.6f)
            ),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                brush = Brush.horizontalGradient(
                    colors = if (enabled && !isLoading) gradientColors else gradientColors.map { it.copy(alpha = 0.5f) }
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(26.dp), color = Color.White, strokeWidth = 3.dp)
            } else {
                Text(text = text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            }
        }
    }
}

@Composable
fun OutlinedPremiumButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f, 
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "outlinedScale"
    )
    val borderColor by animateColorAsState(targetValue = if (isPressed) SapphireBlue else DividerColor, label = "borderColor")
    val bgColor by animateColorAsState(targetValue = if (isPressed) SurfaceElevatedLight else SurfaceLight, label = "bgColor")

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(58.dp).scale(scale),
        shape = RoundedCornerShape(20.dp),
        border = ButtonDefaults.outlinedButtonBorder(true).copy(brush = Brush.horizontalGradient(listOf(borderColor, borderColor))),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = bgColor, contentColor = TextPrimary),
        interactionSource = interactionSource
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) { icon(); Spacer(modifier = Modifier.width(12.dp)) }
            Text(text = text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}
