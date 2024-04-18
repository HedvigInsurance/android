package com.hedvig.android.core.icons.hedvig.colored.hedvig

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.Chat: ImageVector
  get() {
    if (_chat != null) {
      return _chat!!
    }
    _chat = Builder(
      name = "Chat",
      defaultWidth = 32.0.dp,
      defaultHeight = 32.0.dp,
      viewportWidth = 32.0f,
      viewportHeight = 32.0f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF59BFFA)),
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = NonZero,
      ) {
        moveTo(32.0f, 16.0f)
        curveTo(32.0f, 24.8366f, 24.8366f, 32.0f, 16.0f, 32.0f)
        curveTo(7.1634f, 32.0f, 0.0f, 24.8366f, 0.0f, 16.0f)
        curveTo(0.0f, 7.1634f, 7.1634f, 0.0f, 16.0f, 0.0f)
        curveTo(24.8366f, 0.0f, 32.0f, 7.1634f, 32.0f, 16.0f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFFFAFAFA)),
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = NonZero,
      ) {
        moveTo(16.0f, 23.9927f)
        curveTo(20.4183f, 23.9927f, 24.0f, 20.4126f, 24.0f, 15.9963f)
        curveTo(24.0f, 11.5801f, 20.4183f, 8.0f, 16.0f, 8.0f)
        curveTo(11.5817f, 8.0f, 8.0f, 11.5801f, 8.0f, 15.9963f)
        curveTo(8.0f, 16.3329f, 8.0208f, 16.6647f, 8.0612f, 16.9904f)
        curveTo(8.1394f, 17.6553f, 8.3027f, 18.3078f, 8.5474f, 18.9316f)
        curveTo(8.6964f, 19.3116f, 8.7709f, 19.5016f, 8.7749f, 19.6569f)
        curveTo(8.7789f, 19.8121f, 8.7243f, 19.9758f, 8.6152f, 20.3031f)
        lineTo(8.193f, 21.5692f)
        curveTo(7.7046f, 23.0336f, 7.4604f, 23.7658f, 7.8471f, 24.1523f)
        curveTo(8.2338f, 24.5388f, 8.9663f, 24.2947f, 10.4314f, 23.8066f)
        lineTo(11.698f, 23.3846f)
        curveTo(12.0255f, 23.2755f, 12.1893f, 23.2209f, 12.3446f, 23.2249f)
        curveTo(12.4999f, 23.229f, 12.69f, 23.3034f, 13.0701f, 23.4523f)
        curveTo(13.9592f, 23.8006f, 14.9065f, 23.9841f, 15.8651f, 23.9915f)
        curveTo(15.91f, 23.9923f, 15.9549f, 23.9927f, 16.0f, 23.9927f)
        close()
      }
    }
      .build()
    return _chat!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _chat: ImageVector? = null
