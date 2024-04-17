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
val HedvigIcons.ArrowLeft: ImageVector
  get() {
    val current = _arrowLeft
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ArrowLeft",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12.53 6.03 a.75 .75 0 0 0 -1.06 -1.06 l-5.8 5.8 a1.75 1.75 0 0 0 0 2.47 l5.8 5.79 a.75 .75 0 1 0 1.06 -1.06 l-5.22 -5.22 H19 a.75 .75 0 0 0 0 -1.5 H7.31z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 12.53 6.03
        moveTo(x = 12.53f, y = 6.03f)
        // a 0.75 0.75 0 0 0 -1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.06f,
          dy1 = -1.06f,
        )
        // l -5.8 5.8
        lineToRelative(dx = -5.8f, dy = 5.8f)
        // a 1.75 1.75 0 0 0 0 2.47
        arcToRelative(
          a = 1.75f,
          b = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 2.47f,
        )
        // l 5.8 5.79
        lineToRelative(dx = 5.8f, dy = 5.79f)
        // a 0.75 0.75 0 1 0 1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 1.06f,
          dy1 = -1.06f,
        )
        // l -5.22 -5.22
        lineToRelative(dx = -5.22f, dy = -5.22f)
        // H 19
        horizontalLineTo(x = 19.0f)
        // a 0.75 0.75 0 0 0 0 -1.5
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -1.5f,
        )
        // H 7.31z
        horizontalLineTo(x = 7.31f)
        close()
      }
    }.build().also { _arrowLeft = it }
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
        imageVector = HedvigIcons.ArrowLeft,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _arrowLeft: ImageVector? = null
