package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.CircleWithX: ImageVector
  get() {
    if (_circleWithX != null) {
      return _circleWithX!!
    }
    _circleWithX = materialIcon(name = "Circle with X") {
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
        moveTo(15.3588f, 8.6412f)
        curveTo(15.6517f, 8.9341f, 15.6517f, 9.409f, 15.3588f, 9.7019f)
        lineTo(13.0607f, 12.0f)
        lineTo(15.3588f, 14.2981f)
        curveTo(15.6517f, 14.591f, 15.6517f, 15.0658f, 15.3588f, 15.3587f)
        curveTo(15.0659f, 15.6516f, 14.591f, 15.6516f, 14.2981f, 15.3587f)
        lineTo(12.0f, 13.0606f)
        lineTo(9.7019f, 15.3587f)
        curveTo(9.409f, 15.6516f, 8.9341f, 15.6516f, 8.6412f, 15.3587f)
        curveTo(8.3483f, 15.0658f, 8.3483f, 14.591f, 8.6412f, 14.2981f)
        lineTo(10.9393f, 12.0f)
        lineTo(8.6412f, 9.7019f)
        curveTo(8.3483f, 9.409f, 8.3483f, 8.9341f, 8.6412f, 8.6412f)
        curveTo(8.9341f, 8.3483f, 9.409f, 8.3483f, 9.7019f, 8.6412f)
        lineTo(12.0f, 10.9393f)
        lineTo(14.2981f, 8.6412f)
        curveTo(14.591f, 8.3483f, 15.0659f, 8.3483f, 15.3588f, 8.6412f)
        close()
      }
    }
    return _circleWithX!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _circleWithX: ImageVector? = null
