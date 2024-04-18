package com.hedvig.android.design.system.hedvig.icon.colored

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
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.ColoredCampaign: ImageVector
  get() {
    val current = _campaign
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.colored.Campaign",
      defaultWidth = 40.0.dp,
      defaultHeight = 40.0.dp,
      viewportWidth = 40.0f,
      viewportHeight = 40.0f,
    ).apply {
      // M20 4 A16 16 0 1 0 20 36 16 16 0 1 0 20 4z
      path(
        fill = SolidColor(Color(0xFFD4F5BC)),
      ) {
        // M 20 4
        moveTo(x = 20.0f, y = 4.0f)
        // A 16 16 0 1 0 20 36
        arcTo(
          horizontalEllipseRadius = 16.0f,
          verticalEllipseRadius = 16.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          x1 = 20.0f,
          y1 = 36.0f,
        )
        // A 16 16 0 1 0 20 4z
        arcTo(
          horizontalEllipseRadius = 16.0f,
          verticalEllipseRadius = 16.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          x1 = 20.0f,
          y1 = 4.0f,
        )
        close()
      }
      // M17.86 12.95 a2.3 2.3 0 0 1 4.28 0 l1.02 2.6 a2.3 2.3 0 0 0 1.3 1.3 l2.59 1.01 a2.3 2.3 0 0 1 0 4.28 l-2.6 1.02 a2.3 2.3 0 0 0 -1.3 1.3 l-1.01 2.59 a2.3 2.3 0 0 1 -4.28 0 l-1.02 -2.6 a2.3 2.3 0 0 0 -1.3 -1.3 l-2.59 -1.01 a2.3 2.3 0 0 1 0 -4.28 l2.6 -1.02 a2.3 2.3 0 0 0 1.3 -1.3z
      path(
        fill = SolidColor(Color(0xFF24CC5C)),
      ) {
        // M 17.86 12.95
        moveTo(x = 17.86f, y = 12.95f)
        // a 2.3 2.3 0 0 1 4.28 0
        arcToRelative(
          a = 2.3f,
          b = 2.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 4.28f,
          dy1 = 0.0f,
        )
        // l 1.02 2.6
        lineToRelative(dx = 1.02f, dy = 2.6f)
        // a 2.3 2.3 0 0 0 1.3 1.3
        arcToRelative(
          a = 2.3f,
          b = 2.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.3f,
          dy1 = 1.3f,
        )
        // l 2.59 1.01
        lineToRelative(dx = 2.59f, dy = 1.01f)
        // a 2.3 2.3 0 0 1 0 4.28
        arcToRelative(
          a = 2.3f,
          b = 2.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = 4.28f,
        )
        // l -2.6 1.02
        lineToRelative(dx = -2.6f, dy = 1.02f)
        // a 2.3 2.3 0 0 0 -1.3 1.3
        arcToRelative(
          a = 2.3f,
          b = 2.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.3f,
          dy1 = 1.3f,
        )
        // l -1.01 2.59
        lineToRelative(dx = -1.01f, dy = 2.59f)
        // a 2.3 2.3 0 0 1 -4.28 0
        arcToRelative(
          a = 2.3f,
          b = 2.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -4.28f,
          dy1 = 0.0f,
        )
        // l -1.02 -2.6
        lineToRelative(dx = -1.02f, dy = -2.6f)
        // a 2.3 2.3 0 0 0 -1.3 -1.3
        arcToRelative(
          a = 2.3f,
          b = 2.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.3f,
          dy1 = -1.3f,
        )
        // l -2.59 -1.01
        lineToRelative(dx = -2.59f, dy = -1.01f)
        // a 2.3 2.3 0 0 1 0 -4.28
        arcToRelative(
          a = 2.3f,
          b = 2.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = -4.28f,
        )
        // l 2.6 -1.02
        lineToRelative(dx = 2.6f, dy = -1.02f)
        // a 2.3 2.3 0 0 0 1.3 -1.3z
        arcToRelative(
          a = 2.3f,
          b = 2.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.3f,
          dy1 = -1.3f,
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
        imageVector = HedvigIcons.ColoredCampaign,
        contentDescription = null,
        modifier = Modifier
          .width((40.0).dp)
          .height((40.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _campaign: ImageVector? = null
