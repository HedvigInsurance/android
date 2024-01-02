package com.hedvig.android.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.ImageLoader
import com.hedvig.android.app.navigation.HedvigNavHost
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.material3.motion.MotionTokens
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Market
import com.hedvig.android.navigation.activity.ActivityNavigator
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.TopLevelGraph
import com.kiwi.navigationcompose.typed.navigate

@Composable
internal fun HedvigApp(
  hedvigAppState: HedvigAppState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  activityNavigator: ActivityNavigator,
  getInitialTab: () -> TopLevelGraph?,
  clearInitialTab: () -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  market: Market,
  imageLoader: ImageLoader,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  LaunchedEffect(getInitialTab, clearInitialTab, hedvigAppState) {
    val initialTab: TopLevelGraph = getInitialTab() ?: return@LaunchedEffect
    clearInitialTab()
    hedvigAppState.navigateToTopLevelGraph(initialTab)
  }
  Surface(
    color = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column {
      Row(Modifier.weight(1f).fillMaxWidth()) {
        AnimatedVisibility(
          visible = hedvigAppState.shouldShowNavRail,
          enter = expandHorizontally(expandFrom = Alignment.End),
          exit = shrinkHorizontally(shrinkTowards = Alignment.End),
        ) {
          val topLevelGraphs by hedvigAppState.topLevelGraphs.collectAsStateWithLifecycle()
          val destinationsWithNotifications by hedvigAppState
            .topLevelGraphsWithNotifications.collectAsStateWithLifecycle()
          HedvigNavRail(
            destinations = topLevelGraphs,
            destinationsWithNotifications = destinationsWithNotifications,
            onNavigateToDestination = hedvigAppState::navigateToTopLevelGraph,
            currentDestination = hedvigAppState.currentDestination,
          )
        }
        HedvigNavHost(
          hedvigAppState = hedvigAppState,
          hedvigDeepLinkContainer = hedvigDeepLinkContainer,
          activityNavigator = activityNavigator,
          navigateToConnectPayment = { navigateToConnectPayment(hedvigAppState.navController, market) },
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
      AnimatedVisibility(
        visible = hedvigAppState.shouldShowBottomBar,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
      ) {
        val topLevelGraphs by hedvigAppState.topLevelGraphs.collectAsStateWithLifecycle()
        val destinationsWithNotifications by hedvigAppState
          .topLevelGraphsWithNotifications.collectAsStateWithLifecycle()
        HedvigBottomBar(
          destinations = topLevelGraphs,
          destinationsWithNotifications = destinationsWithNotifications,
          onNavigateToDestination = hedvigAppState::navigateToTopLevelGraph,
          currentDestination = hedvigAppState.currentDestination,
        )
      }
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

private fun navigateToConnectPayment(navController: NavController, market: Market) {
  when (market) {
    Market.SE -> navController.navigate(AppDestination.ConnectPayment)
    Market.NO,
    Market.DK,
    -> navController.navigate(AppDestination.ConnectPaymentAdyen)
  }
}
