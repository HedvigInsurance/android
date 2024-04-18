package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.CircleWithCheckmark: ImageVector
  get() {
    if (_circleWithCheckmark != null) {
      return _circleWithCheckmark!!
    }
    _circleWithCheckmark = materialIcon(name = "Circle with checkmark") {
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
      }
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(15.5303f, 9.4697f)
        curveTo(15.8232f, 9.7626f, 15.8232f, 10.2374f, 15.5303f, 10.5303f)
        lineTo(12.2374f, 13.8232f)
        curveTo(11.554f, 14.5066f, 10.446f, 14.5066f, 9.7626f, 13.8232f)
        lineTo(8.4697f, 12.5303f)
        curveTo(8.1768f, 12.2374f, 8.1768f, 11.7626f, 8.4697f, 11.4697f)
        curveTo(8.7626f, 11.1768f, 9.2374f, 11.1768f, 9.5303f, 11.4697f)
        lineTo(10.8232f, 12.7626f)
        curveTo(10.9209f, 12.8602f, 11.0791f, 12.8602f, 11.1768f, 12.7626f)
        lineTo(14.4697f, 9.4697f)
        curveTo(14.7626f, 9.1768f, 15.2374f, 9.1768f, 15.5303f, 9.4697f)
        close()
      }
    }
    return _circleWithCheckmark!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _circleWithCheckmark: ImageVector? = null
