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
val HedvigIcons.Minus: ImageVector
  get() {
    val current = _minus
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Minus",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M5.25 12 C5.25 11.5858 5.58579 11.25 6 11.25 L18 11.25 C18.4142 11.25 18.75 11.5858 18.75 12 C18.75 12.4142 18.4142 12.75 18 12.75 L6 12.75 C5.58579 12.75 5.25 12.4142 5.25 12Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 5.25 12
        moveTo(x = 5.25f, y = 12.0f)
        // C 5.25 11.5858 5.58579 11.25 6 11.25
        curveTo(
          x1 = 5.25f,
          y1 = 11.5858f,
          x2 = 5.58579f,
          y2 = 11.25f,
          x3 = 6.0f,
          y3 = 11.25f,
        )
        // L 18 11.25
        lineTo(x = 18.0f, y = 11.25f)
        // C 18.4142 11.25 18.75 11.5858 18.75 12
        curveTo(
          x1 = 18.4142f,
          y1 = 11.25f,
          x2 = 18.75f,
          y2 = 11.5858f,
          x3 = 18.75f,
          y3 = 12.0f,
        )
        // C 18.75 12.4142 18.4142 12.75 18 12.75
        curveTo(
          x1 = 18.75f,
          y1 = 12.4142f,
          x2 = 18.4142f,
          y2 = 12.75f,
          x3 = 18.0f,
          y3 = 12.75f,
        )
        // L 6 12.75
        lineTo(x = 6.0f, y = 12.75f)
        // C 5.58579 12.75 5.25 12.4142 5.25 12z
        curveTo(
          x1 = 5.58579f,
          y1 = 12.75f,
          x2 = 5.25f,
          y2 = 12.4142f,
          x3 = 5.25f,
          y3 = 12.0f,
        )
        close()
      }
    }.build().also { _minus = it }
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
        imageVector = HedvigIcons.Minus,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _minus: ImageVector? = null
