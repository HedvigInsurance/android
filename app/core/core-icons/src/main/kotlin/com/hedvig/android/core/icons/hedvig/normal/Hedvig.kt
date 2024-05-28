package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.Hedvig: ImageVector
  get() {
    if (_hedvig != null) {
      return _hedvig!!
    }
    _hedvig = ImageVector.Builder(
      name = "Hedvig logo",
      defaultWidth = 24f.dp,
      defaultHeight = 24f.dp,
      viewportWidth = 40f,
      viewportHeight = 40f,
    ).apply {
      materialPath(pathFillType = PathFillType.NonZero) {
        moveTo(14.0932f, 18.6496f)
        horizontalLineTo(25.9075f)
        verticalLineTo(9.11377f)
        horizontalLineTo(28.608f)
        verticalLineTo(30.8859f)
        horizontalLineTo(25.9075f)
        verticalLineTo(21.3501f)
        horizontalLineTo(14.0932f)
        verticalLineTo(30.8859f)
        horizontalLineTo(11.3084f)
        verticalLineTo(9.11377f)
        horizontalLineTo(14.0932f)
        verticalLineTo(18.6496f)
        close()
      }

      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(0f, 20f)
        curveTo(0f, 8.9451f, 8.9452f, 0f, 20f, 0f)
        curveTo(31.0549f, 0f, 40f, 8.9451f, 40f, 20f)
        curveTo(40f, 31.0549f, 31.0549f, 40f, 20f, 40f)
        curveTo(8.9452f, 40f, 0f, 31.0549f, 0f, 20f)
        close()
        moveTo(2.70043f, 20f)
        curveTo(2.7004f, 29.5359f, 10.4641f, 37.2996f, 20f, 37.2996f)
        curveTo(29.5359f, 37.2996f, 37.2996f, 29.5359f, 37.2996f, 20f)
        curveTo(37.2996f, 10.4641f, 29.5359f, 2.7004f, 20f, 2.7004f)
        curveTo(10.4641f, 2.7004f, 2.7004f, 10.4641f, 2.7004f, 20f)
        close()
      }
    }.build()
    return _hedvig!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _hedvig: ImageVector? = null
