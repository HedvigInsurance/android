package com.hedvig.android.core.icons.hedvig.small.hedvig

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
public val HedvigIcons.Plus: ImageVector
  get() {
    if (_plus != null) {
      return _plus!!
    }
    _plus = Builder(
      name = "Plus",
      defaultWidth = 16.0.dp,
      defaultHeight = 16.0.dp,
      viewportWidth = 16.0f,
      viewportHeight = 16.0f,
    ).apply {
      path(
        fill = SolidColor(Color.Black),
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = NonZero,
      ) {
        moveTo(8.7488f, 2.0f)
        curveTo(8.7488f, 1.5858f, 8.413f, 1.25f, 7.9988f, 1.25f)
        curveTo(7.5846f, 1.25f, 7.2488f, 1.5858f, 7.2488f, 2.0f)
        verticalLineTo(7.2488f)
        horizontalLineTo(2.0f)
        curveTo(1.5858f, 7.2488f, 1.25f, 7.5846f, 1.25f, 7.9988f)
        curveTo(1.25f, 8.413f, 1.5858f, 8.7488f, 2.0f, 8.7488f)
        horizontalLineTo(7.2488f)
        verticalLineTo(14.0f)
        curveTo(7.2488f, 14.4142f, 7.5846f, 14.75f, 7.9988f, 14.75f)
        curveTo(8.413f, 14.75f, 8.7488f, 14.4142f, 8.7488f, 14.0f)
        verticalLineTo(8.7488f)
        horizontalLineTo(14.0f)
        curveTo(14.4142f, 8.7488f, 14.75f, 8.413f, 14.75f, 7.9988f)
        curveTo(14.75f, 7.5846f, 14.4142f, 7.2488f, 14.0f, 7.2488f)
        horizontalLineTo(8.7488f)
        verticalLineTo(2.0f)
        close()
      }
    }
      .build()
    return _plus!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _plus: ImageVector? = null
