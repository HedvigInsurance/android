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
val HedvigIcons.AttentionOutline: ImageVector
  get() {
    val current = _attentionOutline
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.AttentionOutline",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 20 C16.4183 20 20 16.4183 20 12 C20 7.58172 16.4183 4 12 4 C7.58172 4 4 7.58172 4 12 C4 16.4183 7.58172 20 12 20Z M12 21.5 C17.2467 21.5 21.5 17.2467 21.5 12 C21.5 6.75329 17.2467 2.5 12 2.5 C6.75329 2.5 2.5 6.75329 2.5 12 C2.5 17.2467 6.75329 21.5 12 21.5Z M12 7.25 C12.4142 7.25 12.75 7.58579 12.75 8 V13 C12.75 13.4142 12.4142 13.75 12 13.75 C11.5858 13.75 11.25 13.4142 11.25 13 V8 C11.25 7.58579 11.5858 7.25 12 7.25Z M12 17 C12.5523 17 13 16.5523 13 16 C13 15.4477 12.5523 15 12 15 C11.4477 15 11 15.4477 11 16 C11 16.5523 11.4477 17 12 17Z
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
        // M 12 7.25
        moveTo(x = 12.0f, y = 7.25f)
        // C 12.4142 7.25 12.75 7.58579 12.75 8
        curveTo(
          x1 = 12.4142f,
          y1 = 7.25f,
          x2 = 12.75f,
          y2 = 7.58579f,
          x3 = 12.75f,
          y3 = 8.0f,
        )
        // V 13
        verticalLineTo(y = 13.0f)
        // C 12.75 13.4142 12.4142 13.75 12 13.75
        curveTo(
          x1 = 12.75f,
          y1 = 13.4142f,
          x2 = 12.4142f,
          y2 = 13.75f,
          x3 = 12.0f,
          y3 = 13.75f,
        )
        // C 11.5858 13.75 11.25 13.4142 11.25 13
        curveTo(
          x1 = 11.5858f,
          y1 = 13.75f,
          x2 = 11.25f,
          y2 = 13.4142f,
          x3 = 11.25f,
          y3 = 13.0f,
        )
        // V 8
        verticalLineTo(y = 8.0f)
        // C 11.25 7.58579 11.5858 7.25 12 7.25z
        curveTo(
          x1 = 11.25f,
          y1 = 7.58579f,
          x2 = 11.5858f,
          y2 = 7.25f,
          x3 = 12.0f,
          y3 = 7.25f,
        )
        close()
        // M 12 17
        moveTo(x = 12.0f, y = 17.0f)
        // C 12.5523 17 13 16.5523 13 16
        curveTo(
          x1 = 12.5523f,
          y1 = 17.0f,
          x2 = 13.0f,
          y2 = 16.5523f,
          x3 = 13.0f,
          y3 = 16.0f,
        )
        // C 13 15.4477 12.5523 15 12 15
        curveTo(
          x1 = 13.0f,
          y1 = 15.4477f,
          x2 = 12.5523f,
          y2 = 15.0f,
          x3 = 12.0f,
          y3 = 15.0f,
        )
        // C 11.4477 15 11 15.4477 11 16
        curveTo(
          x1 = 11.4477f,
          y1 = 15.0f,
          x2 = 11.0f,
          y2 = 15.4477f,
          x3 = 11.0f,
          y3 = 16.0f,
        )
        // C 11 16.5523 11.4477 17 12 17z
        curveTo(
          x1 = 11.0f,
          y1 = 16.5523f,
          x2 = 11.4477f,
          y2 = 17.0f,
          x3 = 12.0f,
          y3 = 17.0f,
        )
        close()
      }
    }.build().also { _attentionOutline = it }
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
        imageVector = HedvigIcons.AttentionOutline,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _attentionOutline: ImageVector? = null
