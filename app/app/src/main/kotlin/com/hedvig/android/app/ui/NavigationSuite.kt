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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
internal fun NavigationSuite(
  hedvigAppState: HedvigAppState,
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit,
) {
  val topLevelGraphs by hedvigAppState.topLevelGraphs.collectAsStateWithLifecycle()
  Column(modifier) {
    val destinationsWithNotifications by hedvigAppState.topLevelGraphsWithNotifications.collectAsStateWithLifecycle()
    Row(Modifier.weight(1f).fillMaxWidth()) {
      AnimatedVisibility(
        visible = hedvigAppState.shouldShowNavRail,
        enter = expandHorizontally(expandFrom = Alignment.End),
        exit = shrinkHorizontally(shrinkTowards = Alignment.End),
      ) {
        HedvigNavRail(
          destinations = topLevelGraphs,
          destinationsWithNotifications = destinationsWithNotifications,
          onNavigateToDestination = hedvigAppState::navigateToTopLevelGraph,
          currentDestination = hedvigAppState.currentDestination,
        )
      }
      content()
    }
    AnimatedVisibility(
      visible = hedvigAppState.shouldShowBottomBar,
      enter = expandVertically(expandFrom = Alignment.Top),
      exit = shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
      HedvigBottomBar(
        destinations = topLevelGraphs,
        destinationsWithNotifications = destinationsWithNotifications,
        onNavigateToDestination = hedvigAppState::navigateToTopLevelGraph,
        currentDestination = hedvigAppState.currentDestination,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewNavigationSuite() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      NavigationSuite()
    }
  }
}
