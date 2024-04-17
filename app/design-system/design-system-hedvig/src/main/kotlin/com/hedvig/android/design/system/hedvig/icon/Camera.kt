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
val HedvigIcons.Camera: ImageVector
  get() {
    val current = _camera
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Camera",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M21.75 9 C21.75 7.48122 20.5188 6.25 19 6.25 L17.1324 6.25 C16.6933 6.25 16.2864 6.01962 16.0605 5.64312 L15.7257 5.08514 C15.2287 4.25682 14.3336 3.75 13.3676 3.75 H10.6324 C9.66641 3.75 8.77126 4.25682 8.27427 5.08514 L7.93949 5.64312 C7.71358 6.01963 7.3067 6.25 6.86762 6.25 L5 6.25 C3.48122 6.25 2.25 7.48122 2.25 9 V17 C2.25 18.5188 3.48122 19.75 5 19.75 H19 C20.5188 19.75 21.75 18.5188 21.75 17 V9Z M19 7.75 C19.6904 7.75 20.25 8.30964 20.25 9 V17 C20.25 17.6904 19.6904 18.25 19 18.25 H5 C4.30964 18.25 3.75 17.6904 3.75 17 L3.75 9 C3.75 8.30964 4.30964 7.75 5 7.75 L6.86762 7.75 C7.83359 7.75 8.72874 7.24318 9.22573 6.41486 L9.56051 5.85688 C9.78642 5.48037 10.1933 5.25 10.6324 5.25 H13.3676 C13.8067 5.25 14.2136 5.48037 14.4395 5.85688 L14.7743 6.41486 C15.2713 7.24318 16.1664 7.75 17.1324 7.75 L19 7.75Z M14 12.5 C14 13.6046 13.1046 14.5 12 14.5 C10.8954 14.5 10 13.6046 10 12.5 C10 11.3954 10.8954 10.5 12 10.5 C13.1046 10.5 14 11.3954 14 12.5Z M15.5 12.5 C15.5 14.433 13.933 16 12 16 C10.067 16 8.5 14.433 8.5 12.5 C8.5 10.567 10.067 9 12 9 C13.933 9 15.5 10.567 15.5 12.5Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 21.75 9
        moveTo(x = 21.75f, y = 9.0f)
        // C 21.75 7.48122 20.5188 6.25 19 6.25
        curveTo(
          x1 = 21.75f,
          y1 = 7.48122f,
          x2 = 20.5188f,
          y2 = 6.25f,
          x3 = 19.0f,
          y3 = 6.25f,
        )
        // L 17.1324 6.25
        lineTo(x = 17.1324f, y = 6.25f)
        // C 16.6933 6.25 16.2864 6.01962 16.0605 5.64312
        curveTo(
          x1 = 16.6933f,
          y1 = 6.25f,
          x2 = 16.2864f,
          y2 = 6.01962f,
          x3 = 16.0605f,
          y3 = 5.64312f,
        )
        // L 15.7257 5.08514
        lineTo(x = 15.7257f, y = 5.08514f)
        // C 15.2287 4.25682 14.3336 3.75 13.3676 3.75
        curveTo(
          x1 = 15.2287f,
          y1 = 4.25682f,
          x2 = 14.3336f,
          y2 = 3.75f,
          x3 = 13.3676f,
          y3 = 3.75f,
        )
        // H 10.6324
        horizontalLineTo(x = 10.6324f)
        // C 9.66641 3.75 8.77126 4.25682 8.27427 5.08514
        curveTo(
          x1 = 9.66641f,
          y1 = 3.75f,
          x2 = 8.77126f,
          y2 = 4.25682f,
          x3 = 8.27427f,
          y3 = 5.08514f,
        )
        // L 7.93949 5.64312
        lineTo(x = 7.93949f, y = 5.64312f)
        // C 7.71358 6.01963 7.3067 6.25 6.86762 6.25
        curveTo(
          x1 = 7.71358f,
          y1 = 6.01963f,
          x2 = 7.3067f,
          y2 = 6.25f,
          x3 = 6.86762f,
          y3 = 6.25f,
        )
        // L 5 6.25
        lineTo(x = 5.0f, y = 6.25f)
        // C 3.48122 6.25 2.25 7.48122 2.25 9
        curveTo(
          x1 = 3.48122f,
          y1 = 6.25f,
          x2 = 2.25f,
          y2 = 7.48122f,
          x3 = 2.25f,
          y3 = 9.0f,
        )
        // V 17
        verticalLineTo(y = 17.0f)
        // C 2.25 18.5188 3.48122 19.75 5 19.75
        curveTo(
          x1 = 2.25f,
          y1 = 18.5188f,
          x2 = 3.48122f,
          y2 = 19.75f,
          x3 = 5.0f,
          y3 = 19.75f,
        )
        // H 19
        horizontalLineTo(x = 19.0f)
        // C 20.5188 19.75 21.75 18.5188 21.75 17
        curveTo(
          x1 = 20.5188f,
          y1 = 19.75f,
          x2 = 21.75f,
          y2 = 18.5188f,
          x3 = 21.75f,
          y3 = 17.0f,
        )
        // V 9z
        verticalLineTo(y = 9.0f)
        close()
        // M 19 7.75
        moveTo(x = 19.0f, y = 7.75f)
        // C 19.6904 7.75 20.25 8.30964 20.25 9
        curveTo(
          x1 = 19.6904f,
          y1 = 7.75f,
          x2 = 20.25f,
          y2 = 8.30964f,
          x3 = 20.25f,
          y3 = 9.0f,
        )
        // V 17
        verticalLineTo(y = 17.0f)
        // C 20.25 17.6904 19.6904 18.25 19 18.25
        curveTo(
          x1 = 20.25f,
          y1 = 17.6904f,
          x2 = 19.6904f,
          y2 = 18.25f,
          x3 = 19.0f,
          y3 = 18.25f,
        )
        // H 5
        horizontalLineTo(x = 5.0f)
        // C 4.30964 18.25 3.75 17.6904 3.75 17
        curveTo(
          x1 = 4.30964f,
          y1 = 18.25f,
          x2 = 3.75f,
          y2 = 17.6904f,
          x3 = 3.75f,
          y3 = 17.0f,
        )
        // L 3.75 9
        lineTo(x = 3.75f, y = 9.0f)
        // C 3.75 8.30964 4.30964 7.75 5 7.75
        curveTo(
          x1 = 3.75f,
          y1 = 8.30964f,
          x2 = 4.30964f,
          y2 = 7.75f,
          x3 = 5.0f,
          y3 = 7.75f,
        )
        // L 6.86762 7.75
        lineTo(x = 6.86762f, y = 7.75f)
        // C 7.83359 7.75 8.72874 7.24318 9.22573 6.41486
        curveTo(
          x1 = 7.83359f,
          y1 = 7.75f,
          x2 = 8.72874f,
          y2 = 7.24318f,
          x3 = 9.22573f,
          y3 = 6.41486f,
        )
        // L 9.56051 5.85688
        lineTo(x = 9.56051f, y = 5.85688f)
        // C 9.78642 5.48037 10.1933 5.25 10.6324 5.25
        curveTo(
          x1 = 9.78642f,
          y1 = 5.48037f,
          x2 = 10.1933f,
          y2 = 5.25f,
          x3 = 10.6324f,
          y3 = 5.25f,
        )
        // H 13.3676
        horizontalLineTo(x = 13.3676f)
        // C 13.8067 5.25 14.2136 5.48037 14.4395 5.85688
        curveTo(
          x1 = 13.8067f,
          y1 = 5.25f,
          x2 = 14.2136f,
          y2 = 5.48037f,
          x3 = 14.4395f,
          y3 = 5.85688f,
        )
        // L 14.7743 6.41486
        lineTo(x = 14.7743f, y = 6.41486f)
        // C 15.2713 7.24318 16.1664 7.75 17.1324 7.75
        curveTo(
          x1 = 15.2713f,
          y1 = 7.24318f,
          x2 = 16.1664f,
          y2 = 7.75f,
          x3 = 17.1324f,
          y3 = 7.75f,
        )
        // L 19 7.75z
        lineTo(x = 19.0f, y = 7.75f)
        close()
        // M 14 12.5
        moveTo(x = 14.0f, y = 12.5f)
        // C 14 13.6046 13.1046 14.5 12 14.5
        curveTo(
          x1 = 14.0f,
          y1 = 13.6046f,
          x2 = 13.1046f,
          y2 = 14.5f,
          x3 = 12.0f,
          y3 = 14.5f,
        )
        // C 10.8954 14.5 10 13.6046 10 12.5
        curveTo(
          x1 = 10.8954f,
          y1 = 14.5f,
          x2 = 10.0f,
          y2 = 13.6046f,
          x3 = 10.0f,
          y3 = 12.5f,
        )
        // C 10 11.3954 10.8954 10.5 12 10.5
        curveTo(
          x1 = 10.0f,
          y1 = 11.3954f,
          x2 = 10.8954f,
          y2 = 10.5f,
          x3 = 12.0f,
          y3 = 10.5f,
        )
        // C 13.1046 10.5 14 11.3954 14 12.5z
        curveTo(
          x1 = 13.1046f,
          y1 = 10.5f,
          x2 = 14.0f,
          y2 = 11.3954f,
          x3 = 14.0f,
          y3 = 12.5f,
        )
        close()
        // M 15.5 12.5
        moveTo(x = 15.5f, y = 12.5f)
        // C 15.5 14.433 13.933 16 12 16
        curveTo(
          x1 = 15.5f,
          y1 = 14.433f,
          x2 = 13.933f,
          y2 = 16.0f,
          x3 = 12.0f,
          y3 = 16.0f,
        )
        // C 10.067 16 8.5 14.433 8.5 12.5
        curveTo(
          x1 = 10.067f,
          y1 = 16.0f,
          x2 = 8.5f,
          y2 = 14.433f,
          x3 = 8.5f,
          y3 = 12.5f,
        )
        // C 8.5 10.567 10.067 9 12 9
        curveTo(
          x1 = 8.5f,
          y1 = 10.567f,
          x2 = 10.067f,
          y2 = 9.0f,
          x3 = 12.0f,
          y3 = 9.0f,
        )
        // C 13.933 9 15.5 10.567 15.5 12.5z
        curveTo(
          x1 = 13.933f,
          y1 = 9.0f,
          x2 = 15.5f,
          y2 = 10.567f,
          x3 = 15.5f,
          y3 = 12.5f,
        )
        close()
      }
    }.build().also { _camera = it }
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
        imageVector = HedvigIcons.Camera,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _camera: ImageVector? = null
