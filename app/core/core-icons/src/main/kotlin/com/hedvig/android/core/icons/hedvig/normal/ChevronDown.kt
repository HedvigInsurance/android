package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.ChevronDown: ImageVector
  get() {
    if (_chevronDown != null) {
      return _chevronDown!!
    }
    _chevronDown = materialIcon(name = "Chevron down") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(21.5017f, 7.4425f)
        curveTo(21.1938f, 7.1654f, 20.7196f, 7.1904f, 20.4425f, 7.4983f)
        lineTo(14.4157f, 14.1948f)
        curveTo(13.7066f, 14.9826f, 13.2262f, 15.5138f, 12.8172f, 15.8579f)
        curveTo(12.4246f, 16.1882f, 12.1954f, 16.2603f, 12.0f, 16.2603f)
        curveTo(11.8046f, 16.2603f, 11.5754f, 16.1882f, 11.1828f, 15.8579f)
        curveTo(10.7738f, 15.5138f, 10.2934f, 14.9826f, 9.5843f, 14.1948f)
        lineTo(3.5575f, 7.4983f)
        curveTo(3.2804f, 7.1904f, 2.8061f, 7.1654f, 2.4983f, 7.4425f)
        curveTo(2.1904f, 7.7196f, 2.1654f, 8.1938f, 2.4425f, 8.5017f)
        lineTo(8.5063f, 15.2392f)
        curveTo(9.1689f, 15.9756f, 9.7186f, 16.5864f, 10.2172f, 17.0058f)
        curveTo(10.7414f, 17.4468f, 11.3047f, 17.7603f, 12.0f, 17.7603f)
        curveTo(12.6953f, 17.7603f, 13.2585f, 17.4468f, 13.7828f, 17.0058f)
        curveTo(14.2814f, 16.5864f, 14.8311f, 15.9756f, 15.4937f, 15.2392f)
        lineTo(21.5575f, 8.5017f)
        curveTo(21.8346f, 8.1938f, 21.8096f, 7.7196f, 21.5017f, 7.4425f)
        close()
      }
    }
    return _chevronDown!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _chevronDown: ImageVector? = null
