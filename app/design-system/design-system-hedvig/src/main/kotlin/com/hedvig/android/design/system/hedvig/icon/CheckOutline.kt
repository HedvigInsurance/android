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
val HedvigIcons.CheckOutline: ImageVector
  get() {
    val current = _checkOutline
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.CheckOutline",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M20 12 C20 16.4183 16.4183 20 12 20 C7.58172 20 4 16.4183 4 12 C4 7.58172 7.58172 4 12 4 C16.4183 4 20 7.58172 20 12Z M21.5 12 C21.5 17.2467 17.2467 21.5 12 21.5 C6.75329 21.5 2.5 17.2467 2.5 12 C2.5 6.75329 6.75329 2.5 12 2.5 C17.2467 2.5 21.5 6.75329 21.5 12Z M16.3441 10.2303 C16.637 9.93739 16.637 9.46252 16.3442 9.16962 C16.0513 8.87673 15.5764 8.87673 15.2835 9.16962 L10.9336 13.5195 C10.836 13.6171 10.6777 13.6171 10.58 13.5195 L8.82117 11.7606 C8.52828 11.4677 8.0534 11.4677 7.76051 11.7606 C7.46762 12.0535 7.46762 12.5284 7.76051 12.8213 L9.51938 14.5801 C10.2028 15.2636 11.3108 15.2636 11.9943 14.5801 L16.3441 10.2303Z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 20 12
        moveTo(x = 20.0f, y = 12.0f)
        // C 20 16.4183 16.4183 20 12 20
        curveTo(
          x1 = 20.0f,
          y1 = 16.4183f,
          x2 = 16.4183f,
          y2 = 20.0f,
          x3 = 12.0f,
          y3 = 20.0f,
        )
        // C 7.58172 20 4 16.4183 4 12
        curveTo(
          x1 = 7.58172f,
          y1 = 20.0f,
          x2 = 4.0f,
          y2 = 16.4183f,
          x3 = 4.0f,
          y3 = 12.0f,
        )
        // C 4 7.58172 7.58172 4 12 4
        curveTo(
          x1 = 4.0f,
          y1 = 7.58172f,
          x2 = 7.58172f,
          y2 = 4.0f,
          x3 = 12.0f,
          y3 = 4.0f,
        )
        // C 16.4183 4 20 7.58172 20 12z
        curveTo(
          x1 = 16.4183f,
          y1 = 4.0f,
          x2 = 20.0f,
          y2 = 7.58172f,
          x3 = 20.0f,
          y3 = 12.0f,
        )
        close()
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
        // M 16.3441 10.2303
        moveTo(x = 16.3441f, y = 10.2303f)
        // C 16.637 9.93739 16.637 9.46252 16.3442 9.16962
        curveTo(
          x1 = 16.637f,
          y1 = 9.93739f,
          x2 = 16.637f,
          y2 = 9.46252f,
          x3 = 16.3442f,
          y3 = 9.16962f,
        )
        // C 16.0513 8.87673 15.5764 8.87673 15.2835 9.16962
        curveTo(
          x1 = 16.0513f,
          y1 = 8.87673f,
          x2 = 15.5764f,
          y2 = 8.87673f,
          x3 = 15.2835f,
          y3 = 9.16962f,
        )
        // L 10.9336 13.5195
        lineTo(x = 10.9336f, y = 13.5195f)
        // C 10.836 13.6171 10.6777 13.6171 10.58 13.5195
        curveTo(
          x1 = 10.836f,
          y1 = 13.6171f,
          x2 = 10.6777f,
          y2 = 13.6171f,
          x3 = 10.58f,
          y3 = 13.5195f,
        )
        // L 8.82117 11.7606
        lineTo(x = 8.82117f, y = 11.7606f)
        // C 8.52828 11.4677 8.0534 11.4677 7.76051 11.7606
        curveTo(
          x1 = 8.52828f,
          y1 = 11.4677f,
          x2 = 8.0534f,
          y2 = 11.4677f,
          x3 = 7.76051f,
          y3 = 11.7606f,
        )
        // C 7.46762 12.0535 7.46762 12.5284 7.76051 12.8213
        curveTo(
          x1 = 7.46762f,
          y1 = 12.0535f,
          x2 = 7.46762f,
          y2 = 12.5284f,
          x3 = 7.76051f,
          y3 = 12.8213f,
        )
        // L 9.51938 14.5801
        lineTo(x = 9.51938f, y = 14.5801f)
        // C 10.2028 15.2636 11.3108 15.2636 11.9943 14.5801
        curveTo(
          x1 = 10.2028f,
          y1 = 15.2636f,
          x2 = 11.3108f,
          y2 = 15.2636f,
          x3 = 11.9943f,
          y3 = 14.5801f,
        )
        // L 16.3441 10.2303z
        lineTo(x = 16.3441f, y = 10.2303f)
        close()
      }
    }.build().also { _checkOutline = it }
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
        imageVector = HedvigIcons.CheckOutline,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _checkOutline: ImageVector? = null
