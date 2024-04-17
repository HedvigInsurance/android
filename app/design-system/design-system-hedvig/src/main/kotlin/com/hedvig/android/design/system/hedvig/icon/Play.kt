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
val HedvigIcons.Play: ImageVector
  get() {
    val current = _play
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Play",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M9 16.16 V7.85 a.8 .8 0 0 1 .25 -.61 .8 .8 0 0 1 .8 -.2 l.2 .08 6.38 4.17 a1 1 0 0 1 .28 .31 A1 1 0 0 1 17 12 a1 1 0 0 1 -.1 .4 1 1 0 0 1 -.27 .31 l-6.37 4.17 A1 1 0 0 1 9.82 17 a.8 .8 0 0 1 -.57 -.24 .8 .8 0 0 1 -.25 -.6
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 9 16.16
        moveTo(x = 9.0f, y = 16.16f)
        // V 7.85
        verticalLineTo(y = 7.85f)
        // a 0.8 0.8 0 0 1 0.25 -0.61
        arcToRelative(
          a = 0.8f,
          b = 0.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.25f,
          dy1 = -0.61f,
        )
        // a 0.8 0.8 0 0 1 0.8 -0.2
        arcToRelative(
          a = 0.8f,
          b = 0.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.8f,
          dy1 = -0.2f,
        )
        // l 0.2 0.08
        lineToRelative(dx = 0.2f, dy = 0.08f)
        // l 6.38 4.17
        lineToRelative(dx = 6.38f, dy = 4.17f)
        // a 1 1 0 0 1 0.28 0.31
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.28f,
          dy1 = 0.31f,
        )
        // A 1 1 0 0 1 17 12
        arcTo(
          horizontalEllipseRadius = 1.0f,
          verticalEllipseRadius = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 17.0f,
          y1 = 12.0f,
        )
        // a 1 1 0 0 1 -0.1 0.4
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.1f,
          dy1 = 0.4f,
        )
        // a 1 1 0 0 1 -0.27 0.31
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.27f,
          dy1 = 0.31f,
        )
        // l -6.37 4.17
        lineToRelative(dx = -6.37f, dy = 4.17f)
        // A 1 1 0 0 1 9.82 17
        arcTo(
          horizontalEllipseRadius = 1.0f,
          verticalEllipseRadius = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 9.82f,
          y1 = 17.0f,
        )
        // a 0.8 0.8 0 0 1 -0.57 -0.24
        arcToRelative(
          a = 0.8f,
          b = 0.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.57f,
          dy1 = -0.24f,
        )
        // a 0.8 0.8 0 0 1 -0.25 -0.6
        arcToRelative(
          a = 0.8f,
          b = 0.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.25f,
          dy1 = -0.6f,
        )
      }
    }.build().also { _play = it }
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
        imageVector = HedvigIcons.Play,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _play: ImageVector? = null
