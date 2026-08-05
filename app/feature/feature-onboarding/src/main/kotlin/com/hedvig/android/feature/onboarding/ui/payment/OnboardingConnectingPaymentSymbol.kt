package com.hedvig.android.feature.onboarding.ui.payment

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
import hedvig.resources.Res
import hedvig.resources.pillow_hedvig
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

private val SymbolSize = 74.dp
private val CheckBadgeSize = 24.dp
private val DotSize = 6.dp

// Timing for the connected-state graphic, grouped for easy tuning with design.
private const val CheckPopDelayMillis = 400L
private const val DotPulseDurationMillis = 600
private const val DotPulseStaggerMillis = 200

/**
 * The connected-state graphic on the connect-payment step: a "Bank" card linked by pulsing dots to
 * the Hedvig symbol, with a green checkmark that pops onto the symbol once the payment method is
 * connected. The pop plays only the first time the connected state is seen (see
 * [OnboardingPaymentScreen]); the dots pulse continuously.
 */
@Composable
internal fun OnboardingConnectingPaymentSymbol(
  animationAlreadyPlayed: Boolean,
  onAnimationCompleted: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var checkVisible by remember { mutableStateOf(animationAlreadyPlayed) }
  LaunchedEffect(Unit) {
    if (animationAlreadyPlayed) return@LaunchedEffect
    delay(CheckPopDelayMillis.milliseconds)
    checkVisible = true
    onAnimationCompleted()
  }
  ConnectingGraphic(checkVisible = checkVisible, modifier = modifier)
}

@Composable
private fun ConnectingGraphic(checkVisible: Boolean, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    BankCard()
    LoaderDots()
    HedvigSymbolWithCheck(checkVisible = checkVisible)
  }
}

@Composable
private fun BankCard() {
  Surface(
    shape = HedvigTheme.shapes.cornerXLarge,
    color = HedvigTheme.colorScheme.backgroundPrimary,
    border = HedvigTheme.colorScheme.borderSecondary,
    modifier = Modifier
      .size(SymbolSize)
      .hedvigDropShadow(HedvigTheme.shapes.cornerXLarge),
  ) {
    Box(contentAlignment = Alignment.Center) {
      // TODO: Add "Bank" / "Bank" to Lokalise
      HedvigText(text = "Bank", style = HedvigTheme.typography.bodySmall)
    }
  }
}

@Composable
private fun LoaderDots() {
  val transition = rememberInfiniteTransition(label = "connecting loader dots")
  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
    repeat(3) { index ->
      val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
          animation = tween(DotPulseDurationMillis, delayMillis = index * DotPulseStaggerMillis, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
        ),
        label = "connecting dot $index",
      )
      Box(
        Modifier
          .size(DotSize)
          .graphicsLayer { this.alpha = alpha }
          .clip(CircleShape)
          .background(HedvigTheme.colorScheme.fillPrimary),
      )
    }
  }
}

@Composable
private fun HedvigSymbolWithCheck(checkVisible: Boolean) {
  val checkScale by animateFloatAsState(
    targetValue = if (checkVisible) 1f else 0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
    label = "connected check scale",
  )
  val checkAlpha by animateFloatAsState(
    targetValue = if (checkVisible) 1f else 0f,
    animationSpec = tween(MotionTokens.DurationShort4.toInt(), easing = MotionTokens.EasingStandardCubicBezier),
    label = "connected check alpha",
  )
  Box {
    Image(
      painter = painterResource(Res.drawable.pillow_hedvig),
      contentDescription = null,
      modifier = Modifier.size(SymbolSize),
    )
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .offset(x = 6.dp, y = (-6).dp)
        .size(CheckBadgeSize)
        .graphicsLayer {
          scaleX = checkScale
          scaleY = checkScale
          alpha = checkAlpha
        }
        .clip(CircleShape)
        .background(HedvigTheme.colorScheme.signalGreenElement),
    ) {
      Icon(
        imageVector = HedvigIcons.Checkmark,
        contentDescription = null,
        tint = HedvigTheme.colorScheme.fillWhite,
        modifier = Modifier.size(16.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingConnectingPaymentSymbol() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ConnectingGraphic(checkVisible = true, modifier = Modifier.size(width = 210.dp, height = 74.dp))
    }
  }
}
