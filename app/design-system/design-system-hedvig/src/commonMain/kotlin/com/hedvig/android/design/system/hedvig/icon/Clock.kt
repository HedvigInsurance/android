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

// Icons.Hedvig.Waiting in the old DS
@Suppress("UnusedReceiverParameter")
val HedvigIcons.Clock: ImageVector
  get() {
    val current = _clock
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Clock",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 20 C16.4183 20 20 16.4183 20 12 C20 7.58172 16.4183 4 12 4 C7.58172 4 4 7.58172 4 12 C4 16.4183 7.58172 20 12 20Z M12 21.5 C17.2467 21.5 21.5 17.2467 21.5 12 C21.5 6.75329 17.2467 2.5 12 2.5 C6.75329 2.5 2.5 6.75329 2.5 12 C2.5 17.2467 6.75329 21.5 12 21.5Z M12 6.25 C12.4142 6.25 12.75 6.58579 12.75 7 V11.6894 L15.5303 14.4697 C15.8232 14.7626 15.8232 15.2375 15.5303 15.5304 C15.2374 15.8233 14.7626 15.8233 14.4697 15.5304 L11.4697 12.5304 L11.4697 12.5304 C11.334 12.3946 11.25 12.2071 11.25 12 V7 C11.25 6.58579 11.5858 6.25 12 6.25Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 12 20
        moveTo(x = 12.0f, y = 20.0f)
        // C 16.4183 20 20 16.4183 20 12
        curveTo(
          x1 = 16.4183f,
          y1 = 20.0f,
          x2 = 20.0f,
          y2 = 16.4183f,
          x3 = 20.0f,
          y3 = 12.0f,
        )
        // C 20 7.58172 16.4183 4 12 4
        curveTo(
          x1 = 20.0f,
          y1 = 7.58172f,
          x2 = 16.4183f,
          y2 = 4.0f,
          x3 = 12.0f,
          y3 = 4.0f,
        )
        // C 7.58172 4 4 7.58172 4 12
        curveTo(
          x1 = 7.58172f,
          y1 = 4.0f,
          x2 = 4.0f,
          y2 = 7.58172f,
          x3 = 4.0f,
          y3 = 12.0f,
        )
        // C 4 16.4183 7.58172 20 12 20z
        curveTo(
          x1 = 4.0f,
          y1 = 16.4183f,
          x2 = 7.58172f,
          y2 = 20.0f,
          x3 = 12.0f,
          y3 = 20.0f,
        )
        close()
        // M 12 21.5
        moveTo(x = 12.0f, y = 21.5f)
        // C 17.2467 21.5 21.5 17.2467 21.5 12
        curveTo(
          x1 = 17.2467f,
          y1 = 21.5f,
          x2 = 21.5f,
          y2 = 17.2467f,
          x3 = 21.5f,
          y3 = 12.0f,
        )
        // C 21.5 6.75329 17.2467 2.5 12 2.5
        curveTo(
          x1 = 21.5f,
          y1 = 6.75329f,
          x2 = 17.2467f,
          y2 = 2.5f,
          x3 = 12.0f,
          y3 = 2.5f,
        )
        // C 6.75329 2.5 2.5 6.75329 2.5 12
        curveTo(
          x1 = 6.75329f,
          y1 = 2.5f,
          x2 = 2.5f,
          y2 = 6.75329f,
          x3 = 2.5f,
          y3 = 12.0f,
        )
        // C 2.5 17.2467 6.75329 21.5 12 21.5z
        curveTo(
          x1 = 2.5f,
          y1 = 17.2467f,
          x2 = 6.75329f,
          y2 = 21.5f,
          x3 = 12.0f,
          y3 = 21.5f,
        )
        close()
        // M 12 6.25
        moveTo(x = 12.0f, y = 6.25f)
        // C 12.4142 6.25 12.75 6.58579 12.75 7
        curveTo(
          x1 = 12.4142f,
          y1 = 6.25f,
          x2 = 12.75f,
          y2 = 6.58579f,
          x3 = 12.75f,
          y3 = 7.0f,
        )
        // V 11.6894
        verticalLineTo(y = 11.6894f)
        // L 15.5303 14.4697
        lineTo(x = 15.5303f, y = 14.4697f)
        // C 15.8232 14.7626 15.8232 15.2375 15.5303 15.5304
        curveTo(
          x1 = 15.8232f,
          y1 = 14.7626f,
          x2 = 15.8232f,
          y2 = 15.2375f,
          x3 = 15.5303f,
          y3 = 15.5304f,
        )
        // C 15.2374 15.8233 14.7626 15.8233 14.4697 15.5304
        curveTo(
          x1 = 15.2374f,
          y1 = 15.8233f,
          x2 = 14.7626f,
          y2 = 15.8233f,
          x3 = 14.4697f,
          y3 = 15.5304f,
        )
        // L 11.4697 12.5304
        lineTo(x = 11.4697f, y = 12.5304f)
        // L 11.4697 12.5304
        lineTo(x = 11.4697f, y = 12.5304f)
        // C 11.334 12.3946 11.25 12.2071 11.25 12
        curveTo(
          x1 = 11.334f,
          y1 = 12.3946f,
          x2 = 11.25f,
          y2 = 12.2071f,
          x3 = 11.25f,
          y3 = 12.0f,
        )
        // V 7
        verticalLineTo(y = 7.0f)
        // C 11.25 6.58579 11.5858 6.25 12 6.25z
        curveTo(
          x1 = 11.25f,
          y1 = 6.58579f,
          x2 = 11.5858f,
          y2 = 6.25f,
          x3 = 12.0f,
          y3 = 6.25f,
        )
        close()
      }
    }.build().also { _clock = it }
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
        imageVector = HedvigIcons.Clock,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _clock: ImageVector? = null
