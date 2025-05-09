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
val HedvigIcons.Card: ImageVector
  get() {
    val current = _card
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Card",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M21.75 8 C21.75 6.48122 20.5188 5.25 19 5.25 H5 C3.48122 5.25 2.25 6.48122 2.25 8 V16 C2.25 17.5188 3.48122 18.75 5 18.75 L19 18.75 C20.5188 18.75 21.75 17.5188 21.75 16 V8Z M19 6.75 C19.6904 6.75 20.25 7.30964 20.25 8 V9 H3.75 V8 C3.75 7.30964 4.30964 6.75 5 6.75 L19 6.75Z M3.75 12 L3.75 16 C3.75 16.6904 4.30964 17.25 5 17.25 L19 17.25 C19.6904 17.25 20.25 16.6904 20.25 16 V12 H3.75Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 21.75 8
        moveTo(x = 21.75f, y = 8.0f)
        // C 21.75 6.48122 20.5188 5.25 19 5.25
        curveTo(
          x1 = 21.75f,
          y1 = 6.48122f,
          x2 = 20.5188f,
          y2 = 5.25f,
          x3 = 19.0f,
          y3 = 5.25f,
        )
        // H 5
        horizontalLineTo(x = 5.0f)
        // C 3.48122 5.25 2.25 6.48122 2.25 8
        curveTo(
          x1 = 3.48122f,
          y1 = 5.25f,
          x2 = 2.25f,
          y2 = 6.48122f,
          x3 = 2.25f,
          y3 = 8.0f,
        )
        // V 16
        verticalLineTo(y = 16.0f)
        // C 2.25 17.5188 3.48122 18.75 5 18.75
        curveTo(
          x1 = 2.25f,
          y1 = 17.5188f,
          x2 = 3.48122f,
          y2 = 18.75f,
          x3 = 5.0f,
          y3 = 18.75f,
        )
        // L 19 18.75
        lineTo(x = 19.0f, y = 18.75f)
        // C 20.5188 18.75 21.75 17.5188 21.75 16
        curveTo(
          x1 = 20.5188f,
          y1 = 18.75f,
          x2 = 21.75f,
          y2 = 17.5188f,
          x3 = 21.75f,
          y3 = 16.0f,
        )
        // V 8z
        verticalLineTo(y = 8.0f)
        close()
        // M 19 6.75
        moveTo(x = 19.0f, y = 6.75f)
        // C 19.6904 6.75 20.25 7.30964 20.25 8
        curveTo(
          x1 = 19.6904f,
          y1 = 6.75f,
          x2 = 20.25f,
          y2 = 7.30964f,
          x3 = 20.25f,
          y3 = 8.0f,
        )
        // V 9
        verticalLineTo(y = 9.0f)
        // H 3.75
        horizontalLineTo(x = 3.75f)
        // V 8
        verticalLineTo(y = 8.0f)
        // C 3.75 7.30964 4.30964 6.75 5 6.75
        curveTo(
          x1 = 3.75f,
          y1 = 7.30964f,
          x2 = 4.30964f,
          y2 = 6.75f,
          x3 = 5.0f,
          y3 = 6.75f,
        )
        // L 19 6.75z
        lineTo(x = 19.0f, y = 6.75f)
        close()
        // M 3.75 12
        moveTo(x = 3.75f, y = 12.0f)
        // L 3.75 16
        lineTo(x = 3.75f, y = 16.0f)
        // C 3.75 16.6904 4.30964 17.25 5 17.25
        curveTo(
          x1 = 3.75f,
          y1 = 16.6904f,
          x2 = 4.30964f,
          y2 = 17.25f,
          x3 = 5.0f,
          y3 = 17.25f,
        )
        // L 19 17.25
        lineTo(x = 19.0f, y = 17.25f)
        // C 19.6904 17.25 20.25 16.6904 20.25 16
        curveTo(
          x1 = 19.6904f,
          y1 = 17.25f,
          x2 = 20.25f,
          y2 = 16.6904f,
          x3 = 20.25f,
          y3 = 16.0f,
        )
        // V 12
        verticalLineTo(y = 12.0f)
        // H 3.75z
        horizontalLineTo(x = 3.75f)
        close()
      }
    }.build().also { _card = it }
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
        imageVector = HedvigIcons.Card,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _card: ImageVector? = null
