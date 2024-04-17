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

val Link: ImageVector
  get() {
    val current = _link
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Link",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M7 8.75 a3.25 3.25 0 0 0 0 6.5 h3 a.75 .75 0 0 1 0 1.5 H7 a4.75 4.75 0 1 1 0 -9.5 h3 a.75 .75 0 0 1 0 1.5z M13.25 8 A.75 .75 0 0 1 14 7.25 h3 a4.75 4.75 0 1 1 0 9.5 h-3 a.75 .75 0 0 1 0 -1.5 h3 a3.25 3.25 0 0 0 0 -6.5 h-3 A.75 .75 0 0 1 13.25 8 M8.5 11.25 a.75 .75 0 0 0 0 1.5 h7 a.75 .75 0 0 0 0 -1.5z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 7 8.75
        moveTo(x = 7.0f, y = 8.75f)
        // a 3.25 3.25 0 0 0 0 6.5
        arcToRelative(
          a = 3.25f,
          b = 3.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 6.5f,
        )
        // h 3
        horizontalLineToRelative(dx = 3.0f)
        // a 0.75 0.75 0 0 1 0 1.5
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = 1.5f,
        )
        // H 7
        horizontalLineTo(x = 7.0f)
        // a 4.75 4.75 0 1 1 0 -9.5
        arcToRelative(
          a = 4.75f,
          b = 4.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = -9.5f,
        )
        // h 3
        horizontalLineToRelative(dx = 3.0f)
        // a 0.75 0.75 0 0 1 0 1.5z
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = 1.5f,
        )
        close()
        // M 13.25 8
        moveTo(x = 13.25f, y = 8.0f)
        // A 0.75 0.75 0 0 1 14 7.25
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 14.0f,
          y1 = 7.25f,
        )
        // h 3
        horizontalLineToRelative(dx = 3.0f)
        // a 4.75 4.75 0 1 1 0 9.5
        arcToRelative(
          a = 4.75f,
          b = 4.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = 9.5f,
        )
        // h -3
        horizontalLineToRelative(dx = -3.0f)
        // a 0.75 0.75 0 0 1 0 -1.5
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = -1.5f,
        )
        // h 3
        horizontalLineToRelative(dx = 3.0f)
        // a 3.25 3.25 0 0 0 0 -6.5
        arcToRelative(
          a = 3.25f,
          b = 3.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -6.5f,
        )
        // h -3
        horizontalLineToRelative(dx = -3.0f)
        // A 0.75 0.75 0 0 1 13.25 8
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 13.25f,
          y1 = 8.0f,
        )
        // M 8.5 11.25
        moveTo(x = 8.5f, y = 11.25f)
        // a 0.75 0.75 0 0 0 0 1.5
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 1.5f,
        )
        // h 7
        horizontalLineToRelative(dx = 7.0f)
        // a 0.75 0.75 0 0 0 0 -1.5z
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -1.5f,
        )
        close()
      }
    }.build().also { _link = it }
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
        imageVector = Link,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _link: ImageVector? = null
