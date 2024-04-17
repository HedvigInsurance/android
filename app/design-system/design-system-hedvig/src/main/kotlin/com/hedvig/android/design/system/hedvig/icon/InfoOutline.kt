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
val HedvigIcons.InfoOutline: ImageVector
  get() {
    val current = _infoOutline
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.InfoOutline",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 20 a8 8 0 1 0 0 -16 8 8 0 0 0 0 16 m0 1.5 a9.5 9.5 0 1 0 0 -19 9.5 9.5 0 0 0 0 19 m0 -11.25 A.75 .75 0 0 1 12.75 11 v5 a.75 .75 0 0 1 -1.5 0 v-5 A.75 .75 0 0 1 12 10.25 M12 9 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 12 20
        moveTo(x = 12.0f, y = 20.0f)
        // a 8 8 0 1 0 0 -16
        arcToRelative(
          a = 8.0f,
          b = 8.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -16.0f,
        )
        // a 8 8 0 0 0 0 16
        arcToRelative(
          a = 8.0f,
          b = 8.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 16.0f,
        )
        // m 0 1.5
        moveToRelative(dx = 0.0f, dy = 1.5f)
        // a 9.5 9.5 0 1 0 0 -19
        arcToRelative(
          a = 9.5f,
          b = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -19.0f,
        )
        // a 9.5 9.5 0 0 0 0 19
        arcToRelative(
          a = 9.5f,
          b = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 19.0f,
        )
        // m 0 -11.25
        moveToRelative(dx = 0.0f, dy = -11.25f)
        // A 0.75 0.75 0 0 1 12.75 11
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 12.75f,
          y1 = 11.0f,
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
        // A 0.75 0.75 0 0 1 12 10.25
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 12.0f,
          y1 = 10.25f,
        )
        // M 12 9
        moveTo(x = 12.0f, y = 9.0f)
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
    }.build().also { _infoOutline = it }
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
        imageVector = HedvigIcons.InfoOutline,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _infoOutline: ImageVector? = null
