package com.hedvig.android.app.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation3.scene.SceneDecoratorStrategy
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NavigationBar
import com.hedvig.android.design.system.hedvig.NavigationRail
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.compose.NavigationSuiteType
import com.hedvig.android.navigation.compose.rememberNavSuiteSceneDecoratorStrategy

private val WindowSizeClass.navigationSuiteType: NavigationSuiteType
  get() = when (widthSizeClass) {
    WindowWidthSizeClass.Compact -> NavigationSuiteType.NavigationBar

    else -> when (heightSizeClass) {
      WindowHeightSizeClass.Expanded -> NavigationSuiteType.NavigationRailXLarge
      else -> NavigationSuiteType.NavigationRail
    }
  }

/**
 * Builds the navigation chrome (bottom bar / rail) as a [SceneDecoratorStrategy]. The badge and
 * top-level-tab state is read inside [chromeContent] on purpose, so changes recompose only the
 * chrome and never the hosted destination content.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun rememberHedvigChromeStrategy(
  hedvigAppState: HedvigAppState,
  sharedTransitionScope: SharedTransitionScope,
): List<SceneDecoratorStrategy<HedvigNavKey>> {
  return listOf(
    rememberNavSuiteSceneDecoratorStrategy<HedvigNavKey>(
      sharedTransitionScope = sharedTransitionScope,
      navigationSuiteType = { hedvigAppState.windowSizeClass.navigationSuiteType },
      chromeContent = {
        val showPaymentsBadge by hedvigAppState.showPaymentsBadge.collectAsState()
        val topLevelTabs by hedvigAppState.topLevelTabs.collectAsState()
        NavigationSuiteChrome(
          navigationSuiteType = hedvigAppState.windowSizeClass.navigationSuiteType,
          topLevelTabs = topLevelTabs,
          currentTopLevelTab = hedvigAppState.backstackController.currentTopLevel,
          onNavigateToTopLevelTab = hedvigAppState.backstackController::selectTopLevel,
          getShowNotificationBadge = { tab ->
            if (tab == TopLevelTab.Payments) showPaymentsBadge else false
          },
        )
      },
      upBarContent = {
        // Bare Up-bar shown only when a tab root is deep-linked alone; a back arrow with no title
        // is the intended minimal affordance.
        TopAppBarWithBack(
          onClick = { hedvigAppState.backstackController.navigateUp() },
        )
      },
      loneDeepLinkChrome = { hedvigAppState.backstackController.loneDeepLinkChrome },
    ),
  )
}

/**
 * Renders just the navigation chrome (bottom bar, rail, or extra-tall rail) for the given
 * [navigationSuiteType]. Show/hide and the content layout are owned by [NavSuiteSceneDecoratorStrategy],
 * which places this as a shared element inside each chrome-bearing scene — so this composable only
 * picks the right variant and never wraps the destination content.
 */
@Composable
internal fun NavigationSuiteChrome(
  navigationSuiteType: NavigationSuiteType,
  topLevelTabs: Set<TopLevelTab>,
  currentTopLevelTab: TopLevelTab?,
  onNavigateToTopLevelTab: (TopLevelTab) -> Unit,
  modifier: Modifier = Modifier,
  getShowNotificationBadge: (TopLevelTab) -> Boolean = { false },
) {
  when (navigationSuiteType) {
    NavigationSuiteType.NavigationBar -> NavigationBar(
      destinations = topLevelTabs,
      onNavigateToDestination = onNavigateToTopLevelTab,
      getIsCurrentlySelected = { it == currentTopLevelTab },
      getShowNotificationBadge = getShowNotificationBadge,
      modifier = modifier,
    )

    else -> NavigationRail(
      destinations = topLevelTabs,
      onNavigateToDestination = onNavigateToTopLevelTab,
      getIsCurrentlySelected = { it == currentTopLevelTab },
      isExtraTall = navigationSuiteType == NavigationSuiteType.NavigationRailXLarge,
      getShowNotificationBadge = getShowNotificationBadge,
      modifier = modifier,
    )
  }
}

@PreviewFontScale
@PreviewScreenSizes
@Composable
private fun PreviewNavigationSuiteChrome(
  @PreviewParameter(
    com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider::class,
  ) showBottomBar: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      NavigationSuiteChrome(
        navigationSuiteType = if (showBottomBar) {
          NavigationSuiteType.NavigationBar
        } else {
          NavigationSuiteType.NavigationRail
        },
        topLevelTabs = TopLevelTab.entries.toSet(),
        currentTopLevelTab = null,
        onNavigateToTopLevelTab = {},
      )
    }
  }
}
