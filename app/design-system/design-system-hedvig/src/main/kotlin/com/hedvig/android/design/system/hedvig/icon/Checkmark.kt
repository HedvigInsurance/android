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
val HedvigIcons.Checkmark: ImageVector
  get() {
    val current = _checkmark
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Checkmark",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M19.53 6.97 a.75 .75 0 0 1 0 1.06 l-8.44 8.44 a2.25 2.25 0 0 1 -3.18 0 l-3.44 -3.44 a.75 .75 0 1 1 1.06 -1.06 l3.44 3.44 a.75 .75 0 0 0 1.06 0 l8.44 -8.44 a.75 .75 0 0 1 1.06 0
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 19.53 6.97
        moveTo(x = 19.53f, y = 6.97f)
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
        // l -8.44 8.44
        lineToRelative(dx = -8.44f, dy = 8.44f)
        // a 2.25 2.25 0 0 1 -3.18 0
        arcToRelative(
          a = 2.25f,
          b = 2.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -3.18f,
          dy1 = 0.0f,
        )
        // l -3.44 -3.44
        lineToRelative(dx = -3.44f, dy = -3.44f)
        // a 0.75 0.75 0 1 1 1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = 1.06f,
          dy1 = -1.06f,
        )
        // l 3.44 3.44
        lineToRelative(dx = 3.44f, dy = 3.44f)
        // a 0.75 0.75 0 0 0 1.06 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.06f,
          dy1 = 0.0f,
        )
        // l 8.44 -8.44
        lineToRelative(dx = 8.44f, dy = -8.44f)
        // a 0.75 0.75 0 0 1 1.06 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.06f,
          dy1 = 0.0f,
        )
      }
    }.build().also { _checkmark = it }
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
        imageVector = Checkmark,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _checkmark: ImageVector? = null
