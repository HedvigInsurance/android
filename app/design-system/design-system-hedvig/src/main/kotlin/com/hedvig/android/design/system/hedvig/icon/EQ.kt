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
val HedvigIcons.EQ: ImageVector
  get() {
    val current = _eQ
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.EQ",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M10.5 7 C10.5 6.44771 10.9477 6 11.5 6 H12.5 C13.0523 6 13.5 6.44772 13.5 7 V17 C13.5 17.5523 13.0523 18 12.5 18 H11.5 C10.9477 18 10.5 17.5523 10.5 17 V7Z M5.5 13 C5.5 12.4477 5.94772 12 6.5 12 H7.5 C8.05229 12 8.5 12.4477 8.5 13 V17 C8.5 17.5523 8.05228 18 7.5 18 H6.5 C5.94772 18 5.5 17.5523 5.5 17 V13Z M16.5 10 C15.9477 10 15.5 10.4477 15.5 11 V17 C15.5 17.5523 15.9477 18 16.5 18 H17.5 C18.0523 18 18.5 17.5523 18.5 17 V11 C18.5 10.4477 18.0523 10 17.5 10 H16.5Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 10.5 7
        moveTo(x = 10.5f, y = 7.0f)
        // C 10.5 6.44771 10.9477 6 11.5 6
        curveTo(
          x1 = 10.5f,
          y1 = 6.44771f,
          x2 = 10.9477f,
          y2 = 6.0f,
          x3 = 11.5f,
          y3 = 6.0f,
        )
        // H 12.5
        horizontalLineTo(x = 12.5f)
        // C 13.0523 6 13.5 6.44772 13.5 7
        curveTo(
          x1 = 13.0523f,
          y1 = 6.0f,
          x2 = 13.5f,
          y2 = 6.44772f,
          x3 = 13.5f,
          y3 = 7.0f,
        )
        // V 17
        verticalLineTo(y = 17.0f)
        // C 13.5 17.5523 13.0523 18 12.5 18
        curveTo(
          x1 = 13.5f,
          y1 = 17.5523f,
          x2 = 13.0523f,
          y2 = 18.0f,
          x3 = 12.5f,
          y3 = 18.0f,
        )
        // H 11.5
        horizontalLineTo(x = 11.5f)
        // C 10.9477 18 10.5 17.5523 10.5 17
        curveTo(
          x1 = 10.9477f,
          y1 = 18.0f,
          x2 = 10.5f,
          y2 = 17.5523f,
          x3 = 10.5f,
          y3 = 17.0f,
        )
        // V 7z
        verticalLineTo(y = 7.0f)
        close()
        // M 5.5 13
        moveTo(x = 5.5f, y = 13.0f)
        // C 5.5 12.4477 5.94772 12 6.5 12
        curveTo(
          x1 = 5.5f,
          y1 = 12.4477f,
          x2 = 5.94772f,
          y2 = 12.0f,
          x3 = 6.5f,
          y3 = 12.0f,
        )
        // H 7.5
        horizontalLineTo(x = 7.5f)
        // C 8.05229 12 8.5 12.4477 8.5 13
        curveTo(
          x1 = 8.05229f,
          y1 = 12.0f,
          x2 = 8.5f,
          y2 = 12.4477f,
          x3 = 8.5f,
          y3 = 13.0f,
        )
        // V 17
        verticalLineTo(y = 17.0f)
        // C 8.5 17.5523 8.05228 18 7.5 18
        curveTo(
          x1 = 8.5f,
          y1 = 17.5523f,
          x2 = 8.05228f,
          y2 = 18.0f,
          x3 = 7.5f,
          y3 = 18.0f,
        )
        // H 6.5
        horizontalLineTo(x = 6.5f)
        // C 5.94772 18 5.5 17.5523 5.5 17
        curveTo(
          x1 = 5.94772f,
          y1 = 18.0f,
          x2 = 5.5f,
          y2 = 17.5523f,
          x3 = 5.5f,
          y3 = 17.0f,
        )
        // V 13z
        verticalLineTo(y = 13.0f)
        close()
        // M 16.5 10
        moveTo(x = 16.5f, y = 10.0f)
        // C 15.9477 10 15.5 10.4477 15.5 11
        curveTo(
          x1 = 15.9477f,
          y1 = 10.0f,
          x2 = 15.5f,
          y2 = 10.4477f,
          x3 = 15.5f,
          y3 = 11.0f,
        )
        // V 17
        verticalLineTo(y = 17.0f)
        // C 15.5 17.5523 15.9477 18 16.5 18
        curveTo(
          x1 = 15.5f,
          y1 = 17.5523f,
          x2 = 15.9477f,
          y2 = 18.0f,
          x3 = 16.5f,
          y3 = 18.0f,
        )
        // H 17.5
        horizontalLineTo(x = 17.5f)
        // C 18.0523 18 18.5 17.5523 18.5 17
        curveTo(
          x1 = 18.0523f,
          y1 = 18.0f,
          x2 = 18.5f,
          y2 = 17.5523f,
          x3 = 18.5f,
          y3 = 17.0f,
        )
        // V 11
        verticalLineTo(y = 11.0f)
        // C 18.5 10.4477 18.0523 10 17.5 10
        curveTo(
          x1 = 18.5f,
          y1 = 10.4477f,
          x2 = 18.0523f,
          y2 = 10.0f,
          x3 = 17.5f,
          y3 = 10.0f,
        )
        // H 16.5z
        horizontalLineTo(x = 16.5f)
        close()
      }
    }.build().also { _eQ = it }
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
        imageVector = HedvigIcons.EQ,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _eQ: ImageVector? = null
