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
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.ArrowNorthEast: ImageVector
  get() {
    val current = _arrowNorthEast
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M10 5.25 C9.58579 5.25 9.25 5.58579 9.25 6 C9.25 6.41421 9.58579 6.75 10 6.75 L16.1894 6.75 L5.10571 17.8337 C4.81282 18.1266 4.81282 18.6015 5.10571 18.8943 C5.3986 19.1872 5.87348 19.1872 6.16637 18.8943 L17.25 7.81071 V14 C17.25 14.4142 17.5858 14.75 18 14.75 C18.4142 14.75 18.75 14.4142 18.75 14 V7 C18.75 6.0335 17.9665 5.25 17 5.25 L10 5.25Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 10 5.25
        moveTo(x = 10.0f, y = 5.25f)
        // C 9.58579 5.25 9.25 5.58579 9.25 6
        curveTo(
          x1 = 9.58579f,
          y1 = 5.25f,
          x2 = 9.25f,
          y2 = 5.58579f,
          x3 = 9.25f,
          y3 = 6.0f,
        )
        // C 9.25 6.41421 9.58579 6.75 10 6.75
        curveTo(
          x1 = 9.25f,
          y1 = 6.41421f,
          x2 = 9.58579f,
          y2 = 6.75f,
          x3 = 10.0f,
          y3 = 6.75f,
        )
        // L 16.1894 6.75
        lineTo(x = 16.1894f, y = 6.75f)
        // L 5.10571 17.8337
        lineTo(x = 5.10571f, y = 17.8337f)
        // C 4.81282 18.1266 4.81282 18.6015 5.10571 18.8943
        curveTo(
          x1 = 4.81282f,
          y1 = 18.1266f,
          x2 = 4.81282f,
          y2 = 18.6015f,
          x3 = 5.10571f,
          y3 = 18.8943f,
        )
        // C 5.3986 19.1872 5.87348 19.1872 6.16637 18.8943
        curveTo(
          x1 = 5.3986f,
          y1 = 19.1872f,
          x2 = 5.87348f,
          y2 = 19.1872f,
          x3 = 6.16637f,
          y3 = 18.8943f,
        )
        // L 17.25 7.81071
        lineTo(x = 17.25f, y = 7.81071f)
        // V 14
        verticalLineTo(y = 14.0f)
        // C 17.25 14.4142 17.5858 14.75 18 14.75
        curveTo(
          x1 = 17.25f,
          y1 = 14.4142f,
          x2 = 17.5858f,
          y2 = 14.75f,
          x3 = 18.0f,
          y3 = 14.75f,
        )
        // C 18.4142 14.75 18.75 14.4142 18.75 14
        curveTo(
          x1 = 18.4142f,
          y1 = 14.75f,
          x2 = 18.75f,
          y2 = 14.4142f,
          x3 = 18.75f,
          y3 = 14.0f,
        )
        // V 7
        verticalLineTo(y = 7.0f)
        // C 18.75 6.0335 17.9665 5.25 17 5.25
        curveTo(
          x1 = 18.75f,
          y1 = 6.0335f,
          x2 = 17.9665f,
          y2 = 5.25f,
          x3 = 17.0f,
          y3 = 5.25f,
        )
        // L 10 5.25z
        lineTo(x = 10.0f, y = 5.25f)
        close()
      }
    }.build().also { _arrowNorthEast = it }
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
        imageVector = HedvigIcons.ArrowNorthEast,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _arrowNorthEast: ImageVector? = null
