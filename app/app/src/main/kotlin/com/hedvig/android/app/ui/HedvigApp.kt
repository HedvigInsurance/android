package com.hedvig.android.app.ui

import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.app.navigation.HedvigNavHost
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.material3.motion.MotionTokens
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Market
import com.hedvig.android.navigation.activity.ActivityNavigator
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer

@Composable
internal fun HedvigApp(
  hedvigAppState: HedvigAppState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  activityNavigator: ActivityNavigator,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  market: Market,
  imageLoader: ImageLoader,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier.fillMaxSize(),
  ) {
    NavigationSuite(hedvigAppState = hedvigAppState) {
      HedvigNavHost(
        hedvigAppState = hedvigAppState,
        hedvigDeepLinkContainer = hedvigDeepLinkContainer,
        activityNavigator = activityNavigator,
        shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
        imageLoader = imageLoader,
        market = market,
        languageService = languageService,
        hedvigBuildConstants = hedvigBuildConstants,
        modifier = Modifier
          .fillMaxHeight()
          .weight(1f)
          .animatedNavigationBarInsetsConsumption(hedvigAppState),
      )
    }
  }
}

/**
 * Animates how we consume the insets, so that when we leave a screen which does not consume any insets, and we enter a
 * screen which does (by showing the bottom nav for example) then we don't want the outgoing screen to have its
 * contents snap to the bounds of the new insets immediately. This animation makes this visual effect look much more
 * fluid.
 */
@OptIn(ExperimentalLayoutApi::class)
private fun Modifier.animatedNavigationBarInsetsConsumption(hedvigAppState: HedvigAppState) = composed {
  val density = LocalDensity.current
  val insetsToConsume = if (hedvigAppState.shouldShowBottomBar) {
    WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues(density)
  } else if (hedvigAppState.shouldShowNavRail) {
    WindowInsets.systemBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Left).asPaddingValues(density)
  } else {
    PaddingValues(0.dp)
  }

  val paddingValuesVectorConverter: TwoWayConverter<PaddingValues, AnimationVector4D> = TwoWayConverter(
    convertToVector = { paddingValues ->
      AnimationVector4D(
        paddingValues.calculateLeftPadding(LayoutDirection.Ltr).value,
        paddingValues.calculateRightPadding(LayoutDirection.Ltr).value,
        paddingValues.calculateTopPadding().value,
        paddingValues.calculateBottomPadding().value,
      )
    },
    convertFromVector = { animationVector4d ->
      val leftPadding = animationVector4d.v1
      val rightPadding = animationVector4d.v2
      val topPadding = animationVector4d.v3
      val bottomPadding = animationVector4d.v4
      PaddingValues(
        start = leftPadding.dp,
        end = rightPadding.dp,
        top = topPadding.dp,
        bottom = bottomPadding.dp,
      )
    },
  )
  val animatedInsetsToConsume: PaddingValues by animateValueAsState(
    targetValue = insetsToConsume,
    typeConverter = paddingValuesVectorConverter,
    animationSpec = tween(MotionTokens.DurationMedium1.toInt()),
    label = "Padding values inset animation",
  )
  consumeWindowInsets(animatedInsetsToConsume)
}
