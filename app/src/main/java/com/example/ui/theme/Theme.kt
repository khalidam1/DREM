package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Crimson, 
    secondary = CrimsonDark, 
    tertiary = Crimson,
    background = PureBlack,
    surface = DarkGray,
    onPrimary = WhiteText,
    onSecondary = WhiteText,
    onTertiary = WhiteText,
    onBackground = WhiteText,
    onSurface = WhiteText
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  // Always use dark theme for this app
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
      content()
    }
  }
}
