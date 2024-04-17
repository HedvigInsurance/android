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
val HedvigIcons.Copy: ImageVector
  get() {
    val current = _copy
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Copy",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M10.8888 1.75 C9.37001 1.75 8.13879 2.98122 8.13879 4.5 V5.20459 H7 C5.48122 5.20459 4.25 6.43581 4.25 7.95459 V19.5 C4.25 21.0188 5.48122 22.25 7 22.25 H13.1111 C14.6299 22.25 15.8611 21.0188 15.8611 19.5 V18.7955 H16.9999 C18.5187 18.7955 19.7499 17.5642 19.7499 16.0455 V4.5 C19.7499 2.98122 18.5187 1.75 16.9999 1.75 H10.8888Z M15.8611 17.2955 H16.9999 C17.6903 17.2955 18.2499 16.7358 18.2499 16.0455 V4.5 C18.2499 3.80964 17.6903 3.25 16.9999 3.25 H10.8888 C10.1984 3.25 9.63879 3.80964 9.63879 4.5 V5.20459 H13.1111 C14.6299 5.20459 15.8611 6.43581 15.8611 7.95459 V17.2955Z M5.75 7.95459 C5.75 7.26424 6.30965 6.70459 7 6.70459 H13.1111 C13.8015 6.70459 14.3611 7.26423 14.3611 7.95459 V19.5 C14.3611 20.1904 13.8015 20.75 13.1111 20.75 H7 C6.30964 20.75 5.75 20.1904 5.75 19.5 V7.95459Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 10.8888 1.75
        moveTo(x = 10.8888f, y = 1.75f)
        // C 9.37001 1.75 8.13879 2.98122 8.13879 4.5
        curveTo(
          x1 = 9.37001f,
          y1 = 1.75f,
          x2 = 8.13879f,
          y2 = 2.98122f,
          x3 = 8.13879f,
          y3 = 4.5f,
        )
        // V 5.20459
        verticalLineTo(y = 5.20459f)
        // H 7
        horizontalLineTo(x = 7.0f)
        // C 5.48122 5.20459 4.25 6.43581 4.25 7.95459
        curveTo(
          x1 = 5.48122f,
          y1 = 5.20459f,
          x2 = 4.25f,
          y2 = 6.43581f,
          x3 = 4.25f,
          y3 = 7.95459f,
        )
        // V 19.5
        verticalLineTo(y = 19.5f)
        // C 4.25 21.0188 5.48122 22.25 7 22.25
        curveTo(
          x1 = 4.25f,
          y1 = 21.0188f,
          x2 = 5.48122f,
          y2 = 22.25f,
          x3 = 7.0f,
          y3 = 22.25f,
        )
        // H 13.1111
        horizontalLineTo(x = 13.1111f)
        // C 14.6299 22.25 15.8611 21.0188 15.8611 19.5
        curveTo(
          x1 = 14.6299f,
          y1 = 22.25f,
          x2 = 15.8611f,
          y2 = 21.0188f,
          x3 = 15.8611f,
          y3 = 19.5f,
        )
        // V 18.7955
        verticalLineTo(y = 18.7955f)
        // H 16.9999
        horizontalLineTo(x = 16.9999f)
        // C 18.5187 18.7955 19.7499 17.5642 19.7499 16.0455
        curveTo(
          x1 = 18.5187f,
          y1 = 18.7955f,
          x2 = 19.7499f,
          y2 = 17.5642f,
          x3 = 19.7499f,
          y3 = 16.0455f,
        )
        // V 4.5
        verticalLineTo(y = 4.5f)
        // C 19.7499 2.98122 18.5187 1.75 16.9999 1.75
        curveTo(
          x1 = 19.7499f,
          y1 = 2.98122f,
          x2 = 18.5187f,
          y2 = 1.75f,
          x3 = 16.9999f,
          y3 = 1.75f,
        )
        // H 10.8888z
        horizontalLineTo(x = 10.8888f)
        close()
        // M 15.8611 17.2955
        moveTo(x = 15.8611f, y = 17.2955f)
        // H 16.9999
        horizontalLineTo(x = 16.9999f)
        // C 17.6903 17.2955 18.2499 16.7358 18.2499 16.0455
        curveTo(
          x1 = 17.6903f,
          y1 = 17.2955f,
          x2 = 18.2499f,
          y2 = 16.7358f,
          x3 = 18.2499f,
          y3 = 16.0455f,
        )
        // V 4.5
        verticalLineTo(y = 4.5f)
        // C 18.2499 3.80964 17.6903 3.25 16.9999 3.25
        curveTo(
          x1 = 18.2499f,
          y1 = 3.80964f,
          x2 = 17.6903f,
          y2 = 3.25f,
          x3 = 16.9999f,
          y3 = 3.25f,
        )
        // H 10.8888
        horizontalLineTo(x = 10.8888f)
        // C 10.1984 3.25 9.63879 3.80964 9.63879 4.5
        curveTo(
          x1 = 10.1984f,
          y1 = 3.25f,
          x2 = 9.63879f,
          y2 = 3.80964f,
          x3 = 9.63879f,
          y3 = 4.5f,
        )
        // V 5.20459
        verticalLineTo(y = 5.20459f)
        // H 13.1111
        horizontalLineTo(x = 13.1111f)
        // C 14.6299 5.20459 15.8611 6.43581 15.8611 7.95459
        curveTo(
          x1 = 14.6299f,
          y1 = 5.20459f,
          x2 = 15.8611f,
          y2 = 6.43581f,
          x3 = 15.8611f,
          y3 = 7.95459f,
        )
        // V 17.2955z
        verticalLineTo(y = 17.2955f)
        close()
        // M 5.75 7.95459
        moveTo(x = 5.75f, y = 7.95459f)
        // C 5.75 7.26424 6.30965 6.70459 7 6.70459
        curveTo(
          x1 = 5.75f,
          y1 = 7.26424f,
          x2 = 6.30965f,
          y2 = 6.70459f,
          x3 = 7.0f,
          y3 = 6.70459f,
        )
        // H 13.1111
        horizontalLineTo(x = 13.1111f)
        // C 13.8015 6.70459 14.3611 7.26423 14.3611 7.95459
        curveTo(
          x1 = 13.8015f,
          y1 = 6.70459f,
          x2 = 14.3611f,
          y2 = 7.26423f,
          x3 = 14.3611f,
          y3 = 7.95459f,
        )
        // V 19.5
        verticalLineTo(y = 19.5f)
        // C 14.3611 20.1904 13.8015 20.75 13.1111 20.75
        curveTo(
          x1 = 14.3611f,
          y1 = 20.1904f,
          x2 = 13.8015f,
          y2 = 20.75f,
          x3 = 13.1111f,
          y3 = 20.75f,
        )
        // H 7
        horizontalLineTo(x = 7.0f)
        // C 6.30964 20.75 5.75 20.1904 5.75 19.5
        curveTo(
          x1 = 6.30964f,
          y1 = 20.75f,
          x2 = 5.75f,
          y2 = 20.1904f,
          x3 = 5.75f,
          y3 = 19.5f,
        )
        // V 7.95459z
        verticalLineTo(y = 7.95459f)
        close()
      }
    }.build().also { _copy = it }
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
        imageVector = HedvigIcons.Copy,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _copy: ImageVector? = null
