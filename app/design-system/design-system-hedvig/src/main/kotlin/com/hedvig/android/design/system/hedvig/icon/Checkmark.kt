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
val HedvigIcons.Checkmark: ImageVector
  get() {
    val current = _checkmark
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Checkmark",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M19.5303 6.96967 C19.8232 7.26256 19.8232 7.73744 19.5303 8.03033 L11.091 16.4697 C10.2123 17.3483 8.78769 17.3484 7.90901 16.4697 L4.46967 13.0303 C4.17678 12.7374 4.17678 12.2626 4.46967 11.9697 C4.76256 11.6768 5.23744 11.6768 5.53033 11.9697 L8.96967 15.409 C9.26256 15.7019 9.73744 15.7019 10.0303 15.409 L18.4697 6.96967 C18.7626 6.67678 19.2374 6.67678 19.5303 6.96967Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 19.5303 6.96967
        moveTo(x = 19.5303f, y = 6.96967f)
        // C 19.8232 7.26256 19.8232 7.73744 19.5303 8.03033
        curveTo(
          x1 = 19.8232f,
          y1 = 7.26256f,
          x2 = 19.8232f,
          y2 = 7.73744f,
          x3 = 19.5303f,
          y3 = 8.03033f,
        )
        // L 11.091 16.4697
        lineTo(x = 11.091f, y = 16.4697f)
        // C 10.2123 17.3483 8.78769 17.3484 7.90901 16.4697
        curveTo(
          x1 = 10.2123f,
          y1 = 17.3483f,
          x2 = 8.78769f,
          y2 = 17.3484f,
          x3 = 7.90901f,
          y3 = 16.4697f,
        )
        // L 4.46967 13.0303
        lineTo(x = 4.46967f, y = 13.0303f)
        // C 4.17678 12.7374 4.17678 12.2626 4.46967 11.9697
        curveTo(
          x1 = 4.17678f,
          y1 = 12.7374f,
          x2 = 4.17678f,
          y2 = 12.2626f,
          x3 = 4.46967f,
          y3 = 11.9697f,
        )
        // C 4.76256 11.6768 5.23744 11.6768 5.53033 11.9697
        curveTo(
          x1 = 4.76256f,
          y1 = 11.6768f,
          x2 = 5.23744f,
          y2 = 11.6768f,
          x3 = 5.53033f,
          y3 = 11.9697f,
        )
        // L 8.96967 15.409
        lineTo(x = 8.96967f, y = 15.409f)
        // C 9.26256 15.7019 9.73744 15.7019 10.0303 15.409
        curveTo(
          x1 = 9.26256f,
          y1 = 15.7019f,
          x2 = 9.73744f,
          y2 = 15.7019f,
          x3 = 10.0303f,
          y3 = 15.409f,
        )
        // L 18.4697 6.96967
        lineTo(x = 18.4697f, y = 6.96967f)
        // C 18.7626 6.67678 19.2374 6.67678 19.5303 6.96967z
        curveTo(
          x1 = 18.7626f,
          y1 = 6.67678f,
          x2 = 19.2374f,
          y2 = 6.67678f,
          x3 = 19.5303f,
          y3 = 6.96967f,
        )
        close()
      }
    }.build().also { _checkmark = it }
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
        imageVector = HedvigIcons.Checkmark,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _checkmark: ImageVector? = null
