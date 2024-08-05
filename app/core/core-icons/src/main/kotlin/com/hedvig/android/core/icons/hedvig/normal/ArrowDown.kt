package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.ArrowDown: ImageVector
  get() {
    if (_arrowDown != null) {
      return _arrowDown!!
    }
    _arrowDown = materialIcon(name = "Arrow down") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(11.9989f, 2.25f)
        curveTo(12.4131f, 2.2499f, 12.7489f, 2.5857f, 12.749f, 2.9999f)
        lineTo(12.751f, 19.9999f)
        curveTo(12.7511f, 20.4141f, 12.4153f, 20.7499f, 12.0011f, 20.75f)
        curveTo(11.5869f, 20.75f, 11.2511f, 20.4143f, 11.251f, 20.0001f)
        lineTo(11.249f, 3.0001f)
        curveTo(11.2489f, 2.5859f, 11.5847f, 2.2501f, 11.9989f, 2.25f)
        close()
      }
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(21.5017f, 10.4425f)
        curveTo(21.1938f, 10.1654f, 20.7196f, 10.1904f, 20.4425f, 10.4983f)
        lineTo(14.4157f, 17.1948f)
        curveTo(13.7066f, 17.9826f, 13.2262f, 18.5138f, 12.8172f, 18.8579f)
        curveTo(12.4246f, 19.1882f, 12.1954f, 19.2603f, 12.0f, 19.2603f)
        curveTo(11.8046f, 19.2603f, 11.5754f, 19.1882f, 11.1828f, 18.8579f)
        curveTo(10.7738f, 18.5138f, 10.2934f, 17.9826f, 9.5843f, 17.1948f)
        lineTo(3.5575f, 10.4983f)
        curveTo(3.2804f, 10.1904f, 2.8061f, 10.1654f, 2.4983f, 10.4425f)
        curveTo(2.1904f, 10.7196f, 2.1654f, 11.1938f, 2.4425f, 11.5017f)
        lineTo(8.5063f, 18.2392f)
        curveTo(9.1689f, 18.9756f, 9.7186f, 19.5864f, 10.2172f, 20.0058f)
        curveTo(10.7414f, 20.4468f, 11.3047f, 20.7603f, 12.0f, 20.7603f)
        curveTo(12.6953f, 20.7603f, 13.2585f, 20.4468f, 13.7828f, 20.0058f)
        curveTo(14.2814f, 19.5864f, 14.8311f, 18.9756f, 15.4937f, 18.2392f)
        lineTo(21.5575f, 11.5017f)
        curveTo(21.8346f, 11.1938f, 21.8096f, 10.7196f, 21.5017f, 10.4425f)
        close()
      }
    }
    return _arrowDown!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _arrowDown: ImageVector? = null
