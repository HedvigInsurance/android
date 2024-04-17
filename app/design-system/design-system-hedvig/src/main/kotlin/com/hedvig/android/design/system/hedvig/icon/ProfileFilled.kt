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
val HedvigIcons.ProfileFilled: ImageVector
  get() {
    val current = _profileFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ProfileFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 11.5 A4.75 4.75 0 1 0 12 2 a4.75 4.75 0 0 0 0 9.5 m0 1.5 c-4.26 0 -8 2.94 -8 6.9 C4 21.2 5.2 22 6.31 22 H17.7 C18.8 22 20 21.2 20 19.9 c0 -3.96 -3.74 -6.9 -8 -6.9
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 12 11.5
        moveTo(x = 12.0f, y = 11.5f)
        // A 4.75 4.75 0 1 0 12 2
        arcTo(
          horizontalEllipseRadius = 4.75f,
          verticalEllipseRadius = 4.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          x1 = 12.0f,
          y1 = 2.0f,
        )
        // a 4.75 4.75 0 0 0 0 9.5
        arcToRelative(
          a = 4.75f,
          b = 4.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 9.5f,
        )
        // m 0 1.5
        moveToRelative(dx = 0.0f, dy = 1.5f)
        // c -4.26 0 -8 2.94 -8 6.9
        curveToRelative(
          dx1 = -4.26f,
          dy1 = 0.0f,
          dx2 = -8.0f,
          dy2 = 2.94f,
          dx3 = -8.0f,
          dy3 = 6.9f,
        )
        // C 4 21.2 5.2 22 6.31 22
        curveTo(
          x1 = 4.0f,
          y1 = 21.2f,
          x2 = 5.2f,
          y2 = 22.0f,
          x3 = 6.31f,
          y3 = 22.0f,
        )
        // H 17.7
        horizontalLineTo(x = 17.7f)
        // C 18.8 22 20 21.2 20 19.9
        curveTo(
          x1 = 18.8f,
          y1 = 22.0f,
          x2 = 20.0f,
          y2 = 21.2f,
          x3 = 20.0f,
          y3 = 19.9f,
        )
        // c 0 -3.96 -3.74 -6.9 -8 -6.9
        curveToRelative(
          dx1 = 0.0f,
          dy1 = -3.96f,
          dx2 = -3.74f,
          dy2 = -6.9f,
          dx3 = -8.0f,
          dy3 = -6.9f,
        )
      }
    }.build().also { _profileFilled = it }
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
        imageVector = HedvigIcons.ProfileFilled,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _profileFilled: ImageVector? = null
