package com.hedvig.android.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.hedvig.android.navigation.core.TopLevelGraph
import com.kiwi.navigationcompose.typed.createRoutePattern

internal fun Modifier.notificationDot(): Modifier = this.composed {
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

/**
 * Checks if the [TopLevelGraph] [T] is part of the hiararchy of [this]
 */
internal inline fun <reified T : TopLevelGraph> NavDestination?.isTopLevelGraphInHierarchy(): Boolean {
  return this?.hierarchy?.any {
    it.route?.contains(createRoutePattern<T>(), true) ?: false
  } ?: false
}
