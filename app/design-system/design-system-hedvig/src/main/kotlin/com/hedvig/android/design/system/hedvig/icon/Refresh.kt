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
val HedvigIcons.Refresh: ImageVector
  get() {
    val current = _refresh
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Refresh",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12.69 5.25 H12 l-.79 .04 a7.75 7.75 0 1 0 7.43 3.72 .75 .75 0 0 0 -1.28 .77 A6.25 6.25 0 1 1 12 6.75 h.69 l-1.22 1.22 a.75 .75 0 0 0 1.06 1.06 l2.15 -2.15 a1.25 1.25 0 0 0 0 -1.76 l-2.15 -2.15 a.75 .75 0 1 0 -1.06 1.06z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 12.69 5.25
        moveTo(x = 12.69f, y = 5.25f)
        // H 12
        horizontalLineTo(x = 12.0f)
        // l -0.79 0.04
        lineToRelative(dx = -0.79f, dy = 0.04f)
        // a 7.75 7.75 0 1 0 7.43 3.72
        arcToRelative(
          a = 7.75f,
          b = 7.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 7.43f,
          dy1 = 3.72f,
        )
        // a 0.75 0.75 0 0 0 -1.28 0.77
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.28f,
          dy1 = 0.77f,
        )
        // A 6.25 6.25 0 1 1 12 6.75
        arcTo(
          horizontalEllipseRadius = 6.25f,
          verticalEllipseRadius = 6.25f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          x1 = 12.0f,
          y1 = 6.75f,
        )
        // h 0.69
        horizontalLineToRelative(dx = 0.69f)
        // l -1.22 1.22
        lineToRelative(dx = -1.22f, dy = 1.22f)
        // a 0.75 0.75 0 0 0 1.06 1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.06f,
          dy1 = 1.06f,
        )
        // l 2.15 -2.15
        lineToRelative(dx = 2.15f, dy = -2.15f)
        // a 1.25 1.25 0 0 0 0 -1.76
        arcToRelative(
          a = 1.25f,
          b = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -1.76f,
        )
        // l -2.15 -2.15
        lineToRelative(dx = -2.15f, dy = -2.15f)
        // a 0.75 0.75 0 1 0 -1.06 1.06z
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = -1.06f,
          dy1 = 1.06f,
        )
        close()
      }
    }.build().also { _refresh = it }
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
        imageVector = HedvigIcons.Refresh,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _refresh: ImageVector? = null
