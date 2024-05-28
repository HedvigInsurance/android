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
val HedvigIcons.Document: ImageVector
  get() {
    val current = _document
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Document",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M7.5 3.75 C6.80964 3.75 6.25 4.30964 6.25 5 V19 C6.25 19.6904 6.80964 20.25 7.5 20.25 H16.5 C17.1904 20.25 17.75 19.6904 17.75 19 V5 C17.75 4.30964 17.1904 3.75 16.5 3.75 H7.5Z M4.75 5 C4.75 3.48122 5.98122 2.25 7.5 2.25 H16.5 C18.0188 2.25 19.25 3.48122 19.25 5 V19 C19.25 20.5188 18.0188 21.75 16.5 21.75 H7.5 C5.98122 21.75 4.75 20.5188 4.75 19 V5Z M15.75 10.5 C15.75 10.0858 15.4142 9.75 15 9.75 H9 C8.58579 9.75 8.25 10.0858 8.25 10.5 C8.25 10.9142 8.58579 11.25 9 11.25 H15 C15.4142 11.25 15.75 10.9142 15.75 10.5Z M15 7.25 C15.4142 7.25 15.75 7.58579 15.75 8 C15.75 8.41421 15.4142 8.75 15 8.75 H9 C8.58579 8.75 8.25 8.41421 8.25 8 C8.25 7.58579 8.58579 7.25 9 7.25 H15Z M15.75 13 C15.75 12.5858 15.4142 12.25 15 12.25 H9 C8.58579 12.25 8.25 12.5858 8.25 13 C8.25 13.4142 8.58579 13.75 9 13.75 H15 C15.4142 13.75 15.75 13.4142 15.75 13Z M11.5 14.75 C11.9142 14.75 12.25 15.0858 12.25 15.5 C12.25 15.9142 11.9142 16.25 11.5 16.25 H9 C8.58579 16.25 8.25 15.9142 8.25 15.5 C8.25 15.0858 8.58579 14.75 9 14.75 H11.5Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 7.5 3.75
        moveTo(x = 7.5f, y = 3.75f)
        // C 6.80964 3.75 6.25 4.30964 6.25 5
        curveTo(
          x1 = 6.80964f,
          y1 = 3.75f,
          x2 = 6.25f,
          y2 = 4.30964f,
          x3 = 6.25f,
          y3 = 5.0f,
        )
        // V 19
        verticalLineTo(y = 19.0f)
        // C 6.25 19.6904 6.80964 20.25 7.5 20.25
        curveTo(
          x1 = 6.25f,
          y1 = 19.6904f,
          x2 = 6.80964f,
          y2 = 20.25f,
          x3 = 7.5f,
          y3 = 20.25f,
        )
        // H 16.5
        horizontalLineTo(x = 16.5f)
        // C 17.1904 20.25 17.75 19.6904 17.75 19
        curveTo(
          x1 = 17.1904f,
          y1 = 20.25f,
          x2 = 17.75f,
          y2 = 19.6904f,
          x3 = 17.75f,
          y3 = 19.0f,
        )
        // V 5
        verticalLineTo(y = 5.0f)
        // C 17.75 4.30964 17.1904 3.75 16.5 3.75
        curveTo(
          x1 = 17.75f,
          y1 = 4.30964f,
          x2 = 17.1904f,
          y2 = 3.75f,
          x3 = 16.5f,
          y3 = 3.75f,
        )
        // H 7.5z
        horizontalLineTo(x = 7.5f)
        close()
        // M 4.75 5
        moveTo(x = 4.75f, y = 5.0f)
        // C 4.75 3.48122 5.98122 2.25 7.5 2.25
        curveTo(
          x1 = 4.75f,
          y1 = 3.48122f,
          x2 = 5.98122f,
          y2 = 2.25f,
          x3 = 7.5f,
          y3 = 2.25f,
        )
        // H 16.5
        horizontalLineTo(x = 16.5f)
        // C 18.0188 2.25 19.25 3.48122 19.25 5
        curveTo(
          x1 = 18.0188f,
          y1 = 2.25f,
          x2 = 19.25f,
          y2 = 3.48122f,
          x3 = 19.25f,
          y3 = 5.0f,
        )
        // V 19
        verticalLineTo(y = 19.0f)
        // C 19.25 20.5188 18.0188 21.75 16.5 21.75
        curveTo(
          x1 = 19.25f,
          y1 = 20.5188f,
          x2 = 18.0188f,
          y2 = 21.75f,
          x3 = 16.5f,
          y3 = 21.75f,
        )
        // H 7.5
        horizontalLineTo(x = 7.5f)
        // C 5.98122 21.75 4.75 20.5188 4.75 19
        curveTo(
          x1 = 5.98122f,
          y1 = 21.75f,
          x2 = 4.75f,
          y2 = 20.5188f,
          x3 = 4.75f,
          y3 = 19.0f,
        )
        // V 5z
        verticalLineTo(y = 5.0f)
        close()
        // M 15.75 10.5
        moveTo(x = 15.75f, y = 10.5f)
        // C 15.75 10.0858 15.4142 9.75 15 9.75
        curveTo(
          x1 = 15.75f,
          y1 = 10.0858f,
          x2 = 15.4142f,
          y2 = 9.75f,
          x3 = 15.0f,
          y3 = 9.75f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // C 8.58579 9.75 8.25 10.0858 8.25 10.5
        curveTo(
          x1 = 8.58579f,
          y1 = 9.75f,
          x2 = 8.25f,
          y2 = 10.0858f,
          x3 = 8.25f,
          y3 = 10.5f,
        )
        // C 8.25 10.9142 8.58579 11.25 9 11.25
        curveTo(
          x1 = 8.25f,
          y1 = 10.9142f,
          x2 = 8.58579f,
          y2 = 11.25f,
          x3 = 9.0f,
          y3 = 11.25f,
        )
        // H 15
        horizontalLineTo(x = 15.0f)
        // C 15.4142 11.25 15.75 10.9142 15.75 10.5z
        curveTo(
          x1 = 15.4142f,
          y1 = 11.25f,
          x2 = 15.75f,
          y2 = 10.9142f,
          x3 = 15.75f,
          y3 = 10.5f,
        )
        close()
        // M 15 7.25
        moveTo(x = 15.0f, y = 7.25f)
        // C 15.4142 7.25 15.75 7.58579 15.75 8
        curveTo(
          x1 = 15.4142f,
          y1 = 7.25f,
          x2 = 15.75f,
          y2 = 7.58579f,
          x3 = 15.75f,
          y3 = 8.0f,
        )
        // C 15.75 8.41421 15.4142 8.75 15 8.75
        curveTo(
          x1 = 15.75f,
          y1 = 8.41421f,
          x2 = 15.4142f,
          y2 = 8.75f,
          x3 = 15.0f,
          y3 = 8.75f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // C 8.58579 8.75 8.25 8.41421 8.25 8
        curveTo(
          x1 = 8.58579f,
          y1 = 8.75f,
          x2 = 8.25f,
          y2 = 8.41421f,
          x3 = 8.25f,
          y3 = 8.0f,
        )
        // C 8.25 7.58579 8.58579 7.25 9 7.25
        curveTo(
          x1 = 8.25f,
          y1 = 7.58579f,
          x2 = 8.58579f,
          y2 = 7.25f,
          x3 = 9.0f,
          y3 = 7.25f,
        )
        // H 15z
        horizontalLineTo(x = 15.0f)
        close()
        // M 15.75 13
        moveTo(x = 15.75f, y = 13.0f)
        // C 15.75 12.5858 15.4142 12.25 15 12.25
        curveTo(
          x1 = 15.75f,
          y1 = 12.5858f,
          x2 = 15.4142f,
          y2 = 12.25f,
          x3 = 15.0f,
          y3 = 12.25f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // C 8.58579 12.25 8.25 12.5858 8.25 13
        curveTo(
          x1 = 8.58579f,
          y1 = 12.25f,
          x2 = 8.25f,
          y2 = 12.5858f,
          x3 = 8.25f,
          y3 = 13.0f,
        )
        // C 8.25 13.4142 8.58579 13.75 9 13.75
        curveTo(
          x1 = 8.25f,
          y1 = 13.4142f,
          x2 = 8.58579f,
          y2 = 13.75f,
          x3 = 9.0f,
          y3 = 13.75f,
        )
        // H 15
        horizontalLineTo(x = 15.0f)
        // C 15.4142 13.75 15.75 13.4142 15.75 13z
        curveTo(
          x1 = 15.4142f,
          y1 = 13.75f,
          x2 = 15.75f,
          y2 = 13.4142f,
          x3 = 15.75f,
          y3 = 13.0f,
        )
        close()
        // M 11.5 14.75
        moveTo(x = 11.5f, y = 14.75f)
        // C 11.9142 14.75 12.25 15.0858 12.25 15.5
        curveTo(
          x1 = 11.9142f,
          y1 = 14.75f,
          x2 = 12.25f,
          y2 = 15.0858f,
          x3 = 12.25f,
          y3 = 15.5f,
        )
        // C 12.25 15.9142 11.9142 16.25 11.5 16.25
        curveTo(
          x1 = 12.25f,
          y1 = 15.9142f,
          x2 = 11.9142f,
          y2 = 16.25f,
          x3 = 11.5f,
          y3 = 16.25f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // C 8.58579 16.25 8.25 15.9142 8.25 15.5
        curveTo(
          x1 = 8.58579f,
          y1 = 16.25f,
          x2 = 8.25f,
          y2 = 15.9142f,
          x3 = 8.25f,
          y3 = 15.5f,
        )
        // C 8.25 15.0858 8.58579 14.75 9 14.75
        curveTo(
          x1 = 8.25f,
          y1 = 15.0858f,
          x2 = 8.58579f,
          y2 = 14.75f,
          x3 = 9.0f,
          y3 = 14.75f,
        )
        // H 11.5z
        horizontalLineTo(x = 11.5f)
        close()
      }
    }.build().also { _document = it }
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
        imageVector = HedvigIcons.Document,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _document: ImageVector? = null
