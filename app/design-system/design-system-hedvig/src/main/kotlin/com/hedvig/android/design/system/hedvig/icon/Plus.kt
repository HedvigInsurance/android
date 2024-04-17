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
val HedvigIcons.Plus: ImageVector
  get() {
    val current = _plus
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Plus",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M18 12.75 C18.4142 12.75 18.75 12.4142 18.75 12 C18.75 11.5858 18.4142 11.25 18 11.25 L12.75 11.25 L12.75 6 C12.75 5.58579 12.4142 5.25 12 5.25 C11.5858 5.25 11.25 5.58579 11.25 6 L11.25 11.25 L6 11.25 C5.58579 11.25 5.25 11.5858 5.25 12 C5.25 12.4142 5.58579 12.75 6 12.75 L11.25 12.75 L11.25 18 C11.25 18.4142 11.5858 18.75 12 18.75 C12.4142 18.75 12.75 18.4142 12.75 18 L12.75 12.75 L18 12.75Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 18 12.75
        moveTo(x = 18.0f, y = 12.75f)
        // C 18.4142 12.75 18.75 12.4142 18.75 12
        curveTo(
          x1 = 18.4142f,
          y1 = 12.75f,
          x2 = 18.75f,
          y2 = 12.4142f,
          x3 = 18.75f,
          y3 = 12.0f,
        )
        // C 18.75 11.5858 18.4142 11.25 18 11.25
        curveTo(
          x1 = 18.75f,
          y1 = 11.5858f,
          x2 = 18.4142f,
          y2 = 11.25f,
          x3 = 18.0f,
          y3 = 11.25f,
        )
        // L 12.75 11.25
        lineTo(x = 12.75f, y = 11.25f)
        // L 12.75 6
        lineTo(x = 12.75f, y = 6.0f)
        // C 12.75 5.58579 12.4142 5.25 12 5.25
        curveTo(
          x1 = 12.75f,
          y1 = 5.58579f,
          x2 = 12.4142f,
          y2 = 5.25f,
          x3 = 12.0f,
          y3 = 5.25f,
        )
        // C 11.5858 5.25 11.25 5.58579 11.25 6
        curveTo(
          x1 = 11.5858f,
          y1 = 5.25f,
          x2 = 11.25f,
          y2 = 5.58579f,
          x3 = 11.25f,
          y3 = 6.0f,
        )
        // L 11.25 11.25
        lineTo(x = 11.25f, y = 11.25f)
        // L 6 11.25
        lineTo(x = 6.0f, y = 11.25f)
        // C 5.58579 11.25 5.25 11.5858 5.25 12
        curveTo(
          x1 = 5.58579f,
          y1 = 11.25f,
          x2 = 5.25f,
          y2 = 11.5858f,
          x3 = 5.25f,
          y3 = 12.0f,
        )
        // C 5.25 12.4142 5.58579 12.75 6 12.75
        curveTo(
          x1 = 5.25f,
          y1 = 12.4142f,
          x2 = 5.58579f,
          y2 = 12.75f,
          x3 = 6.0f,
          y3 = 12.75f,
        )
        // L 11.25 12.75
        lineTo(x = 11.25f, y = 12.75f)
        // L 11.25 18
        lineTo(x = 11.25f, y = 18.0f)
        // C 11.25 18.4142 11.5858 18.75 12 18.75
        curveTo(
          x1 = 11.25f,
          y1 = 18.4142f,
          x2 = 11.5858f,
          y2 = 18.75f,
          x3 = 12.0f,
          y3 = 18.75f,
        )
        // C 12.4142 18.75 12.75 18.4142 12.75 18
        curveTo(
          x1 = 12.4142f,
          y1 = 18.75f,
          x2 = 12.75f,
          y2 = 18.4142f,
          x3 = 12.75f,
          y3 = 18.0f,
        )
        // L 12.75 12.75
        lineTo(x = 12.75f, y = 12.75f)
        // L 18 12.75z
        lineTo(x = 18.0f, y = 12.75f)
        close()
      }
    }.build().also { _plus = it }
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
        imageVector = HedvigIcons.Plus,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _plus: ImageVector? = null
