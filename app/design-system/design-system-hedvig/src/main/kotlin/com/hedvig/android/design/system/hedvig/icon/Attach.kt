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

val Attach: ImageVector
  get() {
    val current = _attach
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Attach",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M7.75 7 a2.75 2.75 0 0 1 5.5 0 v8 a1 1 0 1 1 -2 0 V7 a.75 .75 0 0 0 -1.5 0 v8 a2.5 2.5 0 0 0 5 0 V7 a4.25 4.25 0 0 0 -8.5 0 v8.25 a6 6 0 0 0 12 0 V3.5 a.75 .75 0 0 0 -1.5 0 v11.75 a4.5 4.5 0 1 1 -9 0z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 7.75 7
        moveTo(x = 7.75f, y = 7.0f)
        // a 2.75 2.75 0 0 1 5.5 0
        arcToRelative(
          a = 2.75f,
          b = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 5.5f,
          dy1 = 0.0f,
        )
        // v 8
        verticalLineToRelative(dy = 8.0f)
        // a 1 1 0 1 1 -2 0
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -2.0f,
          dy1 = 0.0f,
        )
        // V 7
        verticalLineTo(y = 7.0f)
        // a 0.75 0.75 0 0 0 -1.5 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.5f,
          dy1 = 0.0f,
        )
        // v 8
        verticalLineToRelative(dy = 8.0f)
        // a 2.5 2.5 0 0 0 5 0
        arcToRelative(
          a = 2.5f,
          b = 2.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 5.0f,
          dy1 = 0.0f,
        )
        // V 7
        verticalLineTo(y = 7.0f)
        // a 4.25 4.25 0 0 0 -8.5 0
        arcToRelative(
          a = 4.25f,
          b = 4.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -8.5f,
          dy1 = 0.0f,
        )
        // v 8.25
        verticalLineToRelative(dy = 8.25f)
        // a 6 6 0 0 0 12 0
        arcToRelative(
          a = 6.0f,
          b = 6.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 12.0f,
          dy1 = 0.0f,
        )
        // V 3.5
        verticalLineTo(y = 3.5f)
        // a 0.75 0.75 0 0 0 -1.5 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.5f,
          dy1 = 0.0f,
        )
        // v 11.75
        verticalLineToRelative(dy = 11.75f)
        // a 4.5 4.5 0 1 1 -9 0z
        arcToRelative(
          a = 4.5f,
          b = 4.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -9.0f,
          dy1 = 0.0f,
        )
        close()
      }
    }.build().also { _attach = it }
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
        imageVector = Attach,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _attach: ImageVector? = null
