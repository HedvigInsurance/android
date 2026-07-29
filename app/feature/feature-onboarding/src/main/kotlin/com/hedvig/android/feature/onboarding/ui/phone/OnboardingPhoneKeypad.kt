package com.hedvig.android.feature.onboarding.ui.phone

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

private val KeySize = 36.dp
private val HorizontalGap = 11.dp
private val VerticalGap = 9.dp
private val GlyphFontSize = 20.sp
private const val HighlightScale = 1.12f

private val KeypadRows = listOf(
  listOf("1", "2", "3"),
  listOf("4", "5", "6"),
  listOf("7", "8", "9"),
  listOf("*", "0", "#"),
)

@Composable
internal fun OnboardingPhoneKeypad(modifier: Modifier = Modifier) {
  var highlightActive by remember { mutableStateOf(true) }
  LaunchedEffect(Unit) {
    delay(2.seconds)
    entered = true
  }
  KeypadGrid(highlightActive = highlightActive, modifier = modifier)
}

@Composable
private fun KeypadGrid(highlightActive: Boolean, modifier: Modifier = Modifier) {
  // Decorative artwork: freeze the font scale so the glyphs keep their designed size regardless of
  // the user's system font-size setting.
  val density = LocalDensity.current
  CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1f)) {
    Column(
      // Purely decorative: keep it out of the accessibility tree so it is not read out key by key.
      modifier = modifier.clearAndSetSemantics {},
      verticalArrangement = Arrangement.spacedBy(VerticalGap),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      for (row in KeypadRows) {
        Row(horizontalArrangement = Arrangement.spacedBy(HorizontalGap)) {
          for (label in row) {
            KeypadKey(label = label, highlighted = highlightActive && label == KeypadRows[2][0])
          }
        }
      }
    }
  }
}

@Composable
private fun KeypadKey(label: String, highlighted: Boolean) {
  val containerColor by animateColorAsState(
    targetValue = if (highlighted) {
      HedvigTheme.colorScheme.signalGreenFill
    } else {
      HedvigTheme.colorScheme.textPrimary.copy(alpha = 0.068f)
    },
    animationSpec = tween(MotionTokens.DurationLong1.toInt(), easing = MotionTokens.EasingEmphasizedCubicBezier),
    label = "keypad container colour",
  )
  val contentColor by animateColorAsState(
    targetValue = if (highlighted) HedvigTheme.colorScheme.signalGreenText else HedvigTheme.colorScheme.textPrimary,
    animationSpec = tween(MotionTokens.DurationLong1.toInt(), easing = MotionTokens.EasingEmphasizedCubicBezier),
    label = "keypad content colour",
  )
  val scale by animateFloatAsState(
    targetValue = if (highlighted) HighlightScale else 1f,
    animationSpec = tween(MotionTokens.DurationLong1.toInt(), easing = MotionTokens.EasingEmphasizedCubicBezier),
    label = "keypad scale",
  )
  Box(
    modifier = Modifier
      .size(KeySize)
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .background(containerColor, HedvigTheme.shapes.cornerXLarge),
    contentAlignment = Alignment.Center,
  ) {
    HedvigText(
      text = label,
      color = contentColor,
      fontSize = GlyphFontSize,
      lineHeight = GlyphFontSize,
      textAlign = TextAlign.Center,
      style = LocalTextStyle.current.copy(
        lineHeightStyle = LineHeightStyle(
          alignment = LineHeightStyle.Alignment.Center,
          trim = LineHeightStyle.Trim.Both,
        ),
        platformStyle = PlatformTextStyle(includeFontPadding = false),
      ),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingPhoneKeypadHighlighted() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      KeypadGrid(highlightActive = true, modifier = Modifier.padding(24.dp))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingPhoneKeypadResting() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      KeypadGrid(highlightActive = false, modifier = Modifier.padding(24.dp))
    }
  }
}
