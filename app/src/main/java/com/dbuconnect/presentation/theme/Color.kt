package com.dbuconnect.presentation.theme

import androidx.compose.ui.graphics.Color

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Global dark mode tracking state
var isAppInDarkTheme by mutableStateOf(false)

// Primary Green - main action color
val PrimaryGreen = Color(0xFF0F766E)
val PrimaryGreenLight = Color(0xFF14B8A6)
val PrimaryGreenDark = Color(0xFF0D5E58)
val PrimaryGreenContainer = Color(0xFFE6F5F3)

// Accent Pink - like action, match highlight ONLY
val AccentPink = Color(0xFFE11D48)
val AccentPinkLight = Color(0xFFFDA4AF)
val AccentPinkContainer = Color(0xFFFFF1F2)

// Backgrounds
val BackgroundPrimary: Color
    get() = if (isAppInDarkTheme) DarkBackground else Color(0xFFF8FAFC)

val BackgroundWhite: Color
    get() = if (isAppInDarkTheme) DarkSurface else Color(0xFFFFFFFF)

val BackgroundCard: Color
    get() = if (isAppInDarkTheme) DarkCard else Color(0xFFF1F5F9)

// Text
val TextPrimary: Color
    get() = if (isAppInDarkTheme) DarkTextPrimary else Color(0xFF111827)

val TextSecondary: Color
    get() = if (isAppInDarkTheme) DarkTextSecondary else Color(0xFF6B7280)

val TextTertiary: Color
    get() = if (isAppInDarkTheme) Color(0xFF6B7280) else Color(0xFF9CA3AF)

// Border
val BorderDefault: Color
    get() = if (isAppInDarkTheme) DarkBorder else Color(0xFFE5E7EB)

val BorderFocused: Color
    get() = if (isAppInDarkTheme) PrimaryGreenLight else Color(0xFF0F766E)

// Status
val StatusOnline = Color(0xFF22C55E)
val StatusError = Color(0xFFDC2626)
val StatusWarning = Color(0xFFF59E0B)

// Surface variants
val SurfaceElevated: Color
    get() = if (isAppInDarkTheme) DarkCard else Color(0xFFFFFFFF)

val SurfaceMuted: Color
    get() = if (isAppInDarkTheme) DarkCard else Color(0xFFF3F4F6)

// Brand
val BrandPurple = Color(0xFF4338CA)

// Dark theme
val DarkBackground = Color(0xFF111827)
val DarkSurface = Color(0xFF1F2937)
val DarkCard = Color(0xFF374151)
val DarkTextPrimary = Color(0xFFF9FAFB)
val DarkTextSecondary = Color(0xFF9CA3AF)
val DarkBorder = Color(0xFF4B5563)
