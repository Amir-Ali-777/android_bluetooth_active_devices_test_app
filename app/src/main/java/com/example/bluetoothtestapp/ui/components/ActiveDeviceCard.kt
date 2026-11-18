package com.example.bluetoothtestapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bluetoothtestapp.R
import com.example.bluetoothtestapp.model.AudioDevice
import com.example.bluetoothtestapp.model.AudioDeviceType
import com.example.bluetoothtestapp.ui.theme.FooterBorder
import com.example.bluetoothtestapp.ui.theme.FooterFill
import com.example.bluetoothtestapp.ui.theme.FooterGlowEnd
import com.example.bluetoothtestapp.ui.theme.FooterGlowStart
import com.example.bluetoothtestapp.ui.theme.FooterLabel
import com.example.bluetoothtestapp.ui.theme.FooterValue
import com.example.bluetoothtestapp.ui.theme.TextMuted

private val FooterCorner = 28.dp
private val FooterShape = RoundedCornerShape(FooterCorner)
private val FooterGlow = Brush.horizontalGradient(listOf(FooterGlowStart, FooterGlowEnd))

/**
 * The card at the bottom: the active communication device, which Android sends two-way audio
 * (calls, SCO) to, and the [microphone] it records from. That is the device's own microphone, or
 * the host's built-in one for output-only devices such as wired headphones. Media is not shown
 * here; the device cards show it.
 */
@Composable
fun ActiveDeviceCard(communicationDevice: AudioDevice, microphone: AudioDevice, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glow(brush = FooterGlow, cornerRadius = FooterCorner, blurRadius = 10.dp, edgeWidth = 6.dp)
            .background(FooterFill, FooterShape)
            .border(1.dp, FooterBorder, FooterShape)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.active_communication_device),
            style = MaterialTheme.typography.bodyLarge,
            color = FooterLabel
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(communicationDevice.type.iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = communicationDevice.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FooterValue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = communicationDevice.linkLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
                Text(
                    text = if (microphone.type == AudioDeviceType.HOST) {
                        stringResource(R.string.microphone_built_in)
                    } else {
                        stringResource(R.string.microphone, microphone.name)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** How two-way audio reaches the device: built in, wired, classic Bluetooth (HFP) or LE Audio. */
@Composable
private fun AudioDevice.linkLabel(): String = stringResource(
    when {
        type == AudioDeviceType.HOST -> R.string.link_host
        type == AudioDeviceType.WIRED -> R.string.link_wired
        type == AudioDeviceType.USB -> R.string.link_usb
        isLeAudio -> R.string.link_le_audio
        else -> R.string.link_classic
    }
)
