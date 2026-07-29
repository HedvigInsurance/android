package com.hedvig.android.feature.onboarding.ui.welcome

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
import hedvig.resources.Res
import hedvig.resources.onboarding_hedvig_symbol
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

private val SymbolSize = 128.dp
private val BadgeSize = 32.dp

/**
 * The Hedvig symbol on the welcome step, with a red notification badge that is absent on the first
 * frame and pops in a couple of seconds after the screen is shown, mirroring the verified badge on
 * the analytics-consent step.
 */
@Composable
internal fun OnboardingWelcomeSymbol(modifier: Modifier = Modifier) {
  var badgeVisible by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    delay(1.seconds)
    badgeVisible = true
  }
  WelcomeSymbol(badgeVisible = badgeVisible, modifier = modifier)
}

@Composable
private fun WelcomeSymbol(badgeVisible: Boolean, modifier: Modifier = Modifier) {
  val badgeScale by animateFloatAsState(
    targetValue = if (badgeVisible) 1f else 0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
    label = "welcome badge scale",
  )
  val badgeAlpha by animateFloatAsState(
    targetValue = if (badgeVisible) 1f else 0f,
    animationSpec = tween(MotionTokens.DurationShort4.toInt(), easing = MotionTokens.EasingStandardCubicBezier),
    label = "welcome badge alpha",
  )
  Box(
    modifier = modifier
      .size(SymbolSize)
      .clearAndSetSemantics {},
  ) {
    Image(
      painter = painterResource(Res.drawable.onboarding_hedvig_symbol),
      contentDescription = null,
      modifier = Modifier.matchParentSize(),
    )
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .size(BadgeSize)
        .graphicsLayer {
          scaleX = badgeScale
          scaleY = badgeScale
          alpha = badgeAlpha
        }
        .clip(CircleShape)
        .background(HedvigTheme.colorScheme.signalRedElement),
    ) {
      HedvigText(
        text = "1",
        color = HedvigTheme.colorScheme.textWhite,
        style = HedvigTheme.typography.label,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingWelcomeSymbolBadgeVisible() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      WelcomeSymbol(badgeVisible = true)
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingWelcomeSymbolBadgeHidden() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      WelcomeSymbol(badgeVisible = false)
    }
  }
}
