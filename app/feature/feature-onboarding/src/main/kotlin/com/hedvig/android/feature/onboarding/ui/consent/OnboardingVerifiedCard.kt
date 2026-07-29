package com.hedvig.android.feature.onboarding.ui.consent

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
import hedvig.resources.Res
import hedvig.resources.onboarding_verified_badge
import hedvig.resources.onboarding_verified_card
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

private val CardSize = 120.dp

// The badge is drawn in the same 120-unit space as the card; its circle is centred here, so the
// scale-in pivots from the badge itself rather than the artwork centre.
private val BadgeTransformOrigin = TransformOrigin(92f / 120f, 24f / 120f)

/**
 * The card illustration on the analytics-consent step. The green "verified" badge is absent on the
 * first frame and pops in a couple of seconds after the screen is shown.
 */
@Composable
internal fun OnboardingVerifiedCard(modifier: Modifier = Modifier) {
  var badgeVisible by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    delay(2.seconds)
    badgeVisible = true
  }
  VerifiedCard(badgeVisible = badgeVisible, modifier = modifier)
}

@Composable
private fun VerifiedCard(badgeVisible: Boolean, modifier: Modifier = Modifier) {
  val badgeScale by animateFloatAsState(
    targetValue = if (badgeVisible) 1f else 0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
    label = "verified badge scale",
  )
  val badgeAlpha by animateFloatAsState(
    targetValue = if (badgeVisible) 1f else 0f,
    animationSpec = tween(MotionTokens.DurationShort4.toInt(), easing = MotionTokens.EasingStandardCubicBezier),
    label = "verified badge alpha",
  )
  Box(
    // Purely decorative: keep it out of the accessibility tree.
    modifier = modifier
      .size(CardSize)
      .clearAndSetSemantics {},
  ) {
    Spacer(
      // Soft shadow matching the card silhouette (the card is inset within the 120dp artwork).
      Modifier
        .matchParentSize()
        .padding(start = 8.dp, top = 16.dp, end = 20.dp, bottom = 12.dp)
        .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp)),
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
          transformOrigin = BadgeTransformOrigin
          scaleX = badgeScale
          scaleY = badgeScale
          alpha = badgeAlpha
        },
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingVerifiedCardBadgeVisible() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      VerifiedCard(badgeVisible = true, modifier = Modifier.padding(24.dp))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingVerifiedCardBadgeHidden() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      VerifiedCard(badgeVisible = false, modifier = Modifier.padding(24.dp))
    }
  }
}
