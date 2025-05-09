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
val HedvigIcons.CheckFilled: ImageVector
  get() {
    val current = _checkFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.CheckFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 21.5 C17.2467 21.5 21.5 17.2467 21.5 12 C21.5 6.75329 17.2467 2.5 12 2.5 C6.75329 2.5 2.5 6.75329 2.5 12 C2.5 17.2467 6.75329 21.5 12 21.5Z M16.3441 10.2303 C16.637 9.93739 16.637 9.46252 16.3442 9.16962 C16.0513 8.87673 15.5764 8.87673 15.2835 9.16962 L10.9336 13.5195 C10.836 13.6171 10.6777 13.6171 10.58 13.5195 L8.82117 11.7606 C8.52828 11.4677 8.0534 11.4677 7.76051 11.7606 C7.46762 12.0535 7.46762 12.5284 7.76051 12.8213 L9.51938 14.5801 C10.2028 15.2636 11.3108 15.2636 11.9943 14.5801 L16.3441 10.2303Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
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
    }.build().also { _checkFilled = it }
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
        imageVector = HedvigIcons.CheckFilled,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _checkFilled: ImageVector? = null
