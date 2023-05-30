package com.hedvig.android.app.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import com.hedvig.android.app.navigation.TopLevelDestination
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.kiwi.navigationcompose.typed.createRoutePattern
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

@Composable
internal fun HedvigNavRail(
  destinations: ImmutableSet<TopLevelDestination>,
  destinationsWithNotifications: ImmutableSet<TopLevelDestination>,
  onNavigateToDestination: (TopLevelDestination) -> Unit,
  currentDestination: NavDestination?,
  modifier: Modifier = Modifier,
) {
  HedvigNavRail(
    destinations = destinations,
    destinationsWithNotifications = destinationsWithNotifications,
    onNavigateToDestination = onNavigateToDestination,
    getIsCurrentlySelected = { destination: TopLevelDestination ->
      currentDestination.isTopLevelDestinationInHierarchy(
        when (destination) {
          TopLevelDestination.HOME -> createRoutePattern<TopLevelDestination.HOME>()
          TopLevelDestination.INSURANCE -> createRoutePattern<TopLevelDestination.INSURANCE>()
          TopLevelDestination.PROFILE -> createRoutePattern<TopLevelDestination.PROFILE>()
          TopLevelDestination.REFERRALS -> createRoutePattern<TopLevelDestination.REFERRALS>()
        },
      )
    },
    modifier = modifier,
  )
}

@Composable
private fun HedvigNavRail(
  destinations: ImmutableSet<TopLevelDestination>,
  destinationsWithNotifications: ImmutableSet<TopLevelDestination>,
  onNavigateToDestination: (TopLevelDestination) -> Unit,
  getIsCurrentlySelected: (TopLevelDestination) -> Boolean,
  modifier: Modifier = Modifier,
) {
  NavigationRail(
    containerColor = Color.Transparent,
    modifier = modifier,
  ) {
    for (destination in destinations) {
      val hasNotification = destinationsWithNotifications.contains(destination)
      val selected = getIsCurrentlySelected(destination)
      NavigationRailItem(
        selected = selected,
        onClick = { onNavigateToDestination(destination) },
        icon = {
          Icon(
            painter = painterResource(
              if (selected) {
                destination.selectedIcon
              } else {
                destination.icon
              },
            ),
            contentDescription = null,
            // Color is defined in the drawables themselves.
            tint = Color.Unspecified,
            modifier = if (hasNotification) Modifier.notificationDot() else Modifier,
          )
        },
        label = { Text(stringResource(destination.titleTextId)) },
        colors = NavigationRailItemDefaults.colors(
          indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
          selectedIconColor = MaterialTheme.colorScheme.onSurface,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigNavRail() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HedvigNavRail(
        destinations = persistentSetOf(
          TopLevelDestination.HOME,
          TopLevelDestination.INSURANCE,
          TopLevelDestination.REFERRALS,
          TopLevelDestination.PROFILE,
        ),
        destinationsWithNotifications = persistentSetOf(TopLevelDestination.INSURANCE),
        onNavigateToDestination = {},
        getIsCurrentlySelected = { it == TopLevelDestination.HOME },
      )
    }
  }
}
