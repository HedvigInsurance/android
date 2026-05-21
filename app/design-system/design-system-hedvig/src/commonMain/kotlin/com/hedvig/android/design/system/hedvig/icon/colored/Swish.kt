package com.hedvig.android.design.system.hedvig.icon.colored

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.icon.CheckFilled
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

val HedvigIcons.Swish: ImageVector
  get() {
    if (_Swish != null) {
      return _Swish!!
    }
    _Swish = ImageVector.Builder(
      name = "Swish",
      defaultWidth = 420.dp,
      defaultHeight = 566.dp,
      viewportWidth = 420f,
      viewportHeight = 566f
    ).apply {
      path(fill = SolidColor(Color(0xFF17191F))) {
        moveTo(381.4f, 485.9f)
        curveToRelative(0f, -2f, 0.4f, -3.9f, 1.1f, -5.7f)
        curveToRelative(0.7f, -1.8f, 1.8f, -3.3f, 3f, -4.6f)
        curveToRelative(1.3f, -1.3f, 2.8f, -2.4f, 4.5f, -3.1f)
        curveToRelative(1.7f, -0.8f, 3.6f, -1.1f, 5.5f, -1.1f)
        reflectiveCurveToRelative(3.9f, 0.4f, 5.6f, 1.1f)
        curveToRelative(1.7f, 0.8f, 3.3f, 1.8f, 4.6f, 3.1f)
        reflectiveCurveToRelative(2.3f, 2.9f, 3.1f, 4.6f)
        curveToRelative(0.7f, 1.8f, 1.1f, 3.7f, 1.1f, 5.7f)
        reflectiveCurveToRelative(-0.4f, 3.9f, -1.1f, 5.7f)
        curveToRelative(-0.7f, 1.8f, -1.8f, 3.3f, -3.1f, 4.6f)
        curveToRelative(-1.3f, 1.3f, -2.8f, 2.4f, -4.6f, 3.1f)
        curveToRelative(-1.7f, 0.8f, -3.6f, 1.1f, -5.6f, 1.1f)
        reflectiveCurveToRelative(-3.8f, -0.4f, -5.5f, -1.1f)
        curveToRelative(-1.7f, -0.8f, -3.2f, -1.8f, -4.5f, -3.1f)
        curveToRelative(-1.3f, -1.3f, -2.3f, -2.9f, -3f, -4.6f)
        curveToRelative(-0.7f, -1.8f, -1.1f, -3.7f, -1.1f, -5.7f)
        close()
        moveTo(384.3f, 485.9f)
        curveToRelative(0f, 1.7f, 0.3f, 3.2f, 0.9f, 4.7f)
        curveToRelative(0.6f, 1.4f, 1.4f, 2.7f, 2.4f, 3.8f)
        curveToRelative(1f, 1.1f, 2.2f, 1.9f, 3.6f, 2.5f)
        curveToRelative(1.4f, 0.6f, 2.9f, 0.9f, 4.5f, 0.9f)
        reflectiveCurveToRelative(3.1f, -0.3f, 4.5f, -0.9f)
        curveToRelative(1.4f, -0.6f, 2.6f, -1.5f, 3.6f, -2.5f)
        reflectiveCurveToRelative(1.8f, -2.3f, 2.4f, -3.8f)
        curveToRelative(0.6f, -1.4f, 0.9f, -3f, 0.9f, -4.7f)
        reflectiveCurveToRelative(-0.3f, -3.2f, -0.9f, -4.7f)
        curveToRelative(-0.6f, -1.4f, -1.4f, -2.7f, -2.4f, -3.8f)
        curveToRelative(-1f, -1.1f, -2.2f, -1.9f, -3.6f, -2.5f)
        curveToRelative(-1.4f, -0.6f, -2.9f, -0.9f, -4.5f, -0.9f)
        reflectiveCurveToRelative(-3.1f, 0.3f, -4.5f, 0.9f)
        curveToRelative(-1.4f, 0.6f, -2.6f, 1.5f, -3.6f, 2.5f)
        curveToRelative(-1f, 1.1f, -1.8f, 2.3f, -2.4f, 3.8f)
        curveToRelative(-0.6f, 1.4f, -0.9f, 3f, -0.9f, 4.7f)
        close()
        moveTo(390.7f, 479.6f)
        curveToRelative(0f, -0.9f, 0.4f, -1.3f, 1.3f, -1.3f)
        horizontalLineToRelative(4.5f)
        curveToRelative(1.4f, 0f, 2.6f, 0.4f, 3.4f, 1.2f)
        curveToRelative(0.9f, 0.8f, 1.3f, 1.9f, 1.3f, 3.4f)
        reflectiveCurveToRelative(0f, 1.1f, -0.3f, 1.6f)
        curveToRelative(-0.2f, 0.5f, -0.4f, 0.8f, -0.7f, 1.2f)
        curveToRelative(-0.3f, 0.3f, -0.6f, 0.6f, -0.9f, 0.8f)
        reflectiveCurveToRelative(-0.6f, 0.4f, -1f, 0.4f)
        horizontalLineToRelative(0f)
        curveToRelative(0f, 0.1f, 0f, 0.2f, 0.1f, 0.2f)
        curveToRelative(0f, 0f, 0.1f, 0.1f, 0.2f, 0.3f)
        curveToRelative(0f, 0.1f, 0.1f, 0.3f, 0.2f, 0.4f)
        lineToRelative(2.1f, 4f)
        curveToRelative(0.2f, 0.5f, 0.3f, 0.8f, 0.2f, 1.1f)
        curveToRelative(-0.1f, 0.3f, -0.4f, 0.4f, -0.9f, 0.4f)
        horizontalLineToRelative(-0.5f)
        curveToRelative(-0.7f, 0f, -1.3f, -0.3f, -1.6f, -1f)
        lineToRelative(-2.3f, -4.9f)
        horizontalLineToRelative(-2.5f)
        verticalLineToRelative(4.6f)
        curveToRelative(0f, 0.9f, -0.4f, 1.3f, -1.2f, 1.3f)
        horizontalLineToRelative(-0.4f)
        curveToRelative(-0.8f, 0f, -1.2f, -0.4f, -1.2f, -1.3f)
        verticalLineToRelative(-12.5f)
        close()
        moveTo(396f, 485.4f)
        curveToRelative(0.8f, 0f, 1.4f, -0.2f, 1.8f, -0.7f)
        reflectiveCurveToRelative(0.6f, -1.1f, 0.6f, -1.9f)
        reflectiveCurveToRelative(-0.2f, -1.4f, -0.6f, -1.8f)
        curveToRelative(-0.4f, -0.4f, -1f, -0.6f, -1.8f, -0.6f)
        horizontalLineToRelative(-2.4f)
        verticalLineToRelative(5f)
        horizontalLineToRelative(2.4f)
        close()
        moveTo(279.3f, 495f)
        curveToRelative(4.5f, 0f, 8.4f, 0.6f, 11.5f, 1.7f)
        curveToRelative(3.2f, 1.2f, 5.6f, 2.2f, 7.4f, 3.2f)
        curveToRelative(1.5f, 0.8f, 2.4f, 1.9f, 2.7f, 3.2f)
        curveToRelative(0.3f, 1.3f, 0f, 2.7f, -0.7f, 4.3f)
        lineToRelative(-1.3f, 2.4f)
        curveToRelative(-0.8f, 1.6f, -1.8f, 2.5f, -3.1f, 2.8f)
        reflectiveCurveToRelative(-2.7f, 0f, -4.4f, -0.7f)
        curveToRelative(-1.5f, -0.7f, -3.3f, -1.4f, -5.5f, -2.2f)
        reflectiveCurveToRelative(-4.6f, -1.1f, -7.5f, -1.1f)
        reflectiveCurveToRelative(-5.2f, 0.6f, -6.8f, 1.7f)
        curveToRelative(-1.6f, 1.2f, -2.4f, 2.8f, -2.4f, 4.9f)
        reflectiveCurveToRelative(0.8f, 3.4f, 2.5f, 4.5f)
        curveToRelative(1.6f, 1.2f, 3.7f, 2.2f, 6.3f, 3.1f)
        curveToRelative(2.5f, 0.9f, 5.2f, 1.8f, 8.1f, 2.9f)
        curveToRelative(2.9f, 1f, 5.6f, 2.3f, 8.1f, 3.9f)
        reflectiveCurveToRelative(4.6f, 3.6f, 6.3f, 6.1f)
        curveToRelative(1.6f, 2.5f, 2.5f, 5.6f, 2.5f, 9.4f)
        reflectiveCurveToRelative(-0.6f, 5.8f, -1.8f, 8.3f)
        reflectiveCurveToRelative(-2.9f, 4.7f, -5.2f, 6.6f)
        curveToRelative(-2.3f, 1.9f, -5f, 3.3f, -8.2f, 4.4f)
        curveToRelative(-3.2f, 1.1f, -6.7f, 1.6f, -10.7f, 1.6f)
        reflectiveCurveToRelative(-10.1f, -0.8f, -13.9f, -2.4f)
        curveToRelative(-3.8f, -1.6f, -6.7f, -3.1f, -8.7f, -4.5f)
        curveToRelative(-1.5f, -0.9f, -2.4f, -2f, -2.5f, -3.3f)
        curveToRelative(-0.2f, -1.3f, 0.2f, -2.7f, 1.2f, -4.3f)
        lineToRelative(1.6f, -2.4f)
        curveToRelative(1f, -1.4f, 2.1f, -2.2f, 3.3f, -2.4f)
        reflectiveCurveToRelative(2.6f, 0.2f, 4.3f, 1.1f)
        curveToRelative(1.6f, 0.9f, 3.7f, 1.9f, 6.2f, 3f)
        curveToRelative(2.5f, 1.1f, 5.5f, 1.7f, 9f, 1.7f)
        reflectiveCurveToRelative(5.2f, -0.6f, 6.9f, -1.9f)
        reflectiveCurveToRelative(2.5f, -2.9f, 2.5f, -5.1f)
        reflectiveCurveToRelative(-0.8f, -3.3f, -2.5f, -4.5f)
        curveToRelative(-1.6f, -1.1f, -3.7f, -2.1f, -6.3f, -3.1f)
        curveToRelative(-2.5f, -0.9f, -5.2f, -1.9f, -8.1f, -3f)
        curveToRelative(-2.9f, -1.1f, -5.6f, -2.4f, -8.1f, -4f)
        curveToRelative(-2.5f, -1.6f, -4.6f, -3.6f, -6.3f, -6.1f)
        curveToRelative(-1.6f, -2.5f, -2.5f, -5.7f, -2.5f, -9.6f)
        reflectiveCurveToRelative(0.7f, -6.2f, 2.1f, -8.8f)
        curveToRelative(1.4f, -2.6f, 3.2f, -4.7f, 5.6f, -6.4f)
        reflectiveCurveToRelative(5.1f, -3f, 8.3f, -3.9f)
        curveToRelative(3.2f, -0.9f, 6.5f, -1.3f, 10.1f, -1.3f)
        close()
        moveTo(81.3f, 495f)
        curveToRelative(4.5f, 0f, 8.4f, 0.6f, 11.5f, 1.7f)
        reflectiveCurveToRelative(5.6f, 2.2f, 7.4f, 3.2f)
        curveToRelative(1.5f, 0.8f, 2.4f, 1.9f, 2.7f, 3.2f)
        curveToRelative(0.3f, 1.3f, 0f, 2.7f, -0.7f, 4.3f)
        lineToRelative(-1.3f, 2.4f)
        curveToRelative(-0.8f, 1.6f, -1.8f, 2.5f, -3.1f, 2.8f)
        reflectiveCurveToRelative(-2.7f, 0f, -4.4f, -0.7f)
        curveToRelative(-1.5f, -0.7f, -3.3f, -1.4f, -5.5f, -2.2f)
        curveToRelative(-2.1f, -0.8f, -4.6f, -1.1f, -7.5f, -1.1f)
        reflectiveCurveToRelative(-5.2f, 0.6f, -6.8f, 1.7f)
        curveToRelative(-1.6f, 1.2f, -2.4f, 2.8f, -2.4f, 4.9f)
        reflectiveCurveToRelative(0.8f, 3.4f, 2.5f, 4.5f)
        curveToRelative(1.6f, 1.2f, 3.7f, 2.2f, 6.3f, 3.1f)
        curveToRelative(2.5f, 0.9f, 5.2f, 1.8f, 8.1f, 2.9f)
        curveToRelative(2.9f, 1f, 5.6f, 2.3f, 8.1f, 3.9f)
        curveToRelative(2.5f, 1.6f, 4.6f, 3.6f, 6.3f, 6.1f)
        curveToRelative(1.6f, 2.5f, 2.5f, 5.6f, 2.5f, 9.4f)
        reflectiveCurveToRelative(-0.6f, 5.8f, -1.8f, 8.3f)
        reflectiveCurveToRelative(-2.9f, 4.7f, -5.2f, 6.6f)
        curveToRelative(-2.3f, 1.9f, -5f, 3.3f, -8.2f, 4.4f)
        curveToRelative(-3.2f, 1.1f, -6.7f, 1.6f, -10.7f, 1.6f)
        reflectiveCurveToRelative(-10.1f, -0.8f, -13.9f, -2.4f)
        curveToRelative(-3.8f, -1.6f, -6.7f, -3.1f, -8.7f, -4.5f)
        curveToRelative(-1.5f, -0.9f, -2.4f, -2f, -2.5f, -3.3f)
        curveToRelative(-0.2f, -1.3f, 0.2f, -2.7f, 1.2f, -4.3f)
        lineToRelative(1.6f, -2.4f)
        curveToRelative(1f, -1.4f, 2.1f, -2.2f, 3.3f, -2.4f)
        curveToRelative(1.2f, -0.2f, 2.6f, 0.2f, 4.3f, 1.1f)
        curveToRelative(1.6f, 0.9f, 3.7f, 1.9f, 6.2f, 3f)
        curveToRelative(2.5f, 1.1f, 5.5f, 1.7f, 9f, 1.7f)
        reflectiveCurveToRelative(5.2f, -0.6f, 6.9f, -1.9f)
        curveToRelative(1.7f, -1.2f, 2.5f, -2.9f, 2.5f, -5.1f)
        reflectiveCurveToRelative(-0.8f, -3.3f, -2.5f, -4.5f)
        curveToRelative(-1.6f, -1.1f, -3.7f, -2.1f, -6.3f, -3.1f)
        curveToRelative(-2.5f, -0.9f, -5.2f, -1.9f, -8.1f, -3f)
        curveToRelative(-2.9f, -1.1f, -5.6f, -2.4f, -8.1f, -4f)
        curveToRelative(-2.5f, -1.6f, -4.6f, -3.6f, -6.3f, -6.1f)
        reflectiveCurveToRelative(-2.5f, -5.7f, -2.5f, -9.6f)
        reflectiveCurveToRelative(0.7f, -6.2f, 2.1f, -8.8f)
        curveToRelative(1.4f, -2.6f, 3.2f, -4.7f, 5.6f, -6.4f)
        reflectiveCurveToRelative(5.1f, -3f, 8.3f, -3.9f)
        curveToRelative(3.2f, -0.9f, 6.5f, -1.3f, 10.1f, -1.3f)
        close()
        moveTo(324.1f, 470f)
        curveToRelative(3.7f, 0f, 5.6f, 1.9f, 5.6f, 5.6f)
        verticalLineToRelative(27.4f)
        curveToRelative(0f, 0.9f, 0f, 1.7f, 0f, 2.3f)
        curveToRelative(0f, 0.7f, -0.1f, 1.3f, -0.2f, 1.8f)
        curveToRelative(0f, 0.6f, -0.1f, 1.2f, -0.1f, 1.6f)
        horizontalLineToRelative(0.3f)
        curveToRelative(0.8f, -1.6f, 1.9f, -3.2f, 3.4f, -4.9f)
        curveToRelative(1.5f, -1.6f, 3.2f, -3.1f, 5.2f, -4.5f)
        curveToRelative(2f, -1.3f, 4.3f, -2.4f, 6.8f, -3.2f)
        curveToRelative(2.5f, -0.8f, 5.3f, -1.2f, 8.2f, -1.2f)
        curveToRelative(7.5f, 0f, 13.4f, 2f, 17.5f, 6.1f)
        curveToRelative(4.1f, 4.1f, 6.2f, 10.6f, 6.2f, 19.7f)
        verticalLineToRelative(38f)
        curveToRelative(0f, 3.7f, -1.9f, 5.6f, -5.6f, 5.6f)
        horizontalLineToRelative(-5.7f)
        curveToRelative(-3.7f, 0f, -5.6f, -1.9f, -5.6f, -5.6f)
        verticalLineToRelative(-34.6f)
        curveToRelative(0f, -4.2f, -0.7f, -7.5f, -2.1f, -10f)
        reflectiveCurveToRelative(-4.3f, -3.8f, -8.5f, -3.8f)
        reflectiveCurveToRelative(-5.6f, 0.6f, -8.1f, 1.7f)
        curveToRelative(-2.4f, 1.2f, -4.5f, 2.7f, -6.2f, 4.7f)
        curveToRelative(-1.7f, 2f, -3f, 4.4f, -3.9f, 7.1f)
        curveToRelative(-0.9f, 2.7f, -1.4f, 5.7f, -1.4f, 8.9f)
        verticalLineToRelative(25.9f)
        curveToRelative(0f, 3.7f, -1.9f, 5.6f, -5.6f, 5.6f)
        horizontalLineToRelative(-5.7f)
        curveToRelative(-3.7f, 0f, -5.6f, -1.9f, -5.6f, -5.6f)
        verticalLineToRelative(-83.2f)
        curveToRelative(0f, -3.7f, 1.9f, -5.6f, 5.6f, -5.6f)
        horizontalLineToRelative(5.7f)
        close()
        moveTo(235.7f, 496.6f)
        curveToRelative(3.6f, 0f, 5.5f, 1.9f, 5.5f, 5.6f)
        verticalLineToRelative(56.6f)
        curveToRelative(0f, 3.7f, -1.8f, 5.6f, -5.5f, 5.6f)
        horizontalLineToRelative(-5.9f)
        curveToRelative(-3.6f, 0f, -5.5f, -1.9f, -5.5f, -5.6f)
        verticalLineToRelative(-56.6f)
        curveToRelative(0f, -3.7f, 1.8f, -5.6f, 5.5f, -5.6f)
        horizontalLineToRelative(5.9f)
        close()
        moveTo(122.7f, 496.6f)
        curveToRelative(3.4f, 0f, 5.4f, 1.6f, 6f, 4.9f)
        lineToRelative(10.9f, 39.6f)
        curveToRelative(0.2f, 1f, 0.3f, 1.9f, 0.5f, 2.7f)
        curveToRelative(0.1f, 0.8f, 0.3f, 1.6f, 0.5f, 2.3f)
        curveToRelative(0.2f, 0.8f, 0.3f, 1.6f, 0.4f, 2.3f)
        horizontalLineToRelative(0.3f)
        curveToRelative(0f, -0.7f, 0.2f, -1.5f, 0.4f, -2.3f)
        curveToRelative(0.2f, -0.7f, 0.3f, -1.5f, 0.5f, -2.3f)
        curveToRelative(0.1f, -0.8f, 0.3f, -1.7f, 0.6f, -2.7f)
        lineToRelative(11.5f, -39.6f)
        curveToRelative(0.6f, -3.2f, 2.7f, -4.8f, 6.1f, -4.8f)
        horizontalLineToRelative(5.1f)
        curveToRelative(3.3f, 0f, 5.3f, 1.6f, 6.1f, 4.8f)
        lineToRelative(11.3f, 39.6f)
        curveToRelative(0.3f, 1f, 0.5f, 1.9f, 0.6f, 2.7f)
        curveToRelative(0.1f, 0.8f, 0.3f, 1.6f, 0.5f, 2.3f)
        curveToRelative(0.2f, 0.8f, 0.3f, 1.6f, 0.4f, 2.3f)
        horizontalLineToRelative(0.3f)
        curveToRelative(0f, -0.7f, 0.2f, -1.5f, 0.4f, -2.3f)
        curveToRelative(0.2f, -0.7f, 0.3f, -1.5f, 0.5f, -2.3f)
        curveToRelative(0.1f, -0.8f, 0.3f, -1.7f, 0.6f, -2.7f)
        lineToRelative(10.8f, -39.6f)
        curveToRelative(0.8f, -3.3f, 2.8f, -4.9f, 6.1f, -4.9f)
        horizontalLineToRelative(6.1f)
        curveToRelative(2f, 0f, 3.5f, 0.6f, 4.3f, 1.7f)
        curveToRelative(0.8f, 1.2f, 0.9f, 2.7f, 0.4f, 4.5f)
        lineToRelative(-17.4f, 56.9f)
        curveToRelative(-0.9f, 3.1f, -3f, 4.7f, -6.3f, 4.7f)
        horizontalLineToRelative(-8.9f)
        curveToRelative(-3.4f, 0f, -5.5f, -1.6f, -6.3f, -4.8f)
        lineToRelative(-10.3f, -33.9f)
        curveToRelative(-0.3f, -0.9f, -0.5f, -1.8f, -0.7f, -2.7f)
        reflectiveCurveToRelative(-0.4f, -1.7f, -0.5f, -2.4f)
        curveToRelative(-0.2f, -0.8f, -0.3f, -1.6f, -0.4f, -2.3f)
        horizontalLineToRelative(-0.3f)
        curveToRelative(-0.2f, 0.7f, -0.4f, 1.5f, -0.5f, 2.3f)
        curveToRelative(-0.2f, 0.7f, -0.4f, 1.5f, -0.5f, 2.4f)
        curveToRelative(-0.2f, 0.9f, -0.4f, 1.8f, -0.7f, 2.7f)
        lineToRelative(-10.3f, 33.9f)
        curveToRelative(-0.8f, 3.2f, -2.8f, 4.8f, -6.1f, 4.8f)
        horizontalLineToRelative(-9.2f)
        curveToRelative(-3.2f, 0f, -5.2f, -1.6f, -6.1f, -4.7f)
        lineToRelative(-17.6f, -56.9f)
        curveToRelative(-0.5f, -1.9f, -0.4f, -3.4f, 0.5f, -4.5f)
        curveToRelative(0.8f, -1.2f, 2.2f, -1.7f, 4.2f, -1.7f)
        horizontalLineToRelative(6.4f)
        close()
      }
      path(
        fill = Brush.linearGradient(
          colorStops = arrayOf(
            0f to Color(0xFFEF2131),
            1f to Color(0xFFFECF2C)
          ),
          start = Offset(237.8f, 289.7f),
          end = Offset(177.74f, 104.45f)
        ),
        pathFillType = PathFillType.EvenOdd
      ) {
        moveTo(119.3f, 399.2f)
        curveToRelative(84.3f, 40.3f, 188.3f, 20.4f, 251.2f, -54.5f)
        curveToRelative(74.5f, -88.8f, 62.9f, -221.1f, -25.8f, -295.5f)
        lineToRelative(-59f, 70.3f)
        curveToRelative(69.3f, 58.2f, 78.4f, 161.5f, 20.2f, 230.9f)
        curveToRelative(-46.4f, 55.3f, -122.8f, 73.7f, -186.5f, 48.9f)
      }
      path(
        fill = Brush.linearGradient(
          colorStops = arrayOf(
            0f to Color(0xFFFBC52C),
            0.3f to Color(0xFFF87130),
            0.6f to Color(0xFFEF52E2),
            1f to Color(0xFF661EEC)
          ),
          start = Offset(379.9f, 129.59f),
          end = Offset(243f, 399.77f)
        ),
        pathFillType = PathFillType.EvenOdd
      ) {
        moveTo(119.3f, 399.2f)
        curveToRelative(84.3f, 40.3f, 188.3f, 20.4f, 251.2f, -54.5f)
        curveToRelative(7.7f, -9.2f, 14.5f, -18.8f, 20.3f, -28.8f)
        curveToRelative(9.9f, -61.7f, -11.9f, -126.9f, -63.2f, -169.9f)
        curveToRelative(-13f, -10.9f, -27.2f, -19.8f, -41.9f, -26.5f)
        curveToRelative(69.3f, 58.2f, 78.4f, 161.5f, 20.2f, 230.9f)
        curveToRelative(-46.4f, 55.3f, -122.8f, 73.7f, -186.5f, 48.9f)
      }
      path(
        fill = Brush.linearGradient(
          colorStops = arrayOf(
            0f to Color(0xFF78F6D8),
            0.3f to Color(0xFF77D1F6),
            0.6f to Color(0xFF70A4F3),
            1f to Color(0xFF661EEC)
          ),
          start = Offset(118.21f, 92.5f),
          end = Offset(178.27f, 277.75f)
        ),
        pathFillType = PathFillType.EvenOdd
      ) {
        moveTo(300.3f, 20.4f)
        curveTo(216f, -19.9f, 111.9f, 0f, 49.1f, 74.9f)
        curveToRelative(-74.5f, 88.8f, -62.9f, 221.1f, 25.8f, 295.5f)
        lineToRelative(59f, -70.3f)
        curveToRelative(-69.3f, -58.2f, -78.4f, -161.5f, -20.2f, -230.9f)
        curveTo(160.2f, 14f, 236.6f, -4.5f, 300.3f, 20.4f)
      }
      path(
        fill = Brush.linearGradient(
          colorStops = arrayOf(
            0f to Color(0xFF536EED),
            0.2f to Color(0xFF54C3EC),
            0.6f to Color(0xFF64D769),
            1f to Color(0xFFFECF2C)
          ),
          start = Offset(95.13f, 220.03f),
          end = Offset(232.03f, -50.15f)
        ),
        pathFillType = PathFillType.EvenOdd
      ) {
        moveTo(300.3f, 20.4f)
        curveTo(216f, -19.9f, 111.9f, 0f, 49.1f, 74.9f)
        curveToRelative(-7.7f, 9.2f, -14.5f, 18.8f, -20.3f, 28.8f)
        curveToRelative(-9.9f, 61.7f, 11.9f, 126.9f, 63.2f, 169.9f)
        curveToRelative(13f, 10.9f, 27.2f, 19.8f, 41.9f, 26.5f)
        curveToRelative(-69.3f, -58.2f, -78.4f, -161.5f, -20.2f, -230.9f)
        curveTo(160.2f, 14f, 236.6f, -4.5f, 300.3f, 20.4f)
      }
    }.build()

    return _Swish!!
  }

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.Swish,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}


@Suppress("ObjectPropertyName")
private var _Swish: ImageVector? = null
