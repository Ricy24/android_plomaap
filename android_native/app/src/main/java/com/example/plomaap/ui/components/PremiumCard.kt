package com.example.plomaap.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.plomaap.ui.theme.*

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 16.dp,
    contentPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = ShadowColor.copy(alpha = 0.05f),
                spotColor = ShadowColor.copy(alpha = 0.15f)
            )
            .clip(shape)
            .background(SurfaceLight)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(GlassWhite)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = shape
            )
            .padding(24.dp),
        content = content
    )
}

@Composable
fun ShimmerCard(modifier: Modifier = Modifier, cornerRadius: Dp = 20.dp) {
    val shimmerAnim = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = shimmerAnim.animateFloat(
        initialValue = 0f, targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val brush = Brush.horizontalGradient(
        colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
        startX = translateAnim.value - 200f, endX = translateAnim.value + 200f
    )
    Box(modifier = modifier.clip(RoundedCornerShape(cornerRadius)).background(brush))
}
