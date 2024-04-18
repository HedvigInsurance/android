package com.hedvig.android.core.icons.hedvig.normal

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.icons.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.AppleLogo: ImageVector
  get() {
    if (_appleLogo != null) {
      return _appleLogo!!
    }
    _appleLogo = materialIcon(name = "Apple logo") {
      materialPath(pathFillType = PathFillType.NonZero) {
        moveTo(16.2011f, 0.0f)
        curveTo(16.2523f, 0.0f, 16.3035f, 0.0f, 16.3576f, 0.0f)
        curveTo(16.4832f, 1.5515f, 15.891f, 2.7108f, 15.1713f, 3.5503f)
        curveTo(14.4651f, 4.384f, 13.4981f, 5.1926f, 11.9341f, 5.0699f)
        curveTo(11.8297f, 3.5406f, 12.4229f, 2.4673f, 13.1416f, 1.6297f)
        curveTo(13.8082f, 0.8492f, 15.0303f, 0.1546f, 16.2011f, 0.0f)
        close()
      }
      materialPath(pathFillType = PathFillType.NonZero) {
        moveTo(20.9358f, 16.1486f)
        curveTo(20.9358f, 16.164f, 20.9358f, 16.1776f, 20.9358f, 16.1921f)
        curveTo(20.4962f, 17.5233f, 19.8693f, 18.6642f, 19.1042f, 19.723f)
        curveTo(18.4057f, 20.6842f, 17.5498f, 21.9778f, 16.0215f, 21.9778f)
        curveTo(14.7009f, 21.9778f, 13.8237f, 21.1286f, 12.4702f, 21.1054f)
        curveTo(11.0386f, 21.0822f, 10.2512f, 21.8155f, 8.9422f, 22.0f)
        curveTo(8.7925f, 22.0f, 8.6427f, 22.0f, 8.4959f, 22.0f)
        curveTo(7.5347f, 21.8609f, 6.7589f, 21.0996f, 6.1938f, 20.4137f)
        curveTo(4.5273f, 18.3869f, 3.2396f, 15.7689f, 3.0f, 12.4186f)
        curveTo(3.0f, 12.0902f, 3.0f, 11.7627f, 3.0f, 11.4342f)
        curveTo(3.1014f, 9.0365f, 4.2665f, 7.087f, 5.8151f, 6.1422f)
        curveTo(6.6324f, 5.6398f, 7.7559f, 5.2119f, 9.0069f, 5.4031f)
        curveTo(9.5431f, 5.4862f, 10.0909f, 5.6698f, 10.571f, 5.8514f)
        curveTo(11.026f, 6.0262f, 11.595f, 6.3363f, 12.1341f, 6.3199f)
        curveTo(12.4992f, 6.3093f, 12.8625f, 6.119f, 13.2305f, 5.9847f)
        curveTo(14.3087f, 5.5954f, 15.3655f, 5.1491f, 16.7586f, 5.3587f)
        curveTo(18.4327f, 5.6118f, 19.621f, 6.3557f, 20.3552f, 7.5033f)
        curveTo(18.939f, 8.4047f, 17.8193f, 9.7629f, 18.0106f, 12.0825f)
        curveTo(18.1806f, 14.1894f, 19.4056f, 15.4221f, 20.9358f, 16.1486f)
        close()
      }
    }
    return _appleLogo!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _appleLogo: ImageVector? = null
