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
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.WarningFilled: ImageVector
  get() {
    val current = _warningFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.WarningFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M8.85 4.87 a3.59 3.59 0 0 1 6.3 0 l5.9 10.72 A3.64 3.64 0 0 1 17.88 21 H6.11 a3.64 3.64 0 0 1 -3.15 -5.41z M12 7.75 a.75 .75 0 0 1 .75 .75 v5 a.75 .75 0 0 1 -1.5 0 v-5 A.75 .75 0 0 1 12 7.75 m0 9.75 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 8.85 4.87
        moveTo(x = 8.85f, y = 4.87f)
        // a 3.59 3.59 0 0 1 6.3 0
        arcToRelative(
          a = 3.59f,
          b = 3.59f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 6.3f,
          dy1 = 0.0f,
        )
        // l 5.9 10.72
        lineToRelative(dx = 5.9f, dy = 10.72f)
        // A 3.64 3.64 0 0 1 17.88 21
        arcTo(
          horizontalEllipseRadius = 3.64f,
          verticalEllipseRadius = 3.64f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 17.88f,
          y1 = 21.0f,
        )
        // H 6.11
        horizontalLineTo(x = 6.11f)
        // a 3.64 3.64 0 0 1 -3.15 -5.41z
        arcToRelative(
          a = 3.64f,
          b = 3.64f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -3.15f,
          dy1 = -5.41f,
        )
        close()
        // M 12 7.75
        moveTo(x = 12.0f, y = 7.75f)
        // a 0.75 0.75 0 0 1 0.75 0.75
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.75f,
          dy1 = 0.75f,
        )
        // v 5
        verticalLineToRelative(dy = 5.0f)
        // a 0.75 0.75 0 0 1 -1.5 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.5f,
          dy1 = 0.0f,
        )
        // v -5
        verticalLineToRelative(dy = -5.0f)
        // A 0.75 0.75 0 0 1 12 7.75
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 12.0f,
          y1 = 7.75f,
        )
        // m 0 9.75
        moveToRelative(dx = 0.0f, dy = 9.75f)
        // a 1 1 0 1 0 0 -2
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -2.0f,
        )
        // a 1 1 0 0 0 0 2
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 2.0f,
        )
      }
    }.build().also { _warningFilled = it }
  }

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.WarningFilled,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _warningFilled: ImageVector? = null
