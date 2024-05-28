package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.ArrowForward: ImageVector
  get() {
    if (_arrowForward != null) {
      return _arrowForward!!
    }
    _arrowForward = materialIcon(name = "Arrow forward") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(3.25f, 12.0f)
        curveTo(3.25f, 11.5858f, 3.5858f, 11.25f, 4.0f, 11.25f)
        horizontalLineTo(20.0f)
        curveTo(20.4142f, 11.25f, 20.75f, 11.5858f, 20.75f, 12.0f)
        curveTo(20.75f, 12.4142f, 20.4142f, 12.75f, 20.0f, 12.75f)
        horizontalLineTo(4.0f)
        curveTo(3.5858f, 12.75f, 3.25f, 12.4142f, 3.25f, 12.0f)
        close()
      }
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(10.4425f, 2.4983f)
        curveTo(10.1654f, 2.8062f, 10.1904f, 3.2804f, 10.4983f, 3.5575f)
        lineTo(17.1948f, 9.5843f)
        curveTo(17.9826f, 10.2934f, 18.5138f, 10.7738f, 18.8579f, 11.1828f)
        curveTo(19.1882f, 11.5754f, 19.2603f, 11.8046f, 19.2603f, 12.0f)
        curveTo(19.2603f, 12.1954f, 19.1882f, 12.4246f, 18.8579f, 12.8172f)
        curveTo(18.5138f, 13.2262f, 17.9826f, 13.7066f, 17.1948f, 14.4157f)
        lineTo(10.4983f, 20.4425f)
        curveTo(10.1904f, 20.7196f, 10.1654f, 21.1938f, 10.4425f, 21.5017f)
        curveTo(10.7196f, 21.8096f, 11.1938f, 21.8346f, 11.5017f, 21.5575f)
        lineTo(18.2392f, 15.4937f)
        curveTo(18.9756f, 14.8311f, 19.5864f, 14.2814f, 20.0058f, 13.7828f)
        curveTo(20.4468f, 13.2586f, 20.7603f, 12.6953f, 20.7603f, 12.0f)
        curveTo(20.7603f, 11.3047f, 20.4468f, 10.7415f, 20.0058f, 10.2172f)
        curveTo(19.5864f, 9.7186f, 18.9756f, 9.169f, 18.2392f, 8.5063f)
        lineTo(11.5017f, 2.4425f)
        curveTo(11.1938f, 2.1654f, 10.7196f, 2.1904f, 10.4425f, 2.4983f)
        close()
      }
    }
    return _arrowForward!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _arrowForward: ImageVector? = null
