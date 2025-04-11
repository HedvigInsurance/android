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
val HedvigIcons.Mic: ImageVector
  get() {
    val current = _mic
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Mic",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 3 C13.3807 3 14.5 4.11929 14.5 5.5 V11.5 C14.5 12.8807 13.3807 14 12 14 C10.6193 14 9.5 12.8807 9.5 11.5 V5.5 C9.5 4.11929 10.6193 3 12 3Z M6 10.75 C6.41421 10.75 6.75 11.0858 6.75 11.5 C6.75 14.3995 9.10051 16.75 12 16.75 C14.8995 16.75 17.25 14.3995 17.25 11.5 C17.25 11.0858 17.5858 10.75 18 10.75 C18.4142 10.75 18.75 11.0858 18.75 11.5 C18.75 14.9744 16.125 17.8357 12.75 18.2088 V20.5 C12.75 20.9142 12.4142 21.25 12 21.25 C11.5858 21.25 11.25 20.9142 11.25 20.5 V18.2088 C7.87504 17.8357 5.25 14.9744 5.25 11.5 C5.25 11.0858 5.58579 10.75 6 10.75Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 12 3
        moveTo(x = 12.0f, y = 3.0f)
        // C 13.3807 3 14.5 4.11929 14.5 5.5
        curveTo(
          x1 = 13.3807f,
          y1 = 3.0f,
          x2 = 14.5f,
          y2 = 4.11929f,
          x3 = 14.5f,
          y3 = 5.5f,
        )
        // V 11.5
        verticalLineTo(y = 11.5f)
        // C 14.5 12.8807 13.3807 14 12 14
        curveTo(
          x1 = 14.5f,
          y1 = 12.8807f,
          x2 = 13.3807f,
          y2 = 14.0f,
          x3 = 12.0f,
          y3 = 14.0f,
        )
        // C 10.6193 14 9.5 12.8807 9.5 11.5
        curveTo(
          x1 = 10.6193f,
          y1 = 14.0f,
          x2 = 9.5f,
          y2 = 12.8807f,
          x3 = 9.5f,
          y3 = 11.5f,
        )
        // V 5.5
        verticalLineTo(y = 5.5f)
        // C 9.5 4.11929 10.6193 3 12 3z
        curveTo(
          x1 = 9.5f,
          y1 = 4.11929f,
          x2 = 10.6193f,
          y2 = 3.0f,
          x3 = 12.0f,
          y3 = 3.0f,
        )
        close()
        // M 6 10.75
        moveTo(x = 6.0f, y = 10.75f)
        // C 6.41421 10.75 6.75 11.0858 6.75 11.5
        curveTo(
          x1 = 6.41421f,
          y1 = 10.75f,
          x2 = 6.75f,
          y2 = 11.0858f,
          x3 = 6.75f,
          y3 = 11.5f,
        )
        // C 6.75 14.3995 9.10051 16.75 12 16.75
        curveTo(
          x1 = 6.75f,
          y1 = 14.3995f,
          x2 = 9.10051f,
          y2 = 16.75f,
          x3 = 12.0f,
          y3 = 16.75f,
        )
        // C 14.8995 16.75 17.25 14.3995 17.25 11.5
        curveTo(
          x1 = 14.8995f,
          y1 = 16.75f,
          x2 = 17.25f,
          y2 = 14.3995f,
          x3 = 17.25f,
          y3 = 11.5f,
        )
        // C 17.25 11.0858 17.5858 10.75 18 10.75
        curveTo(
          x1 = 17.25f,
          y1 = 11.0858f,
          x2 = 17.5858f,
          y2 = 10.75f,
          x3 = 18.0f,
          y3 = 10.75f,
        )
        // C 18.4142 10.75 18.75 11.0858 18.75 11.5
        curveTo(
          x1 = 18.4142f,
          y1 = 10.75f,
          x2 = 18.75f,
          y2 = 11.0858f,
          x3 = 18.75f,
          y3 = 11.5f,
        )
        // C 18.75 14.9744 16.125 17.8357 12.75 18.2088
        curveTo(
          x1 = 18.75f,
          y1 = 14.9744f,
          x2 = 16.125f,
          y2 = 17.8357f,
          x3 = 12.75f,
          y3 = 18.2088f,
        )
        // V 20.5
        verticalLineTo(y = 20.5f)
        // C 12.75 20.9142 12.4142 21.25 12 21.25
        curveTo(
          x1 = 12.75f,
          y1 = 20.9142f,
          x2 = 12.4142f,
          y2 = 21.25f,
          x3 = 12.0f,
          y3 = 21.25f,
        )
        // C 11.5858 21.25 11.25 20.9142 11.25 20.5
        curveTo(
          x1 = 11.5858f,
          y1 = 21.25f,
          x2 = 11.25f,
          y2 = 20.9142f,
          x3 = 11.25f,
          y3 = 20.5f,
        )
        // V 18.2088
        verticalLineTo(y = 18.2088f)
        // C 7.87504 17.8357 5.25 14.9744 5.25 11.5
        curveTo(
          x1 = 7.87504f,
          y1 = 17.8357f,
          x2 = 5.25f,
          y2 = 14.9744f,
          x3 = 5.25f,
          y3 = 11.5f,
        )
        // C 5.25 11.0858 5.58579 10.75 6 10.75z
        curveTo(
          x1 = 5.25f,
          y1 = 11.0858f,
          x2 = 5.58579f,
          y2 = 10.75f,
          x3 = 6.0f,
          y3 = 10.75f,
        )
        close()
      }
    }.build().also { _mic = it }
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
        imageVector = HedvigIcons.Mic,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _mic: ImageVector? = null
