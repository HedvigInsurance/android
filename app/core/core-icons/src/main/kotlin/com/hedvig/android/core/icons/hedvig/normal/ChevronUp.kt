package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.ChevronUp: ImageVector
  get() {
    if (_chevronUp != null) {
      return _chevronUp!!
    }
    _chevronUp = materialIcon(name = "Chevron up") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(2.4983f, 16.5575f)
        curveTo(2.8062f, 16.8346f, 3.2804f, 16.8096f, 3.5575f, 16.5017f)
        lineTo(9.5843f, 9.8053f)
        curveTo(10.2934f, 9.0174f, 10.7738f, 8.4862f, 11.1828f, 8.1421f)
        curveTo(11.5754f, 7.8118f, 11.8046f, 7.7397f, 12.0f, 7.7397f)
        curveTo(12.1954f, 7.7397f, 12.4246f, 7.8118f, 12.8172f, 8.1421f)
        curveTo(13.2262f, 8.4862f, 13.7066f, 9.0174f, 14.4157f, 9.8053f)
        lineTo(20.4425f, 16.5017f)
        curveTo(20.7196f, 16.8096f, 21.1938f, 16.8346f, 21.5017f, 16.5575f)
        curveTo(21.8096f, 16.2804f, 21.8346f, 15.8062f, 21.5575f, 15.4983f)
        lineTo(15.4937f, 8.7608f)
        curveTo(14.8311f, 8.0244f, 14.2814f, 7.4136f, 13.7828f, 6.9942f)
        curveTo(13.2586f, 6.5532f, 12.6953f, 6.2397f, 12.0f, 6.2397f)
        curveTo(11.3047f, 6.2397f, 10.7415f, 6.5532f, 10.2172f, 6.9942f)
        curveTo(9.7186f, 7.4136f, 9.169f, 8.0244f, 8.5063f, 8.7608f)
        lineTo(2.4425f, 15.4983f)
        curveTo(2.1654f, 15.8062f, 2.1904f, 16.2804f, 2.4983f, 16.5575f)
        close()
      }
    }
    return _chevronUp!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _chevronUp: ImageVector? = null
