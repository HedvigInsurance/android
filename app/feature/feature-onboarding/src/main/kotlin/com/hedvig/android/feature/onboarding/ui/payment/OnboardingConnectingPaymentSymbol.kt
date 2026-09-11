package com.hedvig.android.feature.onboarding.ui.payment

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.PaymentMethodHandoverIllustration
import com.hedvig.android.design.system.hedvig.PaymentMethodMarkSize
import com.hedvig.android.design.system.hedvig.PaymentMethodPlusMark
import com.hedvig.android.design.system.hedvig.PaymentMethodTileBadge
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Trustly
import com.hedvig.android.design.system.hedvig.icon.colored.Swish
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
import com.hedvig.android.feature.onboarding.data.OnboardingPayinProvider
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

// How long after the graphic appears the connected check pops in.
private const val CheckPopDelayMillis = 400L

/**
 * The graphic on the connect-payment step: the method the member picked, linked by pulsing dots to
 * the Hedvig symbol. While [showCheck] is true (a payment method is connected) a green checkmark
 * pops onto the symbol. [showCheck] tracks the live status directly, so the check appears only while
 * the backend actually reports a connected method.
 */
@Composable
internal fun OnboardingConnectingPaymentSymbol(
  provider: OnboardingPayinProvider?,
  showCheck: Boolean,
  modifier: Modifier = Modifier,
) {
  // In a static preview the pop's delay never elapses, so seed the check as already shown there to
  // keep the connected states visible; at runtime it starts hidden and pops in.
  val inPreview = LocalInspectionMode.current
  var checkVisible by remember { mutableStateOf(inPreview && showCheck) }
  LaunchedEffect(showCheck) {
    if (!showCheck) {
      checkVisible = false
      return@LaunchedEffect
    }
    delay(CheckPopDelayMillis.milliseconds)
    checkVisible = true
  }
  ConnectingGraphic(provider = provider, checkVisible = checkVisible, modifier = modifier)
}

@Composable
private fun ConnectingGraphic(
  provider: OnboardingPayinProvider?,
  checkVisible: Boolean,
  modifier: Modifier = Modifier,
) {
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
  PaymentMethodHandoverIllustration(
    modifier = modifier,
    destinationBadge = {
      PaymentMethodTileBadge(
        icon = HedvigIcons.Checkmark,
        containerColor = HedvigTheme.colorScheme.signalGreenElement,
        contentColor = HedvigTheme.colorScheme.fillWhite,
        modifier = Modifier.graphicsLayer {
          scaleX = checkScale
          scaleY = checkScale
          alpha = checkAlpha
        },
      )
    },
    mark = { OnboardingPayinProviderMark(provider) },
  )
}

@Composable
private fun OnboardingPayinProviderMark(provider: OnboardingPayinProvider?) {
  val markModifier = Modifier.size(PaymentMethodMarkSize)
  when (provider) {
    OnboardingPayinProvider.Trustly -> Icon(HedvigIcons.Trustly, EmptyContentDescription, markModifier)
    OnboardingPayinProvider.Swish -> Image(HedvigIcons.Swish, EmptyContentDescription, markModifier)
    null -> PaymentMethodPlusMark(markModifier)
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingConnectingPaymentSymbol(
  @PreviewParameter(ConnectingSymbolProvider::class) provider: OnboardingPayinProvider?,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ConnectingGraphic(
        provider = provider,
        checkVisible = provider != null,
        modifier = Modifier.size(width = 210.dp, height = 74.dp),
      )
    }
  }
}

private class ConnectingSymbolProvider :
  CollectionPreviewParameterProvider<OnboardingPayinProvider?>(
    listOf(
      null,
      OnboardingPayinProvider.Trustly,
      OnboardingPayinProvider.Swish,
    ),
  )
