package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.ChevronLeft: ImageVector
  get() {
    if (_chevronLeft != null) {
      return _chevronLeft!!
    }
    _chevronLeft = materialIcon(name = "Chevron left") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(16.7073f, 21.4939f)
        curveTo(16.9801f, 21.1821f, 16.9485f, 20.7083f, 16.6368f, 20.4356f)
        lineTo(9.7914f, 14.4459f)
        curveTo(8.9733f, 13.7301f, 8.4208f, 13.2443f, 8.0628f, 12.8296f)
        curveTo(7.7188f, 12.4311f, 7.6443f, 12.1983f, 7.6443f, 12.0f)
        curveTo(7.6443f, 11.8017f, 7.7188f, 11.5688f, 8.0628f, 11.1703f)
        curveTo(8.4208f, 10.7557f, 8.9733f, 10.2699f, 9.7914f, 9.5541f)
        lineTo(16.6368f, 3.5644f)
        curveTo(16.9485f, 3.2917f, 16.9801f, 2.8178f, 16.7073f, 2.5061f)
        curveTo(16.4345f, 2.1944f, 15.9607f, 2.1628f, 15.649f, 2.4356f)
        lineTo(8.7615f, 8.4621f)
        curveTo(7.9963f, 9.1316f, 7.3625f, 9.6861f, 6.9275f, 10.1901f)
        curveTo(6.4705f, 10.7194f, 6.1443f, 11.2907f, 6.1443f, 12.0f)
        curveTo(6.1443f, 12.7092f, 6.4705f, 13.2806f, 6.9275f, 13.8099f)
        curveTo(7.3625f, 14.3139f, 7.9963f, 14.8684f, 8.7615f, 15.5378f)
        lineTo(15.649f, 21.5644f)
        curveTo(15.9607f, 21.8372f, 16.4345f, 21.8056f, 16.7073f, 21.4939f)
        close()
      }
    }
    return _chevronLeft!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _chevronLeft: ImageVector? = null
