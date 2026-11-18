package com.example.bluetoothtestapp.ui.theme

import androidx.compose.ui.graphics.Color

// Screen background gradient (top-left to bottom-right)
val BackgroundTop = Color(0xFF03041F)
val BackgroundMiddle = Color(0xFF0E0539)
val BackgroundLower = Color(0xFF22036E)
val BackgroundBottom = Color(0xFF4A0BAA)

// Text and icons
val TextLavender = Color(0xFF8C82C8)
val TextMuted = Color(0xFF9396D0)
val TextOnLight = Color(0xFF1C1B1F)
val InfoIconTint = Color(0xFFCFCCE0)

// Device cards (frosted glass, lit blue from the top-left and violet from the bottom-right)
val GlassTintTop = Color(0xFF8FA8E8).copy(alpha = 0.13f)
val GlassTintBottom = Color(0xFF6F5CE8).copy(alpha = 0.13f)
val GlassLightBlue = Color(0xFF8CC4FF).copy(alpha = 0.32f)
val GlassLightViolet = Color(0xFFA04DFF).copy(alpha = 0.65f)
val GlassRimTop = Color.White.copy(alpha = 0.35f)
val GlassRimMiddle = Color.White.copy(alpha = 0.12f)
val GlassRimBottom = Color(0xFFC6A8FF).copy(alpha = 0.35f)
val GlassGlowBlue = Color(0xFF5A8CFF).copy(alpha = 0.55f)
val GlassGlowViolet = Color(0xFFA04DFF).copy(alpha = 0.55f)

// Silver icons on the device cards
val SilverLight = Color.White
val SilverMid = Color(0xFFDCE0EA)
val SilverDark = Color(0xFF858AA2)

// Active / Inactive status pills
val ActivePillStart = Color(0xFF1565F0)
val ActivePillEnd = Color(0xFF6A3CF0)
val ActivePillBorder = Color(0xFF9CC8FF)
val ActivePillGlow = Color(0xFF2D6BFF).copy(alpha = 0.80f)
val InactivePillTop = Color(0xFF9B99A3)
val InactivePillBottom = Color(0xFF5C5A66)
val InactivePillBorder = Color.White.copy(alpha = 0.35f)
val InactivePillShadow = Color.Black.copy(alpha = 0.35f)
val InactivePillText = Color(0xFFEEEDF2)
val PillShine = Color.White.copy(alpha = 0.18f)

// Audio control buttons
val ButtonLight = Color(0xFFF8F8F8)
val ScoHighlight = Color(0xFF5F2CCB)
val ScoHighlightBorder = Color(0xFF8B6BE0)

// Glow around the audio control buttons, per button state
val ButtonGlowDisabled = Color(0xFFC9BFFF).copy(alpha = 0.35f)
val ButtonGlowEnabled = Color(0xFF7C4DFF).copy(alpha = 0.75f)
val ButtonGlowPressed = Color(0xFF4FC3FF)
val ButtonGlowHighlighted = Color(0xFF9D6BFF).copy(alpha = 0.90f)

// Active device card at the bottom
val FooterFill = Color.White.copy(alpha = 0.07f)
val FooterBorder = Color.White.copy(alpha = 0.10f)
val FooterGlowStart = Color(0xFF5A8CFF).copy(alpha = 0.70f)
val FooterGlowEnd = Color(0xFFA04DFF).copy(alpha = 0.70f)
val FooterLabel = Color(0xFFEDE7FF)
val FooterValue = Color(0xFFD9D0F5)
