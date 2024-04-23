package com.hedvig.android.core.icons.hedvig.small.hedvig

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.SquircleWithCheckmark: ImageVector
  get() {
    if (_squircleWithCheckmark != null) {
      return _squircleWithCheckmark!!
    }
    _squircleWithCheckmark = Builder(
      name = "Square with checkmark",
      defaultWidth = 16.0.dp,
      defaultHeight =
        16.0.dp,
      viewportWidth = 16.0f,
      viewportHeight = 16.0f,
    ).apply {
      path(
        fill = SolidColor(Color.Black),
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = EvenOdd,
      ) {
        moveTo(8.0085f, 15.0f)
        curveTo(2.703f, 15.0f, 1.0f, 13.512f, 1.0f, 8.0f)
        curveTo(1.0f, 2.488f, 2.703f, 1.0f, 8.0085f, 1.0f)
        curveTo(13.314f, 1.0f, 15.0f, 2.3998f, 15.0f, 8.0f)
        curveTo(15.0f, 13.6002f, 13.314f, 15.0f, 8.0085f, 15.0f)
        close()
        moveTo(11.8265f, 6.5682f)
        curveTo(12.1092f, 6.2654f, 12.0928f, 5.7908f, 11.79f, 5.5082f)
        curveTo(11.4872f, 5.2256f, 11.0126f, 5.2419f, 10.7299f, 5.5447f)
        lineTo(7.1222f, 9.4101f)
        curveTo(7.0234f, 9.516f, 6.8555f, 9.516f, 6.7567f, 9.4101f)
        lineTo(5.3184f, 7.8691f)
        curveTo(5.0357f, 7.5663f, 4.5612f, 7.5499f, 4.2583f, 7.8325f)
        curveTo(3.9555f, 8.1151f, 3.9392f, 8.5897f, 4.2218f, 8.8926f)
        lineTo(5.6601f, 10.4336f)
        curveTo(6.352f, 11.1749f, 7.527f, 11.1749f, 8.2188f, 10.4336f)
        lineTo(11.8265f, 6.5682f)
        close()
      }
    }
      .build()
    return _squircleWithCheckmark!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _squircleWithCheckmark: ImageVector? = null
