package com.hedvig.app.feature.loggedin.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.hedvig.android.app.navigation.TopLevelDestination
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.kiwi.navigationcompose.typed.createRoutePattern
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

@Composable
internal fun HedvigBottomBar(
  destinations: ImmutableSet<TopLevelDestination>,
  destinationsWithNotifications: ImmutableSet<TopLevelDestination>,
  onNavigateToDestination: (TopLevelDestination) -> Unit,
  currentDestination: NavDestination?,
  modifier: Modifier = Modifier,
) {
  HedvigBottomBar(
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
private fun HedvigBottomBar(
  destinations: ImmutableSet<TopLevelDestination>,
  destinationsWithNotifications: ImmutableSet<TopLevelDestination>,
  onNavigateToDestination: (TopLevelDestination) -> Unit,
  getIsCurrentlySelected: (TopLevelDestination) -> Boolean,
  modifier: Modifier = Modifier,
) {
  NavigationBar(modifier = modifier) {
    for (destination in destinations) {
      val hasNotification = destinationsWithNotifications.contains(destination)
      val selected = getIsCurrentlySelected(destination)
      NavigationBarItem(
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
          )
        },
        label = { Text(stringResource(destination.titleTextId)) },
        colors = NavigationBarItemDefaults.colors(
          indicatorColor = if (isSystemInDarkTheme()) {
            MaterialTheme.colorScheme.background
          } else {
            MaterialTheme.colorScheme.surfaceVariant
          },
          selectedIconColor = MaterialTheme.colorScheme.onSurface,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = if (hasNotification) Modifier.notificationDot() else Modifier,
      )
    }
  }
}

private fun Modifier.notificationDot(): Modifier = composed {
  val notificationColor = MaterialTheme.colorScheme.error
  Modifier.drawWithContent {
    drawContent()
    drawCircle(
      notificationColor,
      radius = 5.dp.toPx(),
      // This is based on the dimensions of the NavigationBar's "indicator pill";
      // however, its parameters are private, so we must depend on them implicitly
      // (NavigationBarTokens.ActiveIndicatorWidth = 64.dp)
      center = center + Offset(
        64.dp.toPx() * .45f,
        32.dp.toPx() * -.45f - 6.dp.toPx(),
      ),
    )
  }
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(
  destinationRoute: String,
): Boolean {
  return this?.hierarchy?.any {
    it.route?.contains(
      destinationRoute,
      true,
    ) ?: false
  } ?: false
}

@HedvigPreview
@Composable
private fun PreviewHedvigBottomBar() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HedvigBottomBar(
        destinations = persistentSetOf(
          TopLevelDestination.HOME,
          TopLevelDestination.INSURANCE,
          TopLevelDestination.REFERRALS,
          TopLevelDestination.PROFILE,
        ),
        destinationsWithNotifications = persistentSetOf(),
        onNavigateToDestination = {},
        getIsCurrentlySelected = { it == TopLevelDestination.HOME },
      )
    }
  }
}
