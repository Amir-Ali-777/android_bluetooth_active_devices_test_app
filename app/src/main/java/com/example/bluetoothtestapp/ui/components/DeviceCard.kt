package com.example.bluetoothtestapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.bluetoothtestapp.R
import com.example.bluetoothtestapp.model.AudioDevice
import com.example.bluetoothtestapp.model.AudioDeviceType
import com.example.bluetoothtestapp.ui.theme.ActivePillBorder
import com.example.bluetoothtestapp.ui.theme.ActivePillEnd
import com.example.bluetoothtestapp.ui.theme.ActivePillGlow
import com.example.bluetoothtestapp.ui.theme.ActivePillStart
import com.example.bluetoothtestapp.ui.theme.GlassGlowBlue
import com.example.bluetoothtestapp.ui.theme.GlassGlowViolet
import com.example.bluetoothtestapp.ui.theme.GlassLightBlue
import com.example.bluetoothtestapp.ui.theme.GlassLightViolet
import com.example.bluetoothtestapp.ui.theme.GlassRimBottom
import com.example.bluetoothtestapp.ui.theme.GlassRimMiddle
import com.example.bluetoothtestapp.ui.theme.GlassRimTop
import com.example.bluetoothtestapp.ui.theme.GlassTintBottom
import com.example.bluetoothtestapp.ui.theme.GlassTintTop
import com.example.bluetoothtestapp.ui.theme.InactivePillBorder
import com.example.bluetoothtestapp.ui.theme.InactivePillBottom
import com.example.bluetoothtestapp.ui.theme.InactivePillShadow
import com.example.bluetoothtestapp.ui.theme.InactivePillText
import com.example.bluetoothtestapp.ui.theme.InactivePillTop
import com.example.bluetoothtestapp.ui.theme.PillShine
import com.example.bluetoothtestapp.ui.theme.SilverDark
import com.example.bluetoothtestapp.ui.theme.SilverLight
import com.example.bluetoothtestapp.ui.theme.SilverMid
import com.example.bluetoothtestapp.ui.theme.TextMuted
import com.example.bluetoothtestapp.viewmodel.RouteStatus

private val CardCorner = 18.dp
private val PillHeight = 26.dp

private val CardGlow = Brush.linearGradient(
    0.0f to GlassGlowBlue,
    0.5f to GlassGlowBlue.copy(alpha = 0f),
    1.0f to GlassGlowViolet
)
private val ActivePillFill = Brush.horizontalGradient(listOf(ActivePillStart, ActivePillEnd))
private val InactivePillFill = Brush.verticalGradient(listOf(InactivePillTop, InactivePillBottom))
private val PillGloss = Brush.verticalGradient(0.0f to PillShine, 0.5f to PillShine.copy(alpha = 0f))
private val SilverFill = Brush.verticalGradient(listOf(SilverLight, SilverMid, SilverDark))

/**
 * A glass card for one device: icon, name, and two rows saying whether Android sends call audio
 * and media audio to it ([callStatus], [mediaStatus]).
 */
@Composable
fun DeviceCard(
    device: AudioDevice,
    callStatus: RouteStatus,
    mediaStatus: RouteStatus,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .size(width = 150.dp, height = 200.dp)
            .glow(brush = CardGlow, cornerRadius = CardCorner, blurRadius = 8.dp, edgeWidth = 6.dp)
            .glassBackground(cornerRadius = CardCorner)
    ) {
        Column(modifier = Modifier.padding(start = 18.dp, top = 12.dp, end = 18.dp)) {
            Icon(
                painter = painterResource(device.type.iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(48.dp)
                    .fillWith(SilverFill)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = device.name,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(device.statusRes),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 10.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RouteRow(label = stringResource(R.string.call), status = callStatus)
            RouteRow(label = stringResource(R.string.media), status = mediaStatus)
        }
    }
}

/** One row of the card: "Call" or "Media" followed by its status pill. */
@Composable
private fun RouteRow(label: String, status: RouteStatus) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted,
            modifier = Modifier.width(44.dp)
        )
        StatusPill(status = status, modifier = Modifier.weight(1f))
    }
}

/** Active: blue-violet with a glow. Inactive: grey. Not supported: the grey pill, dimmed. */
@Composable
private fun StatusPill(status: RouteStatus, modifier: Modifier = Modifier) {
    val isActive = status == RouteStatus.ACTIVE
    Box(
        modifier = modifier
            .height(PillHeight)
            .alpha(if (status == RouteStatus.NOT_SUPPORTED) 0.5f else 1f)
            .glow(
                brush = SolidColor(if (isActive) ActivePillGlow else InactivePillShadow),
                cornerRadius = PillHeight / 2,
                blurRadius = 8.dp
            )
            .background(if (isActive) ActivePillFill else InactivePillFill, CircleShape)
            .background(PillGloss, CircleShape)
            .border(1.dp, if (isActive) ActivePillBorder else InactivePillBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(
                when (status) {
                    RouteStatus.ACTIVE -> R.string.status_active
                    RouteStatus.INACTIVE -> R.string.status_inactive
                    RouteStatus.NOT_SUPPORTED -> R.string.status_not_supported
                }
            ),
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) Color.White else InactivePillText,
            maxLines = 1
        )
    }
}

/**
 * Frosted glass: a faint tint, blue light from the top-left corner, violet light from the
 * bottom-right corner and a lit rim. Gradients fade to a transparent copy of the same color
 * (not Color.Transparent, which is black) so the light doesn't turn grey as it fades.
 */
private fun Modifier.glassBackground(cornerRadius: Dp): Modifier = drawWithCache {
    val corner = CornerRadius(cornerRadius.toPx())
    val rimWidth = 1.dp.toPx()
    val tint = Brush.verticalGradient(listOf(GlassTintTop, GlassTintBottom))
    val blueLight = Brush.radialGradient(
        colors = listOf(GlassLightBlue, GlassLightBlue.copy(alpha = 0f)),
        center = Offset.Zero,
        radius = size.width * 1.1f
    )
    val violetLight = Brush.radialGradient(
        colors = listOf(GlassLightViolet, GlassLightViolet.copy(alpha = 0f)),
        center = Offset(size.width, size.height),
        radius = size.width * 0.85f
    )
    val rim = Brush.verticalGradient(listOf(GlassRimTop, GlassRimMiddle, GlassRimBottom))
    onDrawBehind {
        drawRoundRect(brush = tint, cornerRadius = corner)
        drawRoundRect(brush = blueLight, cornerRadius = corner)
        drawRoundRect(brush = violetLight, cornerRadius = corner)
        drawRoundRect(
            brush = rim,
            topLeft = Offset(rimWidth / 2, rimWidth / 2),
            size = Size(size.width - rimWidth, size.height - rimWidth),
            cornerRadius = CornerRadius(corner.x - rimWidth / 2),
            style = Stroke(width = rimWidth)
        )
    }
}

/** Paints [brush] only where the content (the icon) has pixels. */
private fun Modifier.fillWith(brush: Brush): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.SrcIn)
    }

/** The line under the name: host, Bluetooth, or wired with or without a microphone. */
private val AudioDevice.statusRes: Int
    get() = when {
        type == AudioDeviceType.HOST -> R.string.host_device
        type == AudioDeviceType.WIRED || type == AudioDeviceType.USB ->
            if (hasMicrophone) R.string.wired_with_mic else R.string.wired_no_mic
        else -> R.string.audio_connected
    }

val AudioDeviceType.iconRes: Int
    get() = when (this) {
        AudioDeviceType.HOST -> R.drawable.ic_smartphone
        AudioDeviceType.HEADSET -> R.drawable.ic_headphones
        AudioDeviceType.CAR -> R.drawable.ic_car
        AudioDeviceType.WIRED -> R.drawable.ic_headset_mic
        AudioDeviceType.USB -> R.drawable.ic_usb
    }
