package com.hedvig.android.app.ui

import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.media3.datasource.cache.SimpleCache
import coil3.ImageLoader
import com.hedvig.android.app.navigation.HedvigNavHost
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.design.system.hedvig.DemoModeLabel
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.activity.ExternalNavigator
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import hedvig.resources.Res
import hedvig.resources.EXIT_DEMO_MODE_BUTTON
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HedvigAppUi(
  hedvigAppState: HedvigAppState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  externalNavigator: ExternalNavigator,
  finishApp: () -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openUrl: (String) -> Unit,
  imageLoader: ImageLoader,
  simpleVideoCache: SimpleCache,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  demoManager: DemoManager,
  logoutUseCase: LogoutUseCase,
) {
  val isDemoMode by demoManager.isDemoMode().collectAsState(false)
  Box(Modifier.fillMaxSize()) {
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
      contentColor = HedvigTheme.colorScheme.textPrimary,
    ) {
      NavigationSuite(
        navigationSuiteType = hedvigAppState.navigationSuiteType,
        topLevelGraphs = hedvigAppState.topLevelGraphs.collectAsState().value,
        currentDestination = hedvigAppState.currentDestination,
        onNavigateToTopLevelGraph = hedvigAppState::navigateToTopLevelGraph,
      ) {
        HedvigNavHost(
          hedvigAppState = hedvigAppState,
          hedvigDeepLinkContainer = hedvigDeepLinkContainer,
          externalNavigator = externalNavigator,
          finishApp = finishApp,
          shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          openUrl = openUrl,
          imageLoader = imageLoader,
          simpleVideoCache = simpleVideoCache,
          languageService = languageService,
          hedvigBuildConstants = hedvigBuildConstants,
          modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .animatedNavigationBarInsetsConsumption(hedvigAppState),
        )
      }
    }
    if (isDemoMode) {
      DemoModeLabel(
        stringResource(Res.string.EXIT_DEMO_MODE_BUTTON),
        onButtonClick = { logoutUseCase.invoke() },
        modifier = Modifier
          .padding(start = 16.dp, end = 32.dp, bottom = 86.dp)
          .align(Alignment.BottomEnd)
          .windowInsetsPadding(WindowInsets.systemBars),
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
  val insetsToConsume = when (hedvigAppState.navigationSuiteType) {
    NavigationSuiteType.NavigationBar -> WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues(density)
    NavigationSuiteType.None -> PaddingValues(0.dp)
    NavigationSuiteType.NavigationRail,
    NavigationSuiteType.NavigationRailXLarge,
    ->
      WindowInsets.systemBars
        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Left)
        .asPaddingValues(density)

    else -> PaddingValues(0.dp)
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
        start = leftPadding.dp.coerceAtLeast(0.dp),
        end = rightPadding.dp.coerceAtLeast(0.dp),
        top = topPadding.dp.coerceAtLeast(0.dp),
        bottom = bottomPadding.dp.coerceAtLeast(0.dp),
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
