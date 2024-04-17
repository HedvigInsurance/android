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
val HedvigIcons.Star: ImageVector
  get() {
    val current = _star
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Star",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M10.3171 3.00707 C11.0683 1.66431 12.9317 1.66431 13.6829 3.00707 L15.5557 6.35426 C15.8309 6.84614 16.292 7.19493 16.8264 7.31541 L20.463 8.13521 C21.9219 8.46408 22.4977 10.309 21.5031 11.4677 L19.0239 14.3562 C18.6596 14.7807 18.4835 15.345 18.5385 15.9114 L18.9134 19.7652 C19.0637 21.3112 17.5562 22.4515 16.1903 21.8248 L12.7854 20.2628 C12.285 20.0333 11.715 20.0333 11.2146 20.2628 L7.80969 21.8248 C6.44377 22.4514 4.93627 21.3112 5.08663 19.7652 L5.46145 15.9114 C5.51653 15.345 5.34039 14.7807 4.97607 14.3562 L2.49688 11.4677 C1.50233 10.309 2.07814 8.46409 3.53699 8.13521 L7.17358 7.31541 C7.70799 7.19493 8.16914 6.84614 8.44434 6.35427 L10.3171 3.00707Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 10.3171 3.00707
        moveTo(x = 10.3171f, y = 3.00707f)
        // C 11.0683 1.66431 12.9317 1.66431 13.6829 3.00707
        curveTo(
          x1 = 11.0683f,
          y1 = 1.66431f,
          x2 = 12.9317f,
          y2 = 1.66431f,
          x3 = 13.6829f,
          y3 = 3.00707f,
        )
        // L 15.5557 6.35426
        lineTo(x = 15.5557f, y = 6.35426f)
        // C 15.8309 6.84614 16.292 7.19493 16.8264 7.31541
        curveTo(
          x1 = 15.8309f,
          y1 = 6.84614f,
          x2 = 16.292f,
          y2 = 7.19493f,
          x3 = 16.8264f,
          y3 = 7.31541f,
        )
        // L 20.463 8.13521
        lineTo(x = 20.463f, y = 8.13521f)
        // C 21.9219 8.46408 22.4977 10.309 21.5031 11.4677
        curveTo(
          x1 = 21.9219f,
          y1 = 8.46408f,
          x2 = 22.4977f,
          y2 = 10.309f,
          x3 = 21.5031f,
          y3 = 11.4677f,
        )
        // L 19.0239 14.3562
        lineTo(x = 19.0239f, y = 14.3562f)
        // C 18.6596 14.7807 18.4835 15.345 18.5385 15.9114
        curveTo(
          x1 = 18.6596f,
          y1 = 14.7807f,
          x2 = 18.4835f,
          y2 = 15.345f,
          x3 = 18.5385f,
          y3 = 15.9114f,
        )
        // L 18.9134 19.7652
        lineTo(x = 18.9134f, y = 19.7652f)
        // C 19.0637 21.3112 17.5562 22.4515 16.1903 21.8248
        curveTo(
          x1 = 19.0637f,
          y1 = 21.3112f,
          x2 = 17.5562f,
          y2 = 22.4515f,
          x3 = 16.1903f,
          y3 = 21.8248f,
        )
        // L 12.7854 20.2628
        lineTo(x = 12.7854f, y = 20.2628f)
        // C 12.285 20.0333 11.715 20.0333 11.2146 20.2628
        curveTo(
          x1 = 12.285f,
          y1 = 20.0333f,
          x2 = 11.715f,
          y2 = 20.0333f,
          x3 = 11.2146f,
          y3 = 20.2628f,
        )
        // L 7.80969 21.8248
        lineTo(x = 7.80969f, y = 21.8248f)
        // C 6.44377 22.4514 4.93627 21.3112 5.08663 19.7652
        curveTo(
          x1 = 6.44377f,
          y1 = 22.4514f,
          x2 = 4.93627f,
          y2 = 21.3112f,
          x3 = 5.08663f,
          y3 = 19.7652f,
        )
        // L 5.46145 15.9114
        lineTo(x = 5.46145f, y = 15.9114f)
        // C 5.51653 15.345 5.34039 14.7807 4.97607 14.3562
        curveTo(
          x1 = 5.51653f,
          y1 = 15.345f,
          x2 = 5.34039f,
          y2 = 14.7807f,
          x3 = 4.97607f,
          y3 = 14.3562f,
        )
        // L 2.49688 11.4677
        lineTo(x = 2.49688f, y = 11.4677f)
        // C 1.50233 10.309 2.07814 8.46409 3.53699 8.13521
        curveTo(
          x1 = 1.50233f,
          y1 = 10.309f,
          x2 = 2.07814f,
          y2 = 8.46409f,
          x3 = 3.53699f,
          y3 = 8.13521f,
        )
        // L 7.17358 7.31541
        lineTo(x = 7.17358f, y = 7.31541f)
        // C 7.70799 7.19493 8.16914 6.84614 8.44434 6.35427
        curveTo(
          x1 = 7.70799f,
          y1 = 7.19493f,
          x2 = 8.16914f,
          y2 = 6.84614f,
          x3 = 8.44434f,
          y3 = 6.35427f,
        )
        // L 10.3171 3.00707z
        lineTo(x = 10.3171f, y = 3.00707f)
        close()
      }
    }.build().also { _star = it }
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
        imageVector = HedvigIcons.Star,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _star: ImageVector? = null
