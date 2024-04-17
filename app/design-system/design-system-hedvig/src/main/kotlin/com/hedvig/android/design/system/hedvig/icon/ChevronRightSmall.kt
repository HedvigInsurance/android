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
val HedvigIcons.ChevronRightSmall: ImageVector
  get() {
    val current = _chevronRightSmall
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ChevronRightSmall",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M9.46967 17.5303 C9.17678 17.2374 9.17678 16.7626 9.46967 16.4697 L13.7626 12.1768 C13.8602 12.0791 13.8602 11.9209 13.7626 11.8232 L9.46967 7.53033 C9.17678 7.23744 9.17678 6.76256 9.46967 6.46967 C9.76256 6.17678 10.2374 6.17678 10.5303 6.46967 L14.8232 10.7626 C15.5066 11.446 15.5066 12.554 14.8232 13.2374 L10.5303 17.5303 C10.2374 17.8232 9.76256 17.8232 9.46967 17.5303Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 9.46967 17.5303
        moveTo(x = 9.46967f, y = 17.5303f)
        // C 9.17678 17.2374 9.17678 16.7626 9.46967 16.4697
        curveTo(
          x1 = 9.17678f,
          y1 = 17.2374f,
          x2 = 9.17678f,
          y2 = 16.7626f,
          x3 = 9.46967f,
          y3 = 16.4697f,
        )
        // L 13.7626 12.1768
        lineTo(x = 13.7626f, y = 12.1768f)
        // C 13.8602 12.0791 13.8602 11.9209 13.7626 11.8232
        curveTo(
          x1 = 13.8602f,
          y1 = 12.0791f,
          x2 = 13.8602f,
          y2 = 11.9209f,
          x3 = 13.7626f,
          y3 = 11.8232f,
        )
        // L 9.46967 7.53033
        lineTo(x = 9.46967f, y = 7.53033f)
        // C 9.17678 7.23744 9.17678 6.76256 9.46967 6.46967
        curveTo(
          x1 = 9.17678f,
          y1 = 7.23744f,
          x2 = 9.17678f,
          y2 = 6.76256f,
          x3 = 9.46967f,
          y3 = 6.46967f,
        )
        // C 9.76256 6.17678 10.2374 6.17678 10.5303 6.46967
        curveTo(
          x1 = 9.76256f,
          y1 = 6.17678f,
          x2 = 10.2374f,
          y2 = 6.17678f,
          x3 = 10.5303f,
          y3 = 6.46967f,
        )
        // L 14.8232 10.7626
        lineTo(x = 14.8232f, y = 10.7626f)
        // C 15.5066 11.446 15.5066 12.554 14.8232 13.2374
        curveTo(
          x1 = 15.5066f,
          y1 = 11.446f,
          x2 = 15.5066f,
          y2 = 12.554f,
          x3 = 14.8232f,
          y3 = 13.2374f,
        )
        // L 10.5303 17.5303
        lineTo(x = 10.5303f, y = 17.5303f)
        // C 10.2374 17.8232 9.76256 17.8232 9.46967 17.5303z
        curveTo(
          x1 = 10.2374f,
          y1 = 17.8232f,
          x2 = 9.76256f,
          y2 = 17.8232f,
          x3 = 9.46967f,
          y3 = 17.5303f,
        )
        close()
      }
    }.build().also { _chevronRightSmall = it }
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
        imageVector = HedvigIcons.ChevronRightSmall,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _chevronRightSmall: ImageVector? = null
