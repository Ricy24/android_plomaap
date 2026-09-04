package com.example.plomaap.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.plomaap.ui.theme.*

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(targetValue = when { isError -> ErrorRed; isFocused -> SapphireBlue; else -> DividerColor }, label = "textFieldBorder")
    val containerColor by animateColorAsState(targetValue = if (isFocused) SurfaceLight else SurfaceElevatedLight, label = "textFieldBg")

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = if (isFocused) SapphireBlue else TextTertiary) },
        modifier = modifier.fillMaxWidth().onFocusChanged { isFocused = it.isFocused },
        leadingIcon = if (leadingIcon != null) { { Icon(leadingIcon, null, tint = if (isFocused) SapphireBlue else TextTertiary) } } else null,
        trailingIcon = trailingIcon,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onImeAction() }, onNext = { onImeAction() }),
        singleLine = singleLine,
        isError = isError,
        supportingText = if (isError && errorMessage != null) { { Text(errorMessage, color = ErrorRed) } } else null,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            errorBorderColor = ErrorRed,
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            cursorColor = SapphireBlue,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedLabelColor = SapphireBlue,
            unfocusedLabelColor = TextTertiary,
        )
    )
}
