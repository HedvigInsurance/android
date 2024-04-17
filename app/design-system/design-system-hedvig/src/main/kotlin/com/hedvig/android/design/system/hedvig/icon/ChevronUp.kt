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
val HedvigIcons.ChevronUp: ImageVector
  get() {
    val current = _chevronUp
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ChevronUp",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M19.03 14.53 a.75 .75 0 0 1 -1.06 0 l-5.8 -5.8 a.25 .25 0 0 0 -.35 0 l-5.79 5.8 a.75 .75 0 0 1 -1.06 -1.06 l5.8 -5.8 a1.75 1.75 0 0 1 2.47 0 l5.79 5.8 a.75 .75 0 0 1 0 1.06
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 19.03 14.53
        moveTo(x = 19.03f, y = 14.53f)
        // a 0.75 0.75 0 0 1 -1.06 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.06f,
          dy1 = 0.0f,
        )
        // l -5.8 -5.8
        lineToRelative(dx = -5.8f, dy = -5.8f)
        // a 0.25 0.25 0 0 0 -0.35 0
        arcToRelative(
          a = 0.25f,
          b = 0.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -0.35f,
          dy1 = 0.0f,
        )
        // l -5.79 5.8
        lineToRelative(dx = -5.79f, dy = 5.8f)
        // a 0.75 0.75 0 0 1 -1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.06f,
          dy1 = -1.06f,
        )
        // l 5.8 -5.8
        lineToRelative(dx = 5.8f, dy = -5.8f)
        // a 1.75 1.75 0 0 1 2.47 0
        arcToRelative(
          a = 1.75f,
          b = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 2.47f,
          dy1 = 0.0f,
        )
        // l 5.79 5.8
        lineToRelative(dx = 5.79f, dy = 5.8f)
        // a 0.75 0.75 0 0 1 0 1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = 1.06f,
        )
      }
    }.build().also { _chevronUp = it }
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
        imageVector = HedvigIcons.ChevronUp,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _chevronUp: ImageVector? = null
