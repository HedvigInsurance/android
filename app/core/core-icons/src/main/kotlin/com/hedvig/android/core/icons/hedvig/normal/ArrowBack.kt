package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.ArrowBack: ImageVector
  get() {
    if (_arrowBack != null) {
      return _arrowBack!!
    }
    _arrowBack = materialIcon(name = "Arrow back") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(13.5575f, 2.4983f)
        curveTo(13.8346f, 2.8062f, 13.8096f, 3.2804f, 13.5017f, 3.5575f)
        lineTo(6.8053f, 9.5843f)
        curveTo(6.0174f, 10.2934f, 5.4862f, 10.7738f, 5.1421f, 11.1828f)
        curveTo(4.8118f, 11.5754f, 4.7397f, 11.8046f, 4.7397f, 12.0f)
        curveTo(4.7397f, 12.1954f, 4.8118f, 12.4246f, 5.1421f, 12.8172f)
        curveTo(5.4862f, 13.2262f, 6.0174f, 13.7066f, 6.8053f, 14.4157f)
        lineTo(13.5017f, 20.4425f)
        curveTo(13.8096f, 20.7196f, 13.8346f, 21.1938f, 13.5575f, 21.5017f)
        curveTo(13.2804f, 21.8096f, 12.8062f, 21.8346f, 12.4983f, 21.5575f)
        lineTo(5.7608f, 15.4937f)
        curveTo(5.0244f, 14.8311f, 4.4136f, 14.2814f, 3.9942f, 13.7828f)
        curveTo(3.5532f, 13.2586f, 3.2397f, 12.6953f, 3.2397f, 12.0f)
        curveTo(3.2397f, 11.3047f, 3.5532f, 10.7415f, 3.9942f, 10.2172f)
        curveTo(4.4136f, 9.7186f, 5.0244f, 9.169f, 5.7608f, 8.5063f)
        lineTo(12.4983f, 2.4425f)
        curveTo(12.8062f, 2.1654f, 13.2804f, 2.1904f, 13.5575f, 2.4983f)
        close()
      }
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(21.0f, 12.75f)
        lineTo(4.0f, 12.75f)
        verticalLineTo(11.25f)
        lineTo(21.0f, 11.25f)
        curveTo(21.4142f, 11.25f, 21.75f, 11.5858f, 21.75f, 12.0f)
        curveTo(21.75f, 12.4142f, 21.4142f, 12.75f, 21.0f, 12.75f)
        close()
      }
    }
    return _arrowBack!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _arrowBack: ImageVector? = null
