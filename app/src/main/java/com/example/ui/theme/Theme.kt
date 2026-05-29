package com.example.ui.theme

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalAppContrast = staticCompositionLocalOf { 0.0f }

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFD0BCFF),        // Vibrant Lavender/Orange operator
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFF2B2930),       // Number buttons standard
    onSecondary = Color(0xFFE6E1E5),
    secondaryContainer = Color(0xFF4A4458), // Cool action keys (AC, DEL, %)
    onSecondaryContainer = Color(0xFFE8DEF8), // System actions text
    tertiary = Color(0xFFD0BCFF),       // Advanced operations text
    onTertiary = Color(0xFF381E72),
    background = Color(0xFF131215),      // Dark obsidian background
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1D1B20),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF25232A),  // Interactive keypad area
    onSurfaceVariant = Color(0xFFCAC4D0),
    tertiaryContainer = Color(0xFFEADDFF), // Equals key background
    onTertiaryContainer = Color(0xFF21005D),
    errorContainer = Color(0xFF850A24),    // Delete key / Alert
    onErrorContainer = Color(0xFFFFDAD6)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFFF3F3F5),       // Number buttons standard
    onSecondary = Color(0xFF1C1B1F),
    secondaryContainer = Color(0xFFE8DEF8), // Cool action keys (AC, DEL, %)
    onSecondaryContainer = Color(0xFF381E72),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFBF8FD),      // Light elegant background
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFECE6F0),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF3EDF7),  // Interactive keypad area
    onSurfaceVariant = Color(0xFF49454F),
    tertiaryContainer = Color(0xFFFFD8E4), // Equals key background
    onTertiaryContainer = Color(0xFF31111D),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val contrast = if (Build.VERSION.SDK_INT >= 34) {
    remember(context) {
      try {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE)
        if (uiModeManager != null) {
          val method = uiModeManager.javaClass.getMethod("getContrast")
          val result = method.invoke(uiModeManager)
          if (result is Float) result else 0.0f
        } else {
          0.0f
        }
      } catch (e: Throwable) {
        0.0f
      }
    }
  } else {
    0.0f
  }

  var colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  // Adjust Scheme dynamically for High/Medium Contrast selected by user on Android 15/16 devices
  if (contrast > 0.01f) {
    colorScheme = if (darkTheme) {
      colorScheme.copy(
        background = Color.Black,
        surface = Color(0xFF16151A),
        onBackground = Color.White,
        onSurface = Color.White,
        primary = Color(0xFFEADDFF),
        onPrimary = Color.Black
      )
    } else {
      colorScheme.copy(
        background = Color.White,
        surface = Color(0xFFF3F3F5),
        onBackground = Color.Black,
        onSurface = Color.Black,
        primary = Color(0xFF6750A4),
        onPrimary = Color.White
      )
    }
  }

  CompositionLocalProvider(LocalAppContrast provides contrast) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
