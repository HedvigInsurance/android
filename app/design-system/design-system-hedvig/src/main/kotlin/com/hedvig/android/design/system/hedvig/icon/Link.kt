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
val HedvigIcons.Link: ImageVector
  get() {
    val current = _link
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Link",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M7 8.75 C5.20507 8.75 3.75 10.2051 3.75 12 C3.75 13.7949 5.20507 15.25 7 15.25 H10 C10.4142 15.25 10.75 15.5858 10.75 16 C10.75 16.4142 10.4142 16.75 10 16.75 H7 C4.37665 16.75 2.25 14.6234 2.25 12 C2.25 9.37665 4.37665 7.25 7 7.25 H10 C10.4142 7.25 10.75 7.58579 10.75 8 C10.75 8.41421 10.4142 8.75 10 8.75 H7Z M13.25 8 C13.25 7.58579 13.5858 7.25 14 7.25 H17 C19.6234 7.25 21.75 9.37665 21.75 12 C21.75 14.6234 19.6234 16.75 17 16.75 H14 C13.5858 16.75 13.25 16.4142 13.25 16 C13.25 15.5858 13.5858 15.25 14 15.25 H17 C18.7949 15.25 20.25 13.7949 20.25 12 C20.25 10.2051 18.7949 8.75 17 8.75 H14 C13.5858 8.75 13.25 8.41421 13.25 8Z M8.5 11.25 C8.08579 11.25 7.75 11.5858 7.75 12 C7.75 12.4142 8.08579 12.75 8.5 12.75 H15.5 C15.9142 12.75 16.25 12.4142 16.25 12 C16.25 11.5858 15.9142 11.25 15.5 11.25 H8.5Z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 7 8.75
        moveTo(x = 7.0f, y = 8.75f)
        // C 5.20507 8.75 3.75 10.2051 3.75 12
        curveTo(
          x1 = 5.20507f,
          y1 = 8.75f,
          x2 = 3.75f,
          y2 = 10.2051f,
          x3 = 3.75f,
          y3 = 12.0f,
        )
        // C 3.75 13.7949 5.20507 15.25 7 15.25
        curveTo(
          x1 = 3.75f,
          y1 = 13.7949f,
          x2 = 5.20507f,
          y2 = 15.25f,
          x3 = 7.0f,
          y3 = 15.25f,
        )
        // H 10
        horizontalLineTo(x = 10.0f)
        // C 10.4142 15.25 10.75 15.5858 10.75 16
        curveTo(
          x1 = 10.4142f,
          y1 = 15.25f,
          x2 = 10.75f,
          y2 = 15.5858f,
          x3 = 10.75f,
          y3 = 16.0f,
        )
        // C 10.75 16.4142 10.4142 16.75 10 16.75
        curveTo(
          x1 = 10.75f,
          y1 = 16.4142f,
          x2 = 10.4142f,
          y2 = 16.75f,
          x3 = 10.0f,
          y3 = 16.75f,
        )
        // H 7
        horizontalLineTo(x = 7.0f)
        // C 4.37665 16.75 2.25 14.6234 2.25 12
        curveTo(
          x1 = 4.37665f,
          y1 = 16.75f,
          x2 = 2.25f,
          y2 = 14.6234f,
          x3 = 2.25f,
          y3 = 12.0f,
        )
        // C 2.25 9.37665 4.37665 7.25 7 7.25
        curveTo(
          x1 = 2.25f,
          y1 = 9.37665f,
          x2 = 4.37665f,
          y2 = 7.25f,
          x3 = 7.0f,
          y3 = 7.25f,
        )
        // H 10
        horizontalLineTo(x = 10.0f)
        // C 10.4142 7.25 10.75 7.58579 10.75 8
        curveTo(
          x1 = 10.4142f,
          y1 = 7.25f,
          x2 = 10.75f,
          y2 = 7.58579f,
          x3 = 10.75f,
          y3 = 8.0f,
        )
        // C 10.75 8.41421 10.4142 8.75 10 8.75
        curveTo(
          x1 = 10.75f,
          y1 = 8.41421f,
          x2 = 10.4142f,
          y2 = 8.75f,
          x3 = 10.0f,
          y3 = 8.75f,
        )
        // H 7z
        horizontalLineTo(x = 7.0f)
        close()
        // M 13.25 8
        moveTo(x = 13.25f, y = 8.0f)
        // C 13.25 7.58579 13.5858 7.25 14 7.25
        curveTo(
          x1 = 13.25f,
          y1 = 7.58579f,
          x2 = 13.5858f,
          y2 = 7.25f,
          x3 = 14.0f,
          y3 = 7.25f,
        )
        // H 17
        horizontalLineTo(x = 17.0f)
        // C 19.6234 7.25 21.75 9.37665 21.75 12
        curveTo(
          x1 = 19.6234f,
          y1 = 7.25f,
          x2 = 21.75f,
          y2 = 9.37665f,
          x3 = 21.75f,
          y3 = 12.0f,
        )
        // C 21.75 14.6234 19.6234 16.75 17 16.75
        curveTo(
          x1 = 21.75f,
          y1 = 14.6234f,
          x2 = 19.6234f,
          y2 = 16.75f,
          x3 = 17.0f,
          y3 = 16.75f,
        )
        // H 14
        horizontalLineTo(x = 14.0f)
        // C 13.5858 16.75 13.25 16.4142 13.25 16
        curveTo(
          x1 = 13.5858f,
          y1 = 16.75f,
          x2 = 13.25f,
          y2 = 16.4142f,
          x3 = 13.25f,
          y3 = 16.0f,
        )
        // C 13.25 15.5858 13.5858 15.25 14 15.25
        curveTo(
          x1 = 13.25f,
          y1 = 15.5858f,
          x2 = 13.5858f,
          y2 = 15.25f,
          x3 = 14.0f,
          y3 = 15.25f,
        )
        // H 17
        horizontalLineTo(x = 17.0f)
        // C 18.7949 15.25 20.25 13.7949 20.25 12
        curveTo(
          x1 = 18.7949f,
          y1 = 15.25f,
          x2 = 20.25f,
          y2 = 13.7949f,
          x3 = 20.25f,
          y3 = 12.0f,
        )
        // C 20.25 10.2051 18.7949 8.75 17 8.75
        curveTo(
          x1 = 20.25f,
          y1 = 10.2051f,
          x2 = 18.7949f,
          y2 = 8.75f,
          x3 = 17.0f,
          y3 = 8.75f,
        )
        // H 14
        horizontalLineTo(x = 14.0f)
        // C 13.5858 8.75 13.25 8.41421 13.25 8z
        curveTo(
          x1 = 13.5858f,
          y1 = 8.75f,
          x2 = 13.25f,
          y2 = 8.41421f,
          x3 = 13.25f,
          y3 = 8.0f,
        )
        close()
        // M 8.5 11.25
        moveTo(x = 8.5f, y = 11.25f)
        // C 8.08579 11.25 7.75 11.5858 7.75 12
        curveTo(
          x1 = 8.08579f,
          y1 = 11.25f,
          x2 = 7.75f,
          y2 = 11.5858f,
          x3 = 7.75f,
          y3 = 12.0f,
        )
        // C 7.75 12.4142 8.08579 12.75 8.5 12.75
        curveTo(
          x1 = 7.75f,
          y1 = 12.4142f,
          x2 = 8.08579f,
          y2 = 12.75f,
          x3 = 8.5f,
          y3 = 12.75f,
        )
        // H 15.5
        horizontalLineTo(x = 15.5f)
        // C 15.9142 12.75 16.25 12.4142 16.25 12
        curveTo(
          x1 = 15.9142f,
          y1 = 12.75f,
          x2 = 16.25f,
          y2 = 12.4142f,
          x3 = 16.25f,
          y3 = 12.0f,
        )
        // C 16.25 11.5858 15.9142 11.25 15.5 11.25
        curveTo(
          x1 = 16.25f,
          y1 = 11.5858f,
          x2 = 15.9142f,
          y2 = 11.25f,
          x3 = 15.5f,
          y3 = 11.25f,
        )
        // H 8.5z
        horizontalLineTo(x = 8.5f)
        close()
      }
    }.build().also { _link = it }
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
        imageVector = HedvigIcons.Link,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _link: ImageVector? = null
