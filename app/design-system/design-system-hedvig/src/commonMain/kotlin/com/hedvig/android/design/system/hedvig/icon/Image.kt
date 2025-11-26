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
val HedvigIcons.Image: ImageVector
  get() {
    val current = _image
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Image",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M19 5.25 C20.5188 5.25 21.75 6.48122 21.75 8 V16 C21.75 17.5188 20.5188 18.75 19 18.75 L5 18.75 C3.48122 18.75 2.25 17.5188 2.25 16 V8 C2.25 6.48122 3.48122 5.25 5 5.25 H19Z M20.25 8 C20.25 7.30964 19.6904 6.75 19 6.75 L5 6.75 C4.30964 6.75 3.75 7.30964 3.75 8 L3.75 16 C3.75 16.0428 3.75215 16.0852 3.75636 16.1269 L8.14245 10.9433 C8.85413 10.1023 10.1566 10.1202 10.8449 10.9805 L13.3877 14.1591 C13.4808 14.2754 13.6544 14.285 13.7597 14.1797 L14.7626 13.1768 C15.446 12.4934 16.5541 12.4934 17.2375 13.1768 L20.2376 16.1769 C20.2458 16.1191 20.25 16.0601 20.25 16 V8Z M19.1769 17.2376 L16.1768 14.2375 C16.0792 14.1398 15.9209 14.1398 15.8233 14.2375 L14.8204 15.2403 C14.0829 15.9778 12.868 15.9106 12.2164 15.0961 L9.67359 11.9176 C9.57527 11.7947 9.3892 11.7921 9.28753 11.9122 L4.78636 17.2318 C4.85578 17.2438 4.92716 17.25 5 17.25 L19 17.25 C19.0601 17.25 19.1191 17.2458 19.1769 17.2376Z M19 9.5 C19 10.3284 18.3284 11 17.5 11 C16.6716 11 16 10.3284 16 9.5 C16 8.67157 16.6716 8 17.5 8 C18.3284 8 19 8.67157 19 9.5Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 19 5.25
        moveTo(x = 19.0f, y = 5.25f)
        // C 20.5188 5.25 21.75 6.48122 21.75 8
        curveTo(
          x1 = 20.5188f,
          y1 = 5.25f,
          x2 = 21.75f,
          y2 = 6.48122f,
          x3 = 21.75f,
          y3 = 8.0f,
        )
        // V 16
        verticalLineTo(y = 16.0f)
        // C 21.75 17.5188 20.5188 18.75 19 18.75
        curveTo(
          x1 = 21.75f,
          y1 = 17.5188f,
          x2 = 20.5188f,
          y2 = 18.75f,
          x3 = 19.0f,
          y3 = 18.75f,
        )
        // L 5 18.75
        lineTo(x = 5.0f, y = 18.75f)
        // C 3.48122 18.75 2.25 17.5188 2.25 16
        curveTo(
          x1 = 3.48122f,
          y1 = 18.75f,
          x2 = 2.25f,
          y2 = 17.5188f,
          x3 = 2.25f,
          y3 = 16.0f,
        )
        // V 8
        verticalLineTo(y = 8.0f)
        // C 2.25 6.48122 3.48122 5.25 5 5.25
        curveTo(
          x1 = 2.25f,
          y1 = 6.48122f,
          x2 = 3.48122f,
          y2 = 5.25f,
          x3 = 5.0f,
          y3 = 5.25f,
        )
        // H 19z
        horizontalLineTo(x = 19.0f)
        close()
        // M 20.25 8
        moveTo(x = 20.25f, y = 8.0f)
        // C 20.25 7.30964 19.6904 6.75 19 6.75
        curveTo(
          x1 = 20.25f,
          y1 = 7.30964f,
          x2 = 19.6904f,
          y2 = 6.75f,
          x3 = 19.0f,
          y3 = 6.75f,
        )
        // L 5 6.75
        lineTo(x = 5.0f, y = 6.75f)
        // C 4.30964 6.75 3.75 7.30964 3.75 8
        curveTo(
          x1 = 4.30964f,
          y1 = 6.75f,
          x2 = 3.75f,
          y2 = 7.30964f,
          x3 = 3.75f,
          y3 = 8.0f,
        )
        // L 3.75 16
        lineTo(x = 3.75f, y = 16.0f)
        // C 3.75 16.0428 3.75215 16.0852 3.75636 16.1269
        curveTo(
          x1 = 3.75f,
          y1 = 16.0428f,
          x2 = 3.75215f,
          y2 = 16.0852f,
          x3 = 3.75636f,
          y3 = 16.1269f,
        )
        // L 8.14245 10.9433
        lineTo(x = 8.14245f, y = 10.9433f)
        // C 8.85413 10.1023 10.1566 10.1202 10.8449 10.9805
        curveTo(
          x1 = 8.85413f,
          y1 = 10.1023f,
          x2 = 10.1566f,
          y2 = 10.1202f,
          x3 = 10.8449f,
          y3 = 10.9805f,
        )
        // L 13.3877 14.1591
        lineTo(x = 13.3877f, y = 14.1591f)
        // C 13.4808 14.2754 13.6544 14.285 13.7597 14.1797
        curveTo(
          x1 = 13.4808f,
          y1 = 14.2754f,
          x2 = 13.6544f,
          y2 = 14.285f,
          x3 = 13.7597f,
          y3 = 14.1797f,
        )
        // L 14.7626 13.1768
        lineTo(x = 14.7626f, y = 13.1768f)
        // C 15.446 12.4934 16.5541 12.4934 17.2375 13.1768
        curveTo(
          x1 = 15.446f,
          y1 = 12.4934f,
          x2 = 16.5541f,
          y2 = 12.4934f,
          x3 = 17.2375f,
          y3 = 13.1768f,
        )
        // L 20.2376 16.1769
        lineTo(x = 20.2376f, y = 16.1769f)
        // C 20.2458 16.1191 20.25 16.0601 20.25 16
        curveTo(
          x1 = 20.2458f,
          y1 = 16.1191f,
          x2 = 20.25f,
          y2 = 16.0601f,
          x3 = 20.25f,
          y3 = 16.0f,
        )
        // V 8z
        verticalLineTo(y = 8.0f)
        close()
        // M 19.1769 17.2376
        moveTo(x = 19.1769f, y = 17.2376f)
        // L 16.1768 14.2375
        lineTo(x = 16.1768f, y = 14.2375f)
        // C 16.0792 14.1398 15.9209 14.1398 15.8233 14.2375
        curveTo(
          x1 = 16.0792f,
          y1 = 14.1398f,
          x2 = 15.9209f,
          y2 = 14.1398f,
          x3 = 15.8233f,
          y3 = 14.2375f,
        )
        // L 14.8204 15.2403
        lineTo(x = 14.8204f, y = 15.2403f)
        // C 14.0829 15.9778 12.868 15.9106 12.2164 15.0961
        curveTo(
          x1 = 14.0829f,
          y1 = 15.9778f,
          x2 = 12.868f,
          y2 = 15.9106f,
          x3 = 12.2164f,
          y3 = 15.0961f,
        )
        // L 9.67359 11.9176
        lineTo(x = 9.67359f, y = 11.9176f)
        // C 9.57527 11.7947 9.3892 11.7921 9.28753 11.9122
        curveTo(
          x1 = 9.57527f,
          y1 = 11.7947f,
          x2 = 9.3892f,
          y2 = 11.7921f,
          x3 = 9.28753f,
          y3 = 11.9122f,
        )
        // L 4.78636 17.2318
        lineTo(x = 4.78636f, y = 17.2318f)
        // C 4.85578 17.2438 4.92716 17.25 5 17.25
        curveTo(
          x1 = 4.85578f,
          y1 = 17.2438f,
          x2 = 4.92716f,
          y2 = 17.25f,
          x3 = 5.0f,
          y3 = 17.25f,
        )
        // L 19 17.25
        lineTo(x = 19.0f, y = 17.25f)
        // C 19.0601 17.25 19.1191 17.2458 19.1769 17.2376z
        curveTo(
          x1 = 19.0601f,
          y1 = 17.25f,
          x2 = 19.1191f,
          y2 = 17.2458f,
          x3 = 19.1769f,
          y3 = 17.2376f,
        )
        close()
        // M 19 9.5
        moveTo(x = 19.0f, y = 9.5f)
        // C 19 10.3284 18.3284 11 17.5 11
        curveTo(
          x1 = 19.0f,
          y1 = 10.3284f,
          x2 = 18.3284f,
          y2 = 11.0f,
          x3 = 17.5f,
          y3 = 11.0f,
        )
        // C 16.6716 11 16 10.3284 16 9.5
        curveTo(
          x1 = 16.6716f,
          y1 = 11.0f,
          x2 = 16.0f,
          y2 = 10.3284f,
          x3 = 16.0f,
          y3 = 9.5f,
        )
        // C 16 8.67157 16.6716 8 17.5 8
        curveTo(
          x1 = 16.0f,
          y1 = 8.67157f,
          x2 = 16.6716f,
          y2 = 8.0f,
          x3 = 17.5f,
          y3 = 8.0f,
        )
        // C 18.3284 8 19 8.67157 19 9.5z
        curveTo(
          x1 = 18.3284f,
          y1 = 8.0f,
          x2 = 19.0f,
          y2 = 8.67157f,
          x3 = 19.0f,
          y3 = 9.5f,
        )
        close()
      }
    }.build().also { _image = it }
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
        imageVector = HedvigIcons.Image,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _image: ImageVector? = null
