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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.NavDestination
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.design.system.hedvig.NavigationBar
import com.hedvig.android.design.system.hedvig.NavigationRail
import com.hedvig.android.navigation.core.TopLevelGraph

@Composable
internal fun NavigationSuite(
  navigationSuiteType: NavigationSuiteType,
  topLevelGraphs: Set<TopLevelGraph>,
  topLevelGraphsWithNotifications: Set<TopLevelGraph>,
  currentDestination: NavDestination?,
  onNavigateToTopLevelGraph: (TopLevelGraph) -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit,
) {
  Column(modifier) {
    Row(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
    ) {
      AnimatedVisibility(
        visible = navigationSuiteType == NavigationSuiteType.NavigationRail,
        enter = expandHorizontally(expandFrom = Alignment.End),
        exit = shrinkHorizontally(shrinkTowards = Alignment.End),
      ) {
        NavigationRail(
          destinations = topLevelGraphs,
          destinationsWithNotifications = topLevelGraphsWithNotifications,
          onNavigateToDestination = onNavigateToTopLevelGraph,
          getIsCurrentlySelected = currentDestination::isTopLevelGraphInHierarchy,
          isExtraTall = navigationSuiteType == NavigationSuiteType.NavigationRailXLarge,
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
        destinationsWithNotifications = topLevelGraphsWithNotifications,
        onNavigateToDestination = onNavigateToTopLevelGraph,
        getIsCurrentlySelected = currentDestination::isTopLevelGraphInHierarchy,
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
    Surface(color = MaterialTheme.colorScheme.background) {
      NavigationSuite(
        navigationSuiteType = if (showBottomBar) {
          NavigationSuiteType.NavigationBar
        } else {
          NavigationSuiteType.NavigationRail
        },
        topLevelGraphs = TopLevelGraph.entries.toSet(),
        topLevelGraphsWithNotifications = TopLevelGraph.entries.toSet(),
        currentDestination = null,
        onNavigateToTopLevelGraph = {},
      ) {
        Text("Content")
      }
    }
  }
}
