package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.MinusInCircle: ImageVector
  get() {
    if (_minusInCircle != null) {
      return _minusInCircle!!
    }
    _minusInCircle = materialIcon(name = "Minus in circle") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(12.0f, 3.75f)
        curveTo(7.4436f, 3.75f, 3.75f, 7.4436f, 3.75f, 12.0f)
        curveTo(3.75f, 16.5563f, 7.4436f, 20.25f, 12.0f, 20.25f)
        curveTo(16.5563f, 20.25f, 20.25f, 16.5563f, 20.25f, 12.0f)
        curveTo(20.25f, 7.4436f, 16.5563f, 3.75f, 12.0f, 3.75f)
        close()
        moveTo(2.25f, 12.0f)
        curveTo(2.25f, 6.6152f, 6.6152f, 2.25f, 12.0f, 2.25f)
        curveTo(17.3848f, 2.25f, 21.75f, 6.6152f, 21.75f, 12.0f)
        curveTo(21.75f, 17.3848f, 17.3848f, 21.75f, 12.0f, 21.75f)
        curveTo(6.6152f, 21.75f, 2.25f, 17.3848f, 2.25f, 12.0f)
        close()
        moveTo(7.25f, 12.0f)
        curveTo(7.25f, 11.5858f, 7.5858f, 11.25f, 8.0f, 11.25f)
        horizontalLineTo(16.0f)
        curveTo(16.4142f, 11.25f, 16.75f, 11.5858f, 16.75f, 12.0f)
        curveTo(16.75f, 12.4142f, 16.4142f, 12.75f, 16.0f, 12.75f)
        horizontalLineTo(8.0f)
        curveTo(7.5858f, 12.75f, 7.25f, 12.4142f, 7.25f, 12.0f)
        close()
      }
    }
    return _minusInCircle!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _minusInCircle: ImageVector? = null
