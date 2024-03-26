package com.hedvig.android.app.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.android.navigation.core.selectedIcon
import com.hedvig.android.navigation.core.titleTextId
import com.hedvig.android.navigation.core.unselectedIcon
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet

@Composable
internal fun HedvigNavRail(
  destinations: ImmutableSet<TopLevelGraph>,
  destinationsWithNotifications: ImmutableSet<TopLevelGraph>,
  onNavigateToDestination: (TopLevelGraph) -> Unit,
  currentDestination: NavDestination?,
  modifier: Modifier = Modifier,
) {
  HedvigNavRail(
    destinations = destinations,
    destinationsWithNotifications = destinationsWithNotifications,
    onNavigateToDestination = onNavigateToDestination,
    getIsCurrentlySelected = currentDestination::isTopLevelGraphInHierarchy,
    modifier = modifier,
  )
}

@Composable
private fun HedvigNavRail(
  destinations: ImmutableSet<TopLevelGraph>,
  destinationsWithNotifications: ImmutableSet<TopLevelGraph>,
  onNavigateToDestination: (TopLevelGraph) -> Unit,
  getIsCurrentlySelected: (TopLevelGraph) -> Boolean,
  modifier: Modifier = Modifier,
) {
  val outlineVariant = MaterialTheme.colorScheme.outlineVariant
  NavigationRail(
    containerColor = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground,
    windowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
      .only(WindowInsetsSides.Vertical + WindowInsetsSides.Left),
    modifier = modifier.drawWithContent {
      drawContent()
      drawLine(
        color = outlineVariant,
        start = Offset(size.width, 0f),
        end = Offset(size.width, size.height),
      )
    },
  ) {
    for (destination in destinations) {
      val hasNotification = destinationsWithNotifications.contains(destination)
      val selected = getIsCurrentlySelected(destination)
      NavigationRailItem(
        selected = selected,
        onClick = { onNavigateToDestination(destination) },
        icon = {
          Icon(
            imageVector = if (selected) {
              destination.selectedIcon()
            } else {
              destination.unselectedIcon()
            },
            contentDescription = null,
            modifier = if (hasNotification) Modifier.notificationDot() else Modifier,
          )
        },
        label = { Text(stringResource(destination.titleTextId())) },
        colors = NavigationRailItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.onSurface,
          selectedTextColor = MaterialTheme.colorScheme.onSurface,
          indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
          unselectedIconColor = MaterialTheme.colorScheme.onSurface,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier.testTag(destination.name),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigNavRail() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.height(300.dp)) {
      Row {
        HedvigNavRail(
          destinations = TopLevelGraph.entries.toSet().toPersistentSet(),
          destinationsWithNotifications = persistentSetOf(TopLevelGraph.Insurances),
          onNavigateToDestination = {},
          getIsCurrentlySelected = { false },
        )
        HedvigNavRail(
          destinations = TopLevelGraph.entries.toSet().toPersistentSet(),
          destinationsWithNotifications = persistentSetOf(TopLevelGraph.Insurances),
          onNavigateToDestination = {},
          getIsCurrentlySelected = { true },
        )
      }
    }
  }
}
