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
val HedvigIcons.Dots: ImageVector
  get() {
    val current = _dots
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Dots",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M6.23079 13.5 C5.8183 13.5 5.46518 13.3531 5.17143 13.0593 C4.8777 12.7656 4.73083 12.4125 4.73083 12 C4.73083 11.5875 4.8777 11.2344 5.17143 10.9406 C5.46518 10.6469 5.8183 10.5 6.23079 10.5 C6.64329 10.5 6.9964 10.6469 7.29014 10.9406 C7.58389 11.2344 7.73076 11.5875 7.73076 12 C7.73076 12.4125 7.58389 12.7656 7.29014 13.0593 C6.9964 13.3531 6.64329 13.5 6.23079 13.5Z M12 13.5 C11.5875 13.5 11.2344 13.3531 10.9407 13.0593 C10.6469 12.7656 10.5 12.4125 10.5 12 C10.5 11.5875 10.6469 11.2344 10.9407 10.9406 C11.2344 10.6469 11.5875 10.5 12 10.5 C12.4125 10.5 12.7656 10.6469 13.0594 10.9406 C13.3531 11.2344 13.5 11.5875 13.5 12 C13.5 12.4125 13.3531 12.7656 13.0594 13.0593 C12.7656 13.3531 12.4125 13.5 12 13.5Z M17.7692 13.5 C17.3567 13.5 17.0036 13.3531 16.7099 13.0593 C16.4161 12.7656 16.2693 12.4125 16.2693 12 C16.2693 11.5875 16.4161 11.2344 16.7099 10.9406 C17.0036 10.6469 17.3567 10.5 17.7692 10.5 C18.1817 10.5 18.5348 10.6469 18.8286 10.9406 C19.1223 11.2344 19.2692 11.5875 19.2692 12 C19.2692 12.4125 19.1223 12.7656 18.8286 13.0593 C18.5348 13.3531 18.1817 13.5 17.7692 13.5Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 6.23079 13.5
        moveTo(x = 6.23079f, y = 13.5f)
        // C 5.8183 13.5 5.46518 13.3531 5.17143 13.0593
        curveTo(
          x1 = 5.8183f,
          y1 = 13.5f,
          x2 = 5.46518f,
          y2 = 13.3531f,
          x3 = 5.17143f,
          y3 = 13.0593f,
        )
        // C 4.8777 12.7656 4.73083 12.4125 4.73083 12
        curveTo(
          x1 = 4.8777f,
          y1 = 12.7656f,
          x2 = 4.73083f,
          y2 = 12.4125f,
          x3 = 4.73083f,
          y3 = 12.0f,
        )
        // C 4.73083 11.5875 4.8777 11.2344 5.17143 10.9406
        curveTo(
          x1 = 4.73083f,
          y1 = 11.5875f,
          x2 = 4.8777f,
          y2 = 11.2344f,
          x3 = 5.17143f,
          y3 = 10.9406f,
        )
        // C 5.46518 10.6469 5.8183 10.5 6.23079 10.5
        curveTo(
          x1 = 5.46518f,
          y1 = 10.6469f,
          x2 = 5.8183f,
          y2 = 10.5f,
          x3 = 6.23079f,
          y3 = 10.5f,
        )
        // C 6.64329 10.5 6.9964 10.6469 7.29014 10.9406
        curveTo(
          x1 = 6.64329f,
          y1 = 10.5f,
          x2 = 6.9964f,
          y2 = 10.6469f,
          x3 = 7.29014f,
          y3 = 10.9406f,
        )
        // C 7.58389 11.2344 7.73076 11.5875 7.73076 12
        curveTo(
          x1 = 7.58389f,
          y1 = 11.2344f,
          x2 = 7.73076f,
          y2 = 11.5875f,
          x3 = 7.73076f,
          y3 = 12.0f,
        )
        // C 7.73076 12.4125 7.58389 12.7656 7.29014 13.0593
        curveTo(
          x1 = 7.73076f,
          y1 = 12.4125f,
          x2 = 7.58389f,
          y2 = 12.7656f,
          x3 = 7.29014f,
          y3 = 13.0593f,
        )
        // C 6.9964 13.3531 6.64329 13.5 6.23079 13.5z
        curveTo(
          x1 = 6.9964f,
          y1 = 13.3531f,
          x2 = 6.64329f,
          y2 = 13.5f,
          x3 = 6.23079f,
          y3 = 13.5f,
        )
        close()
        // M 12 13.5
        moveTo(x = 12.0f, y = 13.5f)
        // C 11.5875 13.5 11.2344 13.3531 10.9407 13.0593
        curveTo(
          x1 = 11.5875f,
          y1 = 13.5f,
          x2 = 11.2344f,
          y2 = 13.3531f,
          x3 = 10.9407f,
          y3 = 13.0593f,
        )
        // C 10.6469 12.7656 10.5 12.4125 10.5 12
        curveTo(
          x1 = 10.6469f,
          y1 = 12.7656f,
          x2 = 10.5f,
          y2 = 12.4125f,
          x3 = 10.5f,
          y3 = 12.0f,
        )
        // C 10.5 11.5875 10.6469 11.2344 10.9407 10.9406
        curveTo(
          x1 = 10.5f,
          y1 = 11.5875f,
          x2 = 10.6469f,
          y2 = 11.2344f,
          x3 = 10.9407f,
          y3 = 10.9406f,
        )
        // C 11.2344 10.6469 11.5875 10.5 12 10.5
        curveTo(
          x1 = 11.2344f,
          y1 = 10.6469f,
          x2 = 11.5875f,
          y2 = 10.5f,
          x3 = 12.0f,
          y3 = 10.5f,
        )
        // C 12.4125 10.5 12.7656 10.6469 13.0594 10.9406
        curveTo(
          x1 = 12.4125f,
          y1 = 10.5f,
          x2 = 12.7656f,
          y2 = 10.6469f,
          x3 = 13.0594f,
          y3 = 10.9406f,
        )
        // C 13.3531 11.2344 13.5 11.5875 13.5 12
        curveTo(
          x1 = 13.3531f,
          y1 = 11.2344f,
          x2 = 13.5f,
          y2 = 11.5875f,
          x3 = 13.5f,
          y3 = 12.0f,
        )
        // C 13.5 12.4125 13.3531 12.7656 13.0594 13.0593
        curveTo(
          x1 = 13.5f,
          y1 = 12.4125f,
          x2 = 13.3531f,
          y2 = 12.7656f,
          x3 = 13.0594f,
          y3 = 13.0593f,
        )
        // C 12.7656 13.3531 12.4125 13.5 12 13.5z
        curveTo(
          x1 = 12.7656f,
          y1 = 13.3531f,
          x2 = 12.4125f,
          y2 = 13.5f,
          x3 = 12.0f,
          y3 = 13.5f,
        )
        close()
        // M 17.7692 13.5
        moveTo(x = 17.7692f, y = 13.5f)
        // C 17.3567 13.5 17.0036 13.3531 16.7099 13.0593
        curveTo(
          x1 = 17.3567f,
          y1 = 13.5f,
          x2 = 17.0036f,
          y2 = 13.3531f,
          x3 = 16.7099f,
          y3 = 13.0593f,
        )
        // C 16.4161 12.7656 16.2693 12.4125 16.2693 12
        curveTo(
          x1 = 16.4161f,
          y1 = 12.7656f,
          x2 = 16.2693f,
          y2 = 12.4125f,
          x3 = 16.2693f,
          y3 = 12.0f,
        )
        // C 16.2693 11.5875 16.4161 11.2344 16.7099 10.9406
        curveTo(
          x1 = 16.2693f,
          y1 = 11.5875f,
          x2 = 16.4161f,
          y2 = 11.2344f,
          x3 = 16.7099f,
          y3 = 10.9406f,
        )
        // C 17.0036 10.6469 17.3567 10.5 17.7692 10.5
        curveTo(
          x1 = 17.0036f,
          y1 = 10.6469f,
          x2 = 17.3567f,
          y2 = 10.5f,
          x3 = 17.7692f,
          y3 = 10.5f,
        )
        // C 18.1817 10.5 18.5348 10.6469 18.8286 10.9406
        curveTo(
          x1 = 18.1817f,
          y1 = 10.5f,
          x2 = 18.5348f,
          y2 = 10.6469f,
          x3 = 18.8286f,
          y3 = 10.9406f,
        )
        // C 19.1223 11.2344 19.2692 11.5875 19.2692 12
        curveTo(
          x1 = 19.1223f,
          y1 = 11.2344f,
          x2 = 19.2692f,
          y2 = 11.5875f,
          x3 = 19.2692f,
          y3 = 12.0f,
        )
        // C 19.2692 12.4125 19.1223 12.7656 18.8286 13.0593
        curveTo(
          x1 = 19.2692f,
          y1 = 12.4125f,
          x2 = 19.1223f,
          y2 = 12.7656f,
          x3 = 18.8286f,
          y3 = 13.0593f,
        )
        // C 18.5348 13.3531 18.1817 13.5 17.7692 13.5z
        curveTo(
          x1 = 18.5348f,
          y1 = 13.3531f,
          x2 = 18.1817f,
          y2 = 13.5f,
          x3 = 17.7692f,
          y3 = 13.5f,
        )
        close()
      }
    }.build().also { _dots = it }
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
        imageVector = HedvigIcons.Dots,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _dots: ImageVector? = null
