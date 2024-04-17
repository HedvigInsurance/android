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
val HedvigIcons.Lock: ImageVector
  get() {
    val current = _lock
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Lock",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 5.25 C13.7949 5.25 15.25 6.70507 15.25 8.5 L15.25 9.75 H8.75 L8.75 8.5 C8.75 6.70507 10.2051 5.25 12 5.25Z M7.25 9.76121 V8.5 C7.25 5.87665 9.37665 3.75 12 3.75 C14.6234 3.75 16.75 5.87665 16.75 8.5 L16.75 9.76121 C18.1516 9.88752 19.25 11.0655 19.25 12.5 V17.5 C19.25 19.0188 18.0188 20.25 16.5 20.25 H7.5 C5.98122 20.25 4.75 19.0188 4.75 17.5 V12.5 C4.75 11.0655 5.84838 9.88752 7.25 9.76121Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 12 5.25
        moveTo(x = 12.0f, y = 5.25f)
        // C 13.7949 5.25 15.25 6.70507 15.25 8.5
        curveTo(
          x1 = 13.7949f,
          y1 = 5.25f,
          x2 = 15.25f,
          y2 = 6.70507f,
          x3 = 15.25f,
          y3 = 8.5f,
        )
        // L 15.25 9.75
        lineTo(x = 15.25f, y = 9.75f)
        // H 8.75
        horizontalLineTo(x = 8.75f)
        // L 8.75 8.5
        lineTo(x = 8.75f, y = 8.5f)
        // C 8.75 6.70507 10.2051 5.25 12 5.25z
        curveTo(
          x1 = 8.75f,
          y1 = 6.70507f,
          x2 = 10.2051f,
          y2 = 5.25f,
          x3 = 12.0f,
          y3 = 5.25f,
        )
        close()
        // M 7.25 9.76121
        moveTo(x = 7.25f, y = 9.76121f)
        // V 8.5
        verticalLineTo(y = 8.5f)
        // C 7.25 5.87665 9.37665 3.75 12 3.75
        curveTo(
          x1 = 7.25f,
          y1 = 5.87665f,
          x2 = 9.37665f,
          y2 = 3.75f,
          x3 = 12.0f,
          y3 = 3.75f,
        )
        // C 14.6234 3.75 16.75 5.87665 16.75 8.5
        curveTo(
          x1 = 14.6234f,
          y1 = 3.75f,
          x2 = 16.75f,
          y2 = 5.87665f,
          x3 = 16.75f,
          y3 = 8.5f,
        )
        // L 16.75 9.76121
        lineTo(x = 16.75f, y = 9.76121f)
        // C 18.1516 9.88752 19.25 11.0655 19.25 12.5
        curveTo(
          x1 = 18.1516f,
          y1 = 9.88752f,
          x2 = 19.25f,
          y2 = 11.0655f,
          x3 = 19.25f,
          y3 = 12.5f,
        )
        // V 17.5
        verticalLineTo(y = 17.5f)
        // C 19.25 19.0188 18.0188 20.25 16.5 20.25
        curveTo(
          x1 = 19.25f,
          y1 = 19.0188f,
          x2 = 18.0188f,
          y2 = 20.25f,
          x3 = 16.5f,
          y3 = 20.25f,
        )
        // H 7.5
        horizontalLineTo(x = 7.5f)
        // C 5.98122 20.25 4.75 19.0188 4.75 17.5
        curveTo(
          x1 = 5.98122f,
          y1 = 20.25f,
          x2 = 4.75f,
          y2 = 19.0188f,
          x3 = 4.75f,
          y3 = 17.5f,
        )
        // V 12.5
        verticalLineTo(y = 12.5f)
        // C 4.75 11.0655 5.84838 9.88752 7.25 9.76121z
        curveTo(
          x1 = 4.75f,
          y1 = 11.0655f,
          x2 = 5.84838f,
          y2 = 9.88752f,
          x3 = 7.25f,
          y3 = 9.76121f,
        )
        close()
      }
    }.build().also { _lock = it }
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
        imageVector = HedvigIcons.Lock,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _lock: ImageVector? = null
