package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.ArrowUp: ImageVector
  get() {
    if (_arrowUp != null) {
      return _arrowUp!!
    }
    _arrowUp = materialIcon(name = "Arrow up") {
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(12.4979f, 21.7457f)
        curveTo(12.0837f, 21.7457f, 11.7479f, 21.4099f, 11.7479f, 20.9957f)
        lineTo(11.7479f, 4.0f)
        curveTo(11.7479f, 3.5858f, 12.0836f, 3.25f, 12.4979f, 3.25f)
        curveTo(12.9121f, 3.25f, 13.2479f, 3.5858f, 13.2479f, 4.0f)
        lineTo(13.2479f, 20.9957f)
        curveTo(13.2479f, 21.4099f, 12.9121f, 21.7457f, 12.4979f, 21.7457f)
        close()
      }
      materialPath(pathFillType = PathFillType.EvenOdd) {
        moveTo(22.0017f, 13.5575f)
        curveTo(21.6938f, 13.8346f, 21.2196f, 13.8096f, 20.9425f, 13.5017f)
        lineTo(14.9157f, 6.8053f)
        curveTo(14.2066f, 6.0174f, 13.7262f, 5.4862f, 13.3172f, 5.1421f)
        curveTo(12.9246f, 4.8118f, 12.6954f, 4.7397f, 12.5f, 4.7397f)
        curveTo(12.3046f, 4.7397f, 12.0754f, 4.8118f, 11.6828f, 5.1421f)
        curveTo(11.2738f, 5.4862f, 10.7934f, 6.0174f, 10.0843f, 6.8053f)
        lineTo(4.0575f, 13.5017f)
        curveTo(3.7804f, 13.8096f, 3.3061f, 13.8346f, 2.9983f, 13.5575f)
        curveTo(2.6904f, 13.2804f, 2.6654f, 12.8062f, 2.9425f, 12.4983f)
        lineTo(9.0063f, 5.7608f)
        curveTo(9.6689f, 5.0244f, 10.2186f, 4.4136f, 10.7172f, 3.9942f)
        curveTo(11.2414f, 3.5532f, 11.8047f, 3.2397f, 12.5f, 3.2397f)
        curveTo(13.1953f, 3.2397f, 13.7585f, 3.5532f, 14.2828f, 3.9942f)
        curveTo(14.7814f, 4.4136f, 15.3311f, 5.0244f, 15.9937f, 5.7608f)
        lineTo(22.0575f, 12.4983f)
        curveTo(22.3346f, 12.8062f, 22.3096f, 13.2804f, 22.0017f, 13.5575f)
        close()
      }
    }
    return _arrowUp!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _arrowUp: ImageVector? = null
