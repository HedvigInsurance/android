package com.hedvig.android.app.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import com.hedvig.android.navigation.common.TopLevelGraph
import com.hedvig.android.navigation.compose.NavigationSuiteType
import com.hedvig.android.navigation.compose.rememberNavSuiteSceneDecoratorStrategy

/**
 * Builds the navigation chrome (bottom bar / rail) as a [SceneDecoratorStrategy]. The badge and
 * top-level-graph state is read inside [chromeContent] on purpose, so changes recompose only the
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
      navigationSuiteType = { hedvigAppState.navigationSuiteType },
      chromeContent = {
        val showPaymentsBadge by hedvigAppState.showPaymentsBadge.collectAsState()
        val topLevelGraphs by hedvigAppState.topLevelGraphs.collectAsState()
        NavigationSuiteChrome(
          navigationSuiteType = hedvigAppState.navigationSuiteType,
          topLevelGraphs = topLevelGraphs,
          currentTopLevelGraph = hedvigAppState.backstackController.currentTopLevel,
          onNavigateToTopLevelGraph = hedvigAppState.backstackController::selectTopLevel,
          getShowNotificationBadge = { graph ->
            if (graph == TopLevelGraph.Payments) showPaymentsBadge else false
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
  topLevelGraphs: Set<TopLevelGraph>,
  currentTopLevelGraph: TopLevelGraph?,
  onNavigateToTopLevelGraph: (TopLevelGraph) -> Unit,
  modifier: Modifier = Modifier,
  getShowNotificationBadge: (TopLevelGraph) -> Boolean = { false },
) {
  when (navigationSuiteType) {
    NavigationSuiteType.NavigationBar -> NavigationBar(
      destinations = topLevelGraphs,
      onNavigateToDestination = onNavigateToTopLevelGraph,
      getIsCurrentlySelected = { it == currentTopLevelGraph },
      getShowNotificationBadge = getShowNotificationBadge,
      modifier = modifier,
    )

    else -> NavigationRail(
      destinations = topLevelGraphs,
      onNavigateToDestination = onNavigateToTopLevelGraph,
      getIsCurrentlySelected = { it == currentTopLevelGraph },
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
        topLevelGraphs = TopLevelGraph.entries.toSet(),
        currentTopLevelGraph = null,
        onNavigateToTopLevelGraph = {},
      )
    }
  }
}
