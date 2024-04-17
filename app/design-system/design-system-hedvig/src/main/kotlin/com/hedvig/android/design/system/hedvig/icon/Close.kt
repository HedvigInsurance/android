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

@Suppress("UnusedReceiverParameter")
val HedvigIcons.Close: ImageVector
  get() {
    val current = _close
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Close",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M6.87348 5.81276 C6.58059 5.51987 6.10571 5.51987 5.81282 5.81276 C5.51993 6.10566 5.51993 6.58053 5.81282 6.87342 L10.9393 11.9999 L5.81284 17.1265 C5.51995 17.4194 5.51995 17.8943 5.81285 18.1871 C6.10574 18.48 6.58062 18.48 6.87351 18.1871 L12 13.0606 L17.1265 18.1871 C17.4194 18.48 17.8943 18.48 18.1872 18.1871 C18.4801 17.8942 18.4801 17.4194 18.1872 17.1265 L13.0606 11.9999 L18.1871 6.87334 C18.48 6.58045 18.48 6.10557 18.1871 5.81268 C17.8942 5.51979 17.4193 5.5198 17.1265 5.81269 L12 10.9393 L6.87348 5.81276Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 6.87348 5.81276
        moveTo(x = 6.87348f, y = 5.81276f)
        // C 6.58059 5.51987 6.10571 5.51987 5.81282 5.81276
        curveTo(
          x1 = 6.58059f,
          y1 = 5.51987f,
          x2 = 6.10571f,
          y2 = 5.51987f,
          x3 = 5.81282f,
          y3 = 5.81276f,
        )
        // C 5.51993 6.10566 5.51993 6.58053 5.81282 6.87342
        curveTo(
          x1 = 5.51993f,
          y1 = 6.10566f,
          x2 = 5.51993f,
          y2 = 6.58053f,
          x3 = 5.81282f,
          y3 = 6.87342f,
        )
        // L 10.9393 11.9999
        lineTo(x = 10.9393f, y = 11.9999f)
        // L 5.81284 17.1265
        lineTo(x = 5.81284f, y = 17.1265f)
        // C 5.51995 17.4194 5.51995 17.8943 5.81285 18.1871
        curveTo(
          x1 = 5.51995f,
          y1 = 17.4194f,
          x2 = 5.51995f,
          y2 = 17.8943f,
          x3 = 5.81285f,
          y3 = 18.1871f,
        )
        // C 6.10574 18.48 6.58062 18.48 6.87351 18.1871
        curveTo(
          x1 = 6.10574f,
          y1 = 18.48f,
          x2 = 6.58062f,
          y2 = 18.48f,
          x3 = 6.87351f,
          y3 = 18.1871f,
        )
        // L 12 13.0606
        lineTo(x = 12.0f, y = 13.0606f)
        // L 17.1265 18.1871
        lineTo(x = 17.1265f, y = 18.1871f)
        // C 17.4194 18.48 17.8943 18.48 18.1872 18.1871
        curveTo(
          x1 = 17.4194f,
          y1 = 18.48f,
          x2 = 17.8943f,
          y2 = 18.48f,
          x3 = 18.1872f,
          y3 = 18.1871f,
        )
        // C 18.4801 17.8942 18.4801 17.4194 18.1872 17.1265
        curveTo(
          x1 = 18.4801f,
          y1 = 17.8942f,
          x2 = 18.4801f,
          y2 = 17.4194f,
          x3 = 18.1872f,
          y3 = 17.1265f,
        )
        // L 13.0606 11.9999
        lineTo(x = 13.0606f, y = 11.9999f)
        // L 18.1871 6.87334
        lineTo(x = 18.1871f, y = 6.87334f)
        // C 18.48 6.58045 18.48 6.10557 18.1871 5.81268
        curveTo(
          x1 = 18.48f,
          y1 = 6.58045f,
          x2 = 18.48f,
          y2 = 6.10557f,
          x3 = 18.1871f,
          y3 = 5.81268f,
        )
        // C 17.8942 5.51979 17.4193 5.5198 17.1265 5.81269
        curveTo(
          x1 = 17.8942f,
          y1 = 5.51979f,
          x2 = 17.4193f,
          y2 = 5.5198f,
          x3 = 17.1265f,
          y3 = 5.81269f,
        )
        // L 12 10.9393
        lineTo(x = 12.0f, y = 10.9393f)
        // L 6.87348 5.81276z
        lineTo(x = 6.87348f, y = 5.81276f)
        close()
      }
    }.build().also { _close = it }
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
        imageVector = HedvigIcons.Close,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _close: ImageVector? = null
