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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val HedvigIcons.InfoFilled: ImageVector
  get() {
    val current = _infoFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.InfoFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M21.5 12 C21.5 17.2467 17.2467 21.5 12 21.5 C6.75329 21.5 2.5 17.2467 2.5 12 C2.5 6.75329 6.75329 2.5 12 2.5 C17.2467 2.5 21.5 6.75329 21.5 12Z M12 10.25 C12.4142 10.25 12.75 10.5858 12.75 11 V16 C12.75 16.4142 12.4142 16.75 12 16.75 C11.5858 16.75 11.25 16.4142 11.25 16 V11 C11.25 10.5858 11.5858 10.25 12 10.25Z M12 9 C12.5523 9 13 8.55229 13 8 C13 7.44772 12.5523 7 12 7 C11.4477 7 11 7.44772 11 8 C11 8.55229 11.4477 9 12 9Z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 21.5 12
        moveTo(x = 21.5f, y = 12.0f)
        // C 21.5 17.2467 17.2467 21.5 12 21.5
        curveTo(
          x1 = 21.5f,
          y1 = 17.2467f,
          x2 = 17.2467f,
          y2 = 21.5f,
          x3 = 12.0f,
          y3 = 21.5f,
        )
        // C 6.75329 21.5 2.5 17.2467 2.5 12
        curveTo(
          x1 = 6.75329f,
          y1 = 21.5f,
          x2 = 2.5f,
          y2 = 17.2467f,
          x3 = 2.5f,
          y3 = 12.0f,
        )
        // C 2.5 6.75329 6.75329 2.5 12 2.5
        curveTo(
          x1 = 2.5f,
          y1 = 6.75329f,
          x2 = 6.75329f,
          y2 = 2.5f,
          x3 = 12.0f,
          y3 = 2.5f,
        )
        // C 17.2467 2.5 21.5 6.75329 21.5 12z
        curveTo(
          x1 = 17.2467f,
          y1 = 2.5f,
          x2 = 21.5f,
          y2 = 6.75329f,
          x3 = 21.5f,
          y3 = 12.0f,
        )
        close()
        // M 12 10.25
        moveTo(x = 12.0f, y = 10.25f)
        // C 12.4142 10.25 12.75 10.5858 12.75 11
        curveTo(
          x1 = 12.4142f,
          y1 = 10.25f,
          x2 = 12.75f,
          y2 = 10.5858f,
          x3 = 12.75f,
          y3 = 11.0f,
        )
        // V 16
        verticalLineTo(y = 16.0f)
        // C 12.75 16.4142 12.4142 16.75 12 16.75
        curveTo(
          x1 = 12.75f,
          y1 = 16.4142f,
          x2 = 12.4142f,
          y2 = 16.75f,
          x3 = 12.0f,
          y3 = 16.75f,
        )
        // C 11.5858 16.75 11.25 16.4142 11.25 16
        curveTo(
          x1 = 11.5858f,
          y1 = 16.75f,
          x2 = 11.25f,
          y2 = 16.4142f,
          x3 = 11.25f,
          y3 = 16.0f,
        )
        // V 11
        verticalLineTo(y = 11.0f)
        // C 11.25 10.5858 11.5858 10.25 12 10.25z
        curveTo(
          x1 = 11.25f,
          y1 = 10.5858f,
          x2 = 11.5858f,
          y2 = 10.25f,
          x3 = 12.0f,
          y3 = 10.25f,
        )
        close()
        // M 12 9
        moveTo(x = 12.0f, y = 9.0f)
        // C 12.5523 9 13 8.55229 13 8
        curveTo(
          x1 = 12.5523f,
          y1 = 9.0f,
          x2 = 13.0f,
          y2 = 8.55229f,
          x3 = 13.0f,
          y3 = 8.0f,
        )
        // C 13 7.44772 12.5523 7 12 7
        curveTo(
          x1 = 13.0f,
          y1 = 7.44772f,
          x2 = 12.5523f,
          y2 = 7.0f,
          x3 = 12.0f,
          y3 = 7.0f,
        )
        // C 11.4477 7 11 7.44772 11 8
        curveTo(
          x1 = 11.4477f,
          y1 = 7.0f,
          x2 = 11.0f,
          y2 = 7.44772f,
          x3 = 11.0f,
          y3 = 8.0f,
        )
        // C 11 8.55229 11.4477 9 12 9z
        curveTo(
          x1 = 11.0f,
          y1 = 8.55229f,
          x2 = 11.4477f,
          y2 = 9.0f,
          x3 = 12.0f,
          y3 = 9.0f,
        )
        close()
      }
    }.build().also { _infoFilled = it }
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
        imageVector = HedvigIcons.InfoFilled,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _infoFilled: ImageVector? = null
