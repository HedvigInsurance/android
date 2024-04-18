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
val HedvigIcons.Attach: ImageVector
  get() {
    val current = _attach
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Attach",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M7.75 7 C7.75 5.48122 8.98122 4.25 10.5 4.25 C12.0188 4.25 13.25 5.48122 13.25 7 V15 C13.25 15.5523 12.8023 16 12.25 16 C11.6977 16 11.25 15.5523 11.25 15 V7 C11.25 6.58579 10.9142 6.25 10.5 6.25 C10.0858 6.25 9.75 6.58579 9.75 7 V15 C9.75 16.3807 10.8693 17.5 12.25 17.5 C13.6307 17.5 14.75 16.3807 14.75 15 V7 C14.75 4.65279 12.8472 2.75 10.5 2.75 C8.15279 2.75 6.25 4.65279 6.25 7 V15.25 C6.25 18.5637 8.93629 21.25 12.25 21.25 C15.5637 21.25 18.25 18.5637 18.25 15.25 V3.5 C18.25 3.08579 17.9142 2.75 17.5 2.75 C17.0858 2.75 16.75 3.08579 16.75 3.5 V15.25 C16.75 17.7353 14.7353 19.75 12.25 19.75 C9.76472 19.75 7.75 17.7353 7.75 15.25 V7Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 7.75 7
        moveTo(x = 7.75f, y = 7.0f)
        // C 7.75 5.48122 8.98122 4.25 10.5 4.25
        curveTo(
          x1 = 7.75f,
          y1 = 5.48122f,
          x2 = 8.98122f,
          y2 = 4.25f,
          x3 = 10.5f,
          y3 = 4.25f,
        )
        // C 12.0188 4.25 13.25 5.48122 13.25 7
        curveTo(
          x1 = 12.0188f,
          y1 = 4.25f,
          x2 = 13.25f,
          y2 = 5.48122f,
          x3 = 13.25f,
          y3 = 7.0f,
        )
        // V 15
        verticalLineTo(y = 15.0f)
        // C 13.25 15.5523 12.8023 16 12.25 16
        curveTo(
          x1 = 13.25f,
          y1 = 15.5523f,
          x2 = 12.8023f,
          y2 = 16.0f,
          x3 = 12.25f,
          y3 = 16.0f,
        )
        // C 11.6977 16 11.25 15.5523 11.25 15
        curveTo(
          x1 = 11.6977f,
          y1 = 16.0f,
          x2 = 11.25f,
          y2 = 15.5523f,
          x3 = 11.25f,
          y3 = 15.0f,
        )
        // V 7
        verticalLineTo(y = 7.0f)
        // C 11.25 6.58579 10.9142 6.25 10.5 6.25
        curveTo(
          x1 = 11.25f,
          y1 = 6.58579f,
          x2 = 10.9142f,
          y2 = 6.25f,
          x3 = 10.5f,
          y3 = 6.25f,
        )
        // C 10.0858 6.25 9.75 6.58579 9.75 7
        curveTo(
          x1 = 10.0858f,
          y1 = 6.25f,
          x2 = 9.75f,
          y2 = 6.58579f,
          x3 = 9.75f,
          y3 = 7.0f,
        )
        // V 15
        verticalLineTo(y = 15.0f)
        // C 9.75 16.3807 10.8693 17.5 12.25 17.5
        curveTo(
          x1 = 9.75f,
          y1 = 16.3807f,
          x2 = 10.8693f,
          y2 = 17.5f,
          x3 = 12.25f,
          y3 = 17.5f,
        )
        // C 13.6307 17.5 14.75 16.3807 14.75 15
        curveTo(
          x1 = 13.6307f,
          y1 = 17.5f,
          x2 = 14.75f,
          y2 = 16.3807f,
          x3 = 14.75f,
          y3 = 15.0f,
        )
        // V 7
        verticalLineTo(y = 7.0f)
        // C 14.75 4.65279 12.8472 2.75 10.5 2.75
        curveTo(
          x1 = 14.75f,
          y1 = 4.65279f,
          x2 = 12.8472f,
          y2 = 2.75f,
          x3 = 10.5f,
          y3 = 2.75f,
        )
        // C 8.15279 2.75 6.25 4.65279 6.25 7
        curveTo(
          x1 = 8.15279f,
          y1 = 2.75f,
          x2 = 6.25f,
          y2 = 4.65279f,
          x3 = 6.25f,
          y3 = 7.0f,
        )
        // V 15.25
        verticalLineTo(y = 15.25f)
        // C 6.25 18.5637 8.93629 21.25 12.25 21.25
        curveTo(
          x1 = 6.25f,
          y1 = 18.5637f,
          x2 = 8.93629f,
          y2 = 21.25f,
          x3 = 12.25f,
          y3 = 21.25f,
        )
        // C 15.5637 21.25 18.25 18.5637 18.25 15.25
        curveTo(
          x1 = 15.5637f,
          y1 = 21.25f,
          x2 = 18.25f,
          y2 = 18.5637f,
          x3 = 18.25f,
          y3 = 15.25f,
        )
        // V 3.5
        verticalLineTo(y = 3.5f)
        // C 18.25 3.08579 17.9142 2.75 17.5 2.75
        curveTo(
          x1 = 18.25f,
          y1 = 3.08579f,
          x2 = 17.9142f,
          y2 = 2.75f,
          x3 = 17.5f,
          y3 = 2.75f,
        )
        // C 17.0858 2.75 16.75 3.08579 16.75 3.5
        curveTo(
          x1 = 17.0858f,
          y1 = 2.75f,
          x2 = 16.75f,
          y2 = 3.08579f,
          x3 = 16.75f,
          y3 = 3.5f,
        )
        // V 15.25
        verticalLineTo(y = 15.25f)
        // C 16.75 17.7353 14.7353 19.75 12.25 19.75
        curveTo(
          x1 = 16.75f,
          y1 = 17.7353f,
          x2 = 14.7353f,
          y2 = 19.75f,
          x3 = 12.25f,
          y3 = 19.75f,
        )
        // C 9.76472 19.75 7.75 17.7353 7.75 15.25
        curveTo(
          x1 = 9.76472f,
          y1 = 19.75f,
          x2 = 7.75f,
          y2 = 17.7353f,
          x3 = 7.75f,
          y3 = 15.25f,
        )
        // V 7z
        verticalLineTo(y = 7.0f)
        close()
      }
    }.build().also { _attach = it }
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
        imageVector = HedvigIcons.Attach,
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
