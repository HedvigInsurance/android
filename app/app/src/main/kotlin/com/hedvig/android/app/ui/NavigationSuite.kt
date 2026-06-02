package com.hedvig.android.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NavigationBar
import com.hedvig.android.design.system.hedvig.NavigationRail
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.navigation.core.TopLevelGraph

@Composable
internal fun NavigationSuite(
  navigationSuiteType: NavigationSuiteType,
  topLevelGraphs: Set<TopLevelGraph>,
  currentTopLevelGraph: TopLevelGraph?,
  onNavigateToTopLevelGraph: (TopLevelGraph) -> Unit,
  modifier: Modifier = Modifier,
  getShowNotificationBadge: (TopLevelGraph) -> Boolean = { false },
  content: @Composable RowScope.() -> Unit,
) {
  Column(modifier) {
    Row(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
    ) {
      AnimatedVisibility(
        visible = navigationSuiteType == NavigationSuiteType.NavigationRail ||
          navigationSuiteType == NavigationSuiteType.NavigationRailXLarge,
        enter = expandHorizontally(expandFrom = Alignment.End),
        exit = shrinkHorizontally(shrinkTowards = Alignment.End),
      ) {
        NavigationRail(
          destinations = topLevelGraphs,
          onNavigateToDestination = onNavigateToTopLevelGraph,
          getIsCurrentlySelected = { it == currentTopLevelGraph },
          isExtraTall = navigationSuiteType == NavigationSuiteType.NavigationRailXLarge,
          getShowNotificationBadge = getShowNotificationBadge,
        )
      }
      content()
    }
    AnimatedVisibility(
      visible = navigationSuiteType == NavigationSuiteType.NavigationBar,
      enter = expandVertically(expandFrom = Alignment.Top),
      exit = shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
      NavigationBar(
        destinations = topLevelGraphs,
        onNavigateToDestination = onNavigateToTopLevelGraph,
        getIsCurrentlySelected = { it == currentTopLevelGraph },
        getShowNotificationBadge = getShowNotificationBadge,
      )
    }
  }
}

@PreviewFontScale
@PreviewScreenSizes
@Composable
private fun PreviewNavigationSuite(
  @PreviewParameter(
    com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider::class,
  ) showBottomBar: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      NavigationSuite(
        navigationSuiteType = if (showBottomBar) {
          NavigationSuiteType.NavigationBar
        } else {
          NavigationSuiteType.NavigationRail
        },
        topLevelGraphs = TopLevelGraph.entries.toSet(),
        currentTopLevelGraph = null,
        onNavigateToTopLevelGraph = {},
      ) {
        HedvigText("Content")
      }
    }
  }
}
