package com.hedvig.android.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy

internal fun Modifier.notificationDot(): Modifier = composed {
  val notificationColor = MaterialTheme.colorScheme.error
  Modifier.drawWithContent {
    drawContent()
    val circleSize = 6.dp
    drawCircle(
      notificationColor,
      radius = circleSize.toPx(),
      center = center + Offset(
        12.dp.toPx() - (circleSize / 2).toPx(),
        -12.dp.toPx() + (circleSize / 2).toPx(),
      ),
    )
  }
}

internal fun NavDestination?.isTopLevelDestinationInHierarchy(
  destinationRoute: String,
): Boolean {
  return this?.hierarchy?.any {
    it.route?.contains(
      destinationRoute,
      true,
    ) ?: false
  } ?: false
}
