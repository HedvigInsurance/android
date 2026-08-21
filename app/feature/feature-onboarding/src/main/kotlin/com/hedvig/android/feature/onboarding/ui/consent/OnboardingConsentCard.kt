package com.hedvig.android.feature.onboarding.ui.consent

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
import hedvig.resources.Res
import hedvig.resources.onboarding_verified_card
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/** Which answer the card's corner badge is reporting. `null` renders no badge at all. */
internal enum class ConsentBadge {
  Accepted,
  Denied,
}

private val CardSize = 120.dp
private val BadgeSize = 32.dp
private val BadgeIconSize = 24.dp

// Straddles the card's top-right corner, half on and half off, so the badge reads as applied to the
// card rather than contained by it. The card is inset within the 120dp artwork, hence the nudge back
// in from the artwork edge.
private val BadgeOffsetX = (-12).dp
private val BadgeOffsetY = 8.dp

private val BadgeScaleInSpec = spring<Float>(
  dampingRatio = Spring.DampingRatioMediumBouncy,
  stiffness = Spring.StiffnessMediumLow,
)

// Scaling away keeps the same stiffness without the bounce, which would send the scale negative and
// briefly mirror the badge.
private val BadgeScaleOutSpec = spring<Float>(
  dampingRatio = Spring.DampingRatioNoBouncy,
  stiffness = Spring.StiffnessMediumLow,
)

private val BadgeAlphaSpec = tween<Float>(
  durationMillis = MotionTokens.DurationShort4.toInt(),
  easing = MotionTokens.EasingStandardCubicBezier,
)

/**
 * The card illustration on the analytics-consent step, with a badge popping in to confirm the answer:
 * a green checkmark for [ConsentBadge.Accepted], a red cross for [ConsentBadge.Denied]. It starts out
 * at whatever [badge] says, so an answer given on an earlier visit shows without animating.
 *
 * [onBadgeSettled] reports the badge the card has finished animating to, which lets the caller hold
 * navigation until the pop has played out. Swapping between two answers only exchanges the glyph and
 * the colour, so it settles immediately.
 */
@Composable
internal fun OnboardingConsentCard(
  badge: ConsentBadge?,
  onBadgeSettled: (badge: ConsentBadge?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val badgeScale = remember { Animatable(if (badge != null) 1f else 0f) }
  val badgeAlpha = remember { Animatable(if (badge != null) 1f else 0f) }
  val currentOnBadgeSettled by rememberUpdatedState(onBadgeSettled)
  LaunchedEffect(badge) {
    val visible = badge != null
    val target = if (visible) 1f else 0f
    coroutineScope {
      launch {
        badgeScale.animateTo(target, if (visible) BadgeScaleInSpec else BadgeScaleOutSpec)
      }
      launch {
        badgeAlpha.animateTo(target, BadgeAlphaSpec)
      }
    }
    currentOnBadgeSettled(badge)
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
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .offset(x = BadgeOffsetX, y = BadgeOffsetY)
        .size(BadgeSize)
        .graphicsLayer {
          scaleX = badgeScale.value
          scaleY = badgeScale.value
          alpha = badgeAlpha.value
        }
        .clip(CircleShape)
        .background(
          when (badge) {
            ConsentBadge.Denied -> HedvigTheme.colorScheme.signalRedElement

            // Holds the accepted colour while scaling away, so a cleared badge does not flash red.
            ConsentBadge.Accepted, null -> HedvigTheme.colorScheme.signalGreenElement
          },
        ),
    ) {
      Icon(
        imageVector = if (badge == ConsentBadge.Denied) HedvigIcons.Close else HedvigIcons.Checkmark,
        contentDescription = null,
        tint = HedvigTheme.colorScheme.fillWhite,
        modifier = Modifier.size(BadgeIconSize),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingConsentCardAccepted() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OnboardingConsentCard(
        badge = ConsentBadge.Accepted,
        onBadgeSettled = {},
        modifier = Modifier.padding(24.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingConsentCardDenied() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OnboardingConsentCard(
        badge = ConsentBadge.Denied,
        onBadgeSettled = {},
        modifier = Modifier.padding(24.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingConsentCardUndecided() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OnboardingConsentCard(
        badge = null,
        onBadgeSettled = {},
        modifier = Modifier.padding(24.dp),
      )
    }
  }
}
