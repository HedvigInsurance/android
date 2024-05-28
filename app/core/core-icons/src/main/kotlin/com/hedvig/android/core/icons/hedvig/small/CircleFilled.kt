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
public val HedvigIcons.CircleFilled: ImageVector
  get() {
    if (_circleFilled != null) {
      return _circleFilled!!
    }
    _circleFilled = Builder(
      name = "Circle filled",
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
        moveTo(8.0f, 8.0f)
        moveToRelative(-7.0f, 0.0f)
        arcToRelative(7.0f, 7.0f, 0.0f, true, true, 14.0f, 0.0f)
        arcToRelative(7.0f, 7.0f, 0.0f, true, true, -14.0f, 0.0f)
      }
    }
      .build()
    return _circleFilled!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _circleFilled: ImageVector? = null
