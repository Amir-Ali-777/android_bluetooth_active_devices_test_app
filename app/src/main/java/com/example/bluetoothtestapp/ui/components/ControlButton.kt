package com.example.bluetoothtestapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.bluetoothtestapp.ui.theme.ButtonGlowDisabled
import com.example.bluetoothtestapp.ui.theme.ButtonGlowEnabled
import com.example.bluetoothtestapp.ui.theme.ButtonGlowHighlighted
import com.example.bluetoothtestapp.ui.theme.ButtonGlowPressed
import com.example.bluetoothtestapp.ui.theme.ButtonLight
import com.example.bluetoothtestapp.ui.theme.ScoHighlight
import com.example.bluetoothtestapp.ui.theme.ScoHighlightBorder
import com.example.bluetoothtestapp.ui.theme.TextOnLight

private val ButtonCorner = 18.dp
private val ButtonShape = RoundedCornerShape(ButtonCorner)

/**
 * [highlighted] shows the "in progress" look (purple) used while SCO is active or a
 * recording is playing; a highlighted button is never clickable.
 */
@Composable
fun ControlButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val glowColor by animateColorAsState(
        targetValue = when {
            highlighted -> ButtonGlowHighlighted
            !enabled -> ButtonGlowDisabled
            isPressed -> ButtonGlowPressed
            else -> ButtonGlowEnabled
        },
        label = "buttonGlow"
    )
    Button(
        onClick = onClick,
        enabled = enabled && !highlighted,
        modifier = modifier
            .height(48.dp)
            .glow(brush = SolidColor(glowColor), cornerRadius = ButtonCorner, blurRadius = 3.dp, edgeWidth = 2.dp),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = ButtonLight,
            contentColor = TextOnLight,
            disabledContainerColor = if (highlighted) ScoHighlight else Color.White.copy(alpha = 0.16f),
            disabledContentColor = if (highlighted) Color.White else Color.White.copy(alpha = 0.5f)
        ),
        border = if (highlighted) BorderStroke(1.dp, ScoHighlightBorder) else null,
        interactionSource = interactionSource
    ) {
        Text(text = text)
    }
}
