package com.hedvig.android.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.navigation.NavDestination
import com.hedvig.android.core.designsystem.component.navigation.HedvigNavigationBar
import com.hedvig.android.core.designsystem.component.navigation.HedvigNavigationBarItem
import com.hedvig.android.core.designsystem.component.navigation.HedvigNavigationBarItemDefaults
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
internal fun HedvigNavigationBar(
  destinations: ImmutableSet<TopLevelGraph>,
  destinationsWithNotifications: ImmutableSet<TopLevelGraph>,
  onNavigateToDestination: (TopLevelGraph) -> Unit,
  currentDestination: NavDestination?,
  modifier: Modifier = Modifier,
) {
  HedvigNavigationBar(
    destinations = destinations,
    destinationsWithNotifications = destinationsWithNotifications,
    onNavigateToDestination = onNavigateToDestination,
    getIsCurrentlySelected = currentDestination::isTopLevelGraphInHierarchy,
    modifier = modifier,
  )
}

@Composable
private fun HedvigNavigationBar(
  destinations: ImmutableSet<TopLevelGraph>,
  destinationsWithNotifications: ImmutableSet<TopLevelGraph>,
  onNavigateToDestination: (TopLevelGraph) -> Unit,
  getIsCurrentlySelected: (TopLevelGraph) -> Boolean,
  modifier: Modifier = Modifier,
) {
  val outlineVariant = MaterialTheme.colorScheme.outlineVariant
  HedvigNavigationBar(
    containerColor = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground,
    modifier = modifier.drawWithContent {
      drawContent()
      drawLine(
        color = outlineVariant,
        start = Offset.Zero,
        end = Offset(size.width, 0f),
      )
    },
  ) {
    for (destination in destinations) {
      val hasNotification = destinationsWithNotifications.contains(destination)
      val selected = getIsCurrentlySelected(destination)
      HedvigNavigationBarItem(
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
        label = stringResource(destination.titleTextId()),
        colors = HedvigNavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.onSurface,
          selectedTextColor = MaterialTheme.colorScheme.onSurface,
          indicatorColor = MaterialTheme.colorScheme.surface,
          unselectedIconColor = MaterialTheme.colorScheme.onSurface,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier.testTag(destination.name),
      )
    }
  }
}

@Preview(locale = "sv", fontScale = 1.36f)
@HedvigPreview
@PreviewFontScale
@Composable
private fun PreviewHedvigBottomBar() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Column {
        HedvigNavigationBar(
          destinations = TopLevelGraph.entries.toSet().toPersistentSet(),
          destinationsWithNotifications = persistentSetOf(TopLevelGraph.Insurances),
          onNavigateToDestination = {},
          getIsCurrentlySelected = { false },
        )
        HedvigNavigationBar(
          destinations = TopLevelGraph.entries.toSet().toPersistentSet(),
          destinationsWithNotifications = persistentSetOf(TopLevelGraph.Insurances),
          onNavigateToDestination = {},
          getIsCurrentlySelected = { true },
        )
      }
    }
  }
}
