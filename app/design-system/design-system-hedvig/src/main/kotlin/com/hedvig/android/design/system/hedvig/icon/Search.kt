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
val HedvigIcons.Search: ImageVector
  get() {
    val current = _search
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Search",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M14 9.5 C14 11.9853 11.9853 14 9.5 14 C7.01472 14 5 11.9853 5 9.5 C5 7.01472 7.01472 5 9.5 5 C11.9853 5 14 7.01472 14 9.5Z M13.1792 14.2399 C12.1632 15.0297 10.8865 15.5 9.5 15.5 C6.18629 15.5 3.5 12.8137 3.5 9.5 C3.5 6.18629 6.18629 3.5 9.5 3.5 C12.8137 3.5 15.5 6.18629 15.5 9.5 C15.5 10.8865 15.0297 12.1632 14.2399 13.1793 L20.0303 18.9697 C20.3232 19.2626 20.3232 19.7375 20.0303 20.0304 C19.7374 20.3233 19.2626 20.3233 18.9697 20.0304 L13.1792 14.2399Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 14 9.5
        moveTo(x = 14.0f, y = 9.5f)
        // C 14 11.9853 11.9853 14 9.5 14
        curveTo(
          x1 = 14.0f,
          y1 = 11.9853f,
          x2 = 11.9853f,
          y2 = 14.0f,
          x3 = 9.5f,
          y3 = 14.0f,
        )
        // C 7.01472 14 5 11.9853 5 9.5
        curveTo(
          x1 = 7.01472f,
          y1 = 14.0f,
          x2 = 5.0f,
          y2 = 11.9853f,
          x3 = 5.0f,
          y3 = 9.5f,
        )
        // C 5 7.01472 7.01472 5 9.5 5
        curveTo(
          x1 = 5.0f,
          y1 = 7.01472f,
          x2 = 7.01472f,
          y2 = 5.0f,
          x3 = 9.5f,
          y3 = 5.0f,
        )
        // C 11.9853 5 14 7.01472 14 9.5z
        curveTo(
          x1 = 11.9853f,
          y1 = 5.0f,
          x2 = 14.0f,
          y2 = 7.01472f,
          x3 = 14.0f,
          y3 = 9.5f,
        )
        close()
        // M 13.1792 14.2399
        moveTo(x = 13.1792f, y = 14.2399f)
        // C 12.1632 15.0297 10.8865 15.5 9.5 15.5
        curveTo(
          x1 = 12.1632f,
          y1 = 15.0297f,
          x2 = 10.8865f,
          y2 = 15.5f,
          x3 = 9.5f,
          y3 = 15.5f,
        )
        // C 6.18629 15.5 3.5 12.8137 3.5 9.5
        curveTo(
          x1 = 6.18629f,
          y1 = 15.5f,
          x2 = 3.5f,
          y2 = 12.8137f,
          x3 = 3.5f,
          y3 = 9.5f,
        )
        // C 3.5 6.18629 6.18629 3.5 9.5 3.5
        curveTo(
          x1 = 3.5f,
          y1 = 6.18629f,
          x2 = 6.18629f,
          y2 = 3.5f,
          x3 = 9.5f,
          y3 = 3.5f,
        )
        // C 12.8137 3.5 15.5 6.18629 15.5 9.5
        curveTo(
          x1 = 12.8137f,
          y1 = 3.5f,
          x2 = 15.5f,
          y2 = 6.18629f,
          x3 = 15.5f,
          y3 = 9.5f,
        )
        // C 15.5 10.8865 15.0297 12.1632 14.2399 13.1793
        curveTo(
          x1 = 15.5f,
          y1 = 10.8865f,
          x2 = 15.0297f,
          y2 = 12.1632f,
          x3 = 14.2399f,
          y3 = 13.1793f,
        )
        // L 20.0303 18.9697
        lineTo(x = 20.0303f, y = 18.9697f)
        // C 20.3232 19.2626 20.3232 19.7375 20.0303 20.0304
        curveTo(
          x1 = 20.3232f,
          y1 = 19.2626f,
          x2 = 20.3232f,
          y2 = 19.7375f,
          x3 = 20.0303f,
          y3 = 20.0304f,
        )
        // C 19.7374 20.3233 19.2626 20.3233 18.9697 20.0304
        curveTo(
          x1 = 19.7374f,
          y1 = 20.3233f,
          x2 = 19.2626f,
          y2 = 20.3233f,
          x3 = 18.9697f,
          y3 = 20.0304f,
        )
        // L 13.1792 14.2399z
        lineTo(x = 13.1792f, y = 14.2399f)
        close()
      }
    }.build().also { _search = it }
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
        imageVector = HedvigIcons.Search,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _search: ImageVector? = null
