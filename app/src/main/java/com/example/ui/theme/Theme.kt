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
    primary = AccentOrange,
    secondary = ButtonNumeric,
    tertiary = ButtonScientific,
    background = ObsidianDark,
    surface = SurfaceSlate,
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
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
