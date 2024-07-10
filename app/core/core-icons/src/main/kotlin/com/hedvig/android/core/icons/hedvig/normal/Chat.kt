package com.hedvig.android.core.icons.hedvig.colored.hedvig

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.Chat: ImageVector
  get() {
    if (_chat != null) {
      return _chat!!
    }
    _chat = ImageVector
      .Builder(
        name = "Chat",
        defaultWidth = 32.0.dp,
        defaultHeight = 32.0.dp,
        viewportWidth = 32.0f,
        viewportHeight = 32.0f,
      ).apply {
        path(
          fill = SolidColor(Color(0xFF000000)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(16f, 32.0001f)
          curveTo(24.8366f, 32.0001f, 32f, 24.8366f, 32f, 16.0001f)
          curveTo(32f, 7.1635f, 24.8366f, 0.0001f, 16f, 0.0001f)
          curveTo(7.1634f, 0.0001f, 0f, 7.1635f, 0f, 16.0001f)
          curveTo(0f, 24.8366f, 7.1634f, 32.0001f, 16f, 32.0001f)
          close()
          moveTo(7.5f, 11.5f)
          curveTo(7.5f, 10.3954f, 8.3954f, 9.5f, 9.5f, 9.5f)
          horizontalLineTo(22.5f)
          curveTo(23.6046f, 9.5f, 24.5f, 10.3954f, 24.5f, 11.5f)
          verticalLineTo(20.5f)
          curveTo(24.5f, 21.6046f, 23.6046f, 22.5f, 22.5f, 22.5f)
          horizontalLineTo(9.5f)
          curveTo(8.3954f, 22.5f, 7.5f, 21.6046f, 7.5f, 20.5f)
          verticalLineTo(11.5f)
          close()
          moveTo(9.82169f, 13.4264f)
          lineTo(15.4495f, 17.1371f)
          curveTo(15.7835f, 17.3572f, 16.2165f, 17.3572f, 16.5505f, 17.1371f)
          lineTo(22.1783f, 13.4264f)
          curveTo(22.3791f, 13.294f, 22.5f, 13.0695f, 22.5f, 12.829f)
          curveTo(22.5f, 12.2591f, 21.8663f, 11.9178f, 21.3905f, 12.2315f)
          lineTo(16.5505f, 15.4228f)
          curveTo(16.2165f, 15.643f, 15.7835f, 15.643f, 15.4495f, 15.4228f)
          lineTo(10.6095f, 12.2315f)
          curveTo(10.1337f, 11.9178f, 9.5f, 12.2591f, 9.5f, 12.829f)
          curveTo(9.5f, 13.0695f, 9.6209f, 13.294f, 9.8217f, 13.4264f)
          close()
        }
      }.build()
    return _chat!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _chat: ImageVector? = null
