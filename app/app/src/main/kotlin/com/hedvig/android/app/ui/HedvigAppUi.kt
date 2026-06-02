package com.hedvig.android.app.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.datasource.cache.SimpleCache
import coil3.ImageLoader
import com.hedvig.android.app.GlobalHedvigSnackBar
import com.hedvig.android.app.navigation.HedvigNavHost
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.compose.ui.LocalSharedTransitionScope
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.design.system.hedvig.DemoModeLabel
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.rememberGlobalSnackBarState
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.activity.ExternalNavigator
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.rememberNavSuiteSceneDecoratorStrategy
import com.hedvig.android.navigation.core.TopLevelGraph
import hedvig.resources.EXIT_DEMO_MODE_BUTTON
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun HedvigAppUi(
  hedvigAppState: HedvigAppState,
  externalNavigator: ExternalNavigator,
  finishApp: () -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Unit,
  imageLoader: ImageLoader,
  simpleVideoCache: SimpleCache,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  demoManager: DemoManager,
  logoutUseCase: LogoutUseCase,
) {
  val isDemoMode by demoManager.isDemoMode().collectAsState(false)
  val showPaymentsBadge by hedvigAppState.showPaymentsBadge.collectAsState()
  val topLevelGraphs by hedvigAppState.topLevelGraphs.collectAsState()
  val globalSnackBarState = rememberGlobalSnackBarState()
  val sharedTransitionScope = LocalSharedTransitionScope.current
  val sceneDecoratorStrategies = if (sharedTransitionScope != null) {
    listOf(
      rememberNavSuiteSceneDecoratorStrategy<HedvigNavKey>(
        sharedTransitionScope = sharedTransitionScope,
        navigationSuiteType = { hedvigAppState.navigationSuiteType },
        chromeContent = {
          NavigationSuiteChrome(
            navigationSuiteType = hedvigAppState.navigationSuiteType,
            topLevelGraphs = topLevelGraphs,
            currentTopLevelGraph = hedvigAppState.currentTopLevelGraph,
            onNavigateToTopLevelGraph = hedvigAppState::navigateToTopLevelGraph,
            getShowNotificationBadge = { graph ->
              if (graph == TopLevelGraph.Payments) showPaymentsBadge else false
            },
          )
        },
      ),
    )
  } else {
    emptyList()
  }
  Box(Modifier.fillMaxSize()) {
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
      contentColor = HedvigTheme.colorScheme.textPrimary,
    ) {
      Box(propagateMinConstraints = true, modifier = Modifier.fillMaxSize()) {
        GlobalHedvigSnackBar(globalSnackBarState = globalSnackBarState)
        HedvigNavHost(
          hedvigAppState = hedvigAppState,
          globalSnackBarState = globalSnackBarState,
          externalNavigator = externalNavigator,
          finishApp = finishApp,
          shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          openUrl = openUrl,
          openCrossSellUrl = openCrossSellUrl,
          imageLoader = imageLoader,
          simpleVideoCache = simpleVideoCache,
          languageService = languageService,
          hedvigBuildConstants = hedvigBuildConstants,
          sharedTransitionScope = sharedTransitionScope,
          sceneDecoratorStrategies = sceneDecoratorStrategies,
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
