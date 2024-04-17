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
val HedvigIcons.Mic: ImageVector
  get() {
    val current = _mic
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Mic",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 3 a2.5 2.5 0 0 1 2.5 2.5 v6 a2.5 2.5 0 0 1 -5 0 v-6 A2.5 2.5 0 0 1 12 3 m-6 7.75 a.75 .75 0 0 1 .75 .75 5.25 5.25 0 1 0 10.5 0 .75 .75 0 0 1 1.5 0 6.75 6.75 0 0 1 -6 6.7 v2.3 a.75 .75 0 0 1 -1.5 0 v-2.3 a6.75 6.75 0 0 1 -6 -6.7 A.75 .75 0 0 1 6 10.75
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 12 3
        moveTo(x = 12.0f, y = 3.0f)
        // a 2.5 2.5 0 0 1 2.5 2.5
        arcToRelative(
          a = 2.5f,
          b = 2.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 2.5f,
          dy1 = 2.5f,
        )
        // v 6
        verticalLineToRelative(dy = 6.0f)
        // a 2.5 2.5 0 0 1 -5 0
        arcToRelative(
          a = 2.5f,
          b = 2.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -5.0f,
          dy1 = 0.0f,
        )
        // v -6
        verticalLineToRelative(dy = -6.0f)
        // A 2.5 2.5 0 0 1 12 3
        arcTo(
          horizontalEllipseRadius = 2.5f,
          verticalEllipseRadius = 2.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 12.0f,
          y1 = 3.0f,
        )
        // m -6 7.75
        moveToRelative(dx = -6.0f, dy = 7.75f)
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
        // a 5.25 5.25 0 1 0 10.5 0
        arcToRelative(
          a = 5.25f,
          b = 5.25f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 10.5f,
          dy1 = 0.0f,
        )
        // a 0.75 0.75 0 0 1 1.5 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.5f,
          dy1 = 0.0f,
        )
        // a 6.75 6.75 0 0 1 -6 6.7
        arcToRelative(
          a = 6.75f,
          b = 6.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -6.0f,
          dy1 = 6.7f,
        )
        // v 2.3
        verticalLineToRelative(dy = 2.3f)
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
        // v -2.3
        verticalLineToRelative(dy = -2.3f)
        // a 6.75 6.75 0 0 1 -6 -6.7
        arcToRelative(
          a = 6.75f,
          b = 6.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -6.0f,
          dy1 = -6.7f,
        )
        // A 0.75 0.75 0 0 1 6 10.75
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 6.0f,
          y1 = 10.75f,
        )
      }
    }.build().also { _mic = it }
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
        imageVector = HedvigIcons.Mic,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _mic: ImageVector? = null
