package com.hedvig.android.design.system.hedvig.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val HedvigIcons.Campaign: ImageVector
  get() {
    val current = _campaign
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Campaign",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M9.5 3.73 a2.7 2.7 0 0 1 5.03 0 l1.2 3.05 a2.7 2.7 0 0 0 1.53 1.52 l3.05 1.2 a2.7 2.7 0 0 1 0 5.03 l-3.05 1.2 a2.7 2.7 0 0 0 -1.52 1.53 l-1.2 3.05 a2.7 2.7 0 0 1 -5.03 0 l-1.2 -3.05 a2.7 2.7 0 0 0 -1.53 -1.52 l-3.05 -1.2 a2.7 2.7 0 0 1 0 -5.03 l3.05 -1.2 A2.7 2.7 0 0 0 8.3 6.77z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 9.5 3.73
        moveTo(x = 9.5f, y = 3.73f)
        // a 2.7 2.7 0 0 1 5.03 0
        arcToRelative(
          a = 2.7f,
          b = 2.7f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 5.03f,
          dy1 = 0.0f,
        )
        // l 1.2 3.05
        lineToRelative(dx = 1.2f, dy = 3.05f)
        // a 2.7 2.7 0 0 0 1.53 1.52
        arcToRelative(
          a = 2.7f,
          b = 2.7f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.53f,
          dy1 = 1.52f,
        )
        // l 3.05 1.2
        lineToRelative(dx = 3.05f, dy = 1.2f)
        // a 2.7 2.7 0 0 1 0 5.03
        arcToRelative(
          a = 2.7f,
          b = 2.7f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = 5.03f,
        )
        // l -3.05 1.2
        lineToRelative(dx = -3.05f, dy = 1.2f)
        // a 2.7 2.7 0 0 0 -1.52 1.53
        arcToRelative(
          a = 2.7f,
          b = 2.7f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.52f,
          dy1 = 1.53f,
        )
        // l -1.2 3.05
        lineToRelative(dx = -1.2f, dy = 3.05f)
        // a 2.7 2.7 0 0 1 -5.03 0
        arcToRelative(
          a = 2.7f,
          b = 2.7f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -5.03f,
          dy1 = 0.0f,
        )
        // l -1.2 -3.05
        lineToRelative(dx = -1.2f, dy = -3.05f)
        // a 2.7 2.7 0 0 0 -1.53 -1.52
        arcToRelative(
          a = 2.7f,
          b = 2.7f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.53f,
          dy1 = -1.52f,
        )
        // l -3.05 -1.2
        lineToRelative(dx = -3.05f, dy = -1.2f)
        // a 2.7 2.7 0 0 1 0 -5.03
        arcToRelative(
          a = 2.7f,
          b = 2.7f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = -5.03f,
        )
        // l 3.05 -1.2
        lineToRelative(dx = 3.05f, dy = -1.2f)
        // A 2.7 2.7 0 0 0 8.3 6.77z
        arcTo(
          horizontalEllipseRadius = 2.7f,
          verticalEllipseRadius = 2.7f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 8.3f,
          y1 = 6.77f,
        )
        close()
      }
    }.build().also { _campaign = it }
  }

@Preview
@Composable
private fun IconPreview() {
  com.hedvig.android.design.system.hedvig.HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.Campaign,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _campaign: ImageVector? = null
