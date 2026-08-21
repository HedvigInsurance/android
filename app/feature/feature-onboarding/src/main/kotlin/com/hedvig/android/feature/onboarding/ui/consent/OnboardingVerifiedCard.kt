package com.hedvig.android.feature.onboarding.ui.consent

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
import hedvig.resources.Res
import hedvig.resources.onboarding_verified_badge
import hedvig.resources.onboarding_verified_card
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

private val CardSize = 120.dp

// The checkmark is drawn in the same 120-unit space as the card; its circle is centred here, so the
// scaling pivots from the checkmark itself rather than the artwork centre.
private val CheckmarkTransformOrigin = TransformOrigin(92f / 120f, 24f / 120f)

private val CheckmarkScaleInSpec = spring<Float>(
  dampingRatio = Spring.DampingRatioMediumBouncy,
  stiffness = Spring.StiffnessMediumLow,
)

// Scaling away keeps the same stiffness without the bounce, which would send the scale negative and
// briefly mirror the artwork.
private val CheckmarkScaleOutSpec = spring<Float>(
  dampingRatio = Spring.DampingRatioNoBouncy,
  stiffness = Spring.StiffnessMediumLow,
)

private val CheckmarkAlphaSpec = tween<Float>(
  durationMillis = MotionTokens.DurationShort4.toInt(),
  easing = MotionTokens.EasingStandardCubicBezier,
)

/**
 * The card illustration on the analytics-consent step, with the green "verified" checkmark popping in
 * and out as [checkmarkVisible] changes. It starts out at whatever [checkmarkVisible] says, so an
 * already granted consent shows the checkmark without animating.
 *
 * [onCheckmarkSettled] reports the visibility the checkmark has finished animating to, which lets the
 * caller hold navigation until the pop has played out.
 */
@Composable
internal fun OnboardingVerifiedCard(
  checkmarkVisible: Boolean,
  onCheckmarkSettled: (checkmarkVisible: Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val checkmarkScale = remember { Animatable(if (checkmarkVisible) 1f else 0f) }
  val checkmarkAlpha = remember { Animatable(if (checkmarkVisible) 1f else 0f) }
  val currentOnCheckmarkSettled by rememberUpdatedState(onCheckmarkSettled)
  LaunchedEffect(checkmarkVisible) {
    val target = if (checkmarkVisible) 1f else 0f
    coroutineScope {
      launch {
        checkmarkScale.animateTo(target, if (checkmarkVisible) CheckmarkScaleInSpec else CheckmarkScaleOutSpec)
      }
      launch {
        checkmarkAlpha.animateTo(target, CheckmarkAlphaSpec)
      }
    }
    currentOnCheckmarkSettled(checkmarkVisible)
  }
  Box(
    modifier = modifier
      .size(CardSize)
      .clearAndSetSemantics {},
  ) {
    Spacer(
      // Soft shadow matching the card silhouette (the card is inset within the 120dp artwork).
      Modifier
        .matchParentSize()
        .padding(start = 8.dp, top = 16.dp, end = 20.dp, bottom = 12.dp)
        .hedvigDropShadow(RoundedCornerShape(24.dp)),
    )
    Image(
      painter = painterResource(Res.drawable.onboarding_verified_card),
      contentDescription = null,
      modifier = Modifier.matchParentSize(),
    )
    Image(
      painter = painterResource(Res.drawable.onboarding_verified_badge),
      contentDescription = null,
      modifier = Modifier
        .matchParentSize()
        .graphicsLayer {
          transformOrigin = CheckmarkTransformOrigin
          scaleX = checkmarkScale.value
          scaleY = checkmarkScale.value
          alpha = checkmarkAlpha.value
        },
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingVerifiedCardCheckmarkVisible() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OnboardingVerifiedCard(
        checkmarkVisible = true,
        onCheckmarkSettled = {},
        modifier = Modifier.padding(24.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingVerifiedCardCheckmarkHidden() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OnboardingVerifiedCard(
        checkmarkVisible = false,
        onCheckmarkSettled = {},
        modifier = Modifier.padding(24.dp),
      )
    }
  }
}
