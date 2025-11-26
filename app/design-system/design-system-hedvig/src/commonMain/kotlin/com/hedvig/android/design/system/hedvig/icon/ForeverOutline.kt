package com.hedvig.android.design.system.hedvig.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.ForeverOutline: ImageVector
  get() {
    if (_forever != null) {
      return _forever!!
    }
    _forever = Builder(
      name = "Forever tab",
      defaultWidth = 25.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 25.0f,
      viewportHeight = 24.0f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF121212)),
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        pathFillType = EvenOdd,
      ) {
        moveTo(17.5f, 8.25f)
        curveTo(19.2949f, 8.25f, 20.75f, 9.7051f, 20.75f, 11.5f)
        curveTo(20.75f, 13.2949f, 19.2949f, 14.75f, 17.5f, 14.75f)
        curveTo(16.5391f, 14.75f, 15.6763f, 14.3339f, 15.0803f, 13.6697f)
        lineTo(11.0983f, 8.4019f)
        curveTo(11.0869f, 8.3868f, 11.0749f, 8.3721f, 11.0623f, 8.3579f)
        curveTo(10.193f, 7.373f, 8.9188f, 6.75f, 7.5f, 6.75f)
        curveTo(4.8766f, 6.75f, 2.75f, 8.8766f, 2.75f, 11.5f)
        curveTo(2.75f, 14.1234f, 4.8766f, 16.25f, 7.5f, 16.25f)
        curveTo(8.6331f, 16.25f, 9.6754f, 15.8523f, 10.4918f, 15.1895f)
        curveTo(10.8134f, 14.9285f, 10.8625f, 14.4561f, 10.6015f, 14.1345f)
        curveTo(10.3404f, 13.813f, 9.8681f, 13.7639f, 9.5465f, 14.0249f)
        curveTo(8.9875f, 14.4786f, 8.2765f, 14.75f, 7.5f, 14.75f)
        curveTo(5.7051f, 14.75f, 4.25f, 13.2949f, 4.25f, 11.5f)
        curveTo(4.25f, 9.7051f, 5.7051f, 8.25f, 7.5f, 8.25f)
        curveTo(8.4609f, 8.25f, 9.3237f, 8.6661f, 9.9198f, 9.3303f)
        lineTo(13.9017f, 14.5981f)
        curveTo(13.9131f, 14.6132f, 13.9251f, 14.6279f, 13.9377f, 14.6421f)
        curveTo(14.807f, 15.627f, 16.0812f, 16.25f, 17.5f, 16.25f)
        curveTo(20.1234f, 16.25f, 22.25f, 14.1234f, 22.25f, 11.5f)
        curveTo(22.25f, 8.8766f, 20.1234f, 6.75f, 17.5f, 6.75f)
        curveTo(16.3389f, 6.75f, 15.2733f, 7.1676f, 14.4482f, 7.8599f)
        curveTo(14.1309f, 8.1262f, 14.0896f, 8.5992f, 14.3558f, 8.9165f)
        curveTo(14.6221f, 9.2339f, 15.0951f, 9.2752f, 15.4124f, 9.009f)
        curveTo(15.9775f, 8.5349f, 16.7045f, 8.25f, 17.5f, 8.25f)
        close()
      }
    }
      .build()
    return _forever!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _forever: ImageVector? = null
