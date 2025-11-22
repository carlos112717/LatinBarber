package com.latinbarber.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


// Esquema Oscuro (El principal para Latin Barber)
private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    secondary = GoldPrimary,
    tertiary = GoldPrimary,
    background = BlackBackground,
    surface = DarkSurface,
    onPrimary = BlackBackground, // El texto sobre botones dorados será negro
    onBackground = WhiteText,
    onSurface = WhiteText,
    error = ErrorRed
)

// Esquema Claro (Lo forzaremos a parecerse al oscuro o lo usaremos de respaldo)
private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    secondary = GoldPrimary,
    tertiary = GoldPrimary,
    background = BlackBackground, // Mantenemos fondo oscuro incluso en modo claro por diseño
    surface = DarkSurface,
    onPrimary = BlackBackground,
    onBackground = WhiteText,
    onSurface = WhiteText,
    error = ErrorRed
)

@Composable
fun LatinBarberTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // IMPORTANTE: Ponemos esto en 'false' para que NO tome los colores del fondo de pantalla del usuario
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Pintamos la barra de estado (donde está la hora y batería) del color de fondo
            window.statusBarColor = colorScheme.background.toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}