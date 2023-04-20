package com.hedvig.android.odyssey.step.summary.resources

import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

/**
 * An updated version of [androidx.compose.material.icons.filled.DeviceUnknown] since the material.icons dependency is
 * lacking the latest designs from google fonts website.
 * https://fonts.google.com/icons?selected=Material%20Symbols%20Outlined%3Adevice_unknown%3AFILL%400%3Bwght%40300%3BGRAD%400%3Bopsz%4048
 */
@Suppress("UnusedReceiverParameter")
internal val Icons.Filled.HedvigDeviceUnknown: ImageVector
  get() {
    if (_deviceUnknown != null) {
      return _deviceUnknown!!
    }
    _deviceUnknown = ImageVector.Builder(
      name = "Filled.HedvigDeviceUnknown",
      defaultWidth = 48.0.dp,
      defaultHeight = 48.0.dp,
      viewportWidth = 960.0f,
      viewportHeight = 960.0f,
    ).apply {
      path(fill = SolidColor(Color(0xFF000000))) {
        moveTo(277.69f, 900.0f)
        quadTo(254.16f, 900.0f, 237.08f, 882.92f)
        quadTo(220.0f, 865.84f, 220.0f, 842.31f)
        lineTo(220.0f, 117.69f)
        quadTo(220.0f, 94.16f, 237.08f, 77.08f)
        quadTo(254.16f, 60.0f, 277.69f, 60.0f)
        lineTo(682.31f, 60.0f)
        quadTo(705.84f, 60.0f, 722.92f, 77.08f)
        quadTo(740.0f, 94.16f, 740.0f, 117.69f)
        lineTo(740.0f, 842.31f)
        quadTo(740.0f, 865.84f, 722.92f, 882.92f)
        quadTo(705.84f, 900.0f, 682.31f, 900.0f)
        lineTo(277.69f, 900.0f)
        close()
        moveTo(265.39f, 811.92f)
        lineTo(265.39f, 842.31f)
        quadTo(265.39f, 846.92f, 269.23f, 850.77f)
        quadTo(273.08f, 854.61f, 277.69f, 854.61f)
        lineTo(682.31f, 854.61f)
        quadTo(686.92f, 854.61f, 690.77f, 850.77f)
        quadTo(694.61f, 846.92f, 694.61f, 842.31f)
        lineTo(694.61f, 811.92f)
        lineTo(265.39f, 811.92f)
        close()
        moveTo(265.39f, 766.54f)
        lineTo(694.61f, 766.54f)
        lineTo(694.61f, 193.46f)
        lineTo(265.39f, 193.46f)
        lineTo(265.39f, 766.54f)
        close()
        moveTo(265.39f, 148.08f)
        lineTo(694.61f, 148.08f)
        lineTo(694.61f, 117.69f)
        quadTo(694.61f, 113.08f, 690.77f, 109.23f)
        quadTo(686.92f, 105.38f, 682.31f, 105.38f)
        lineTo(277.69f, 105.38f)
        quadTo(273.08f, 105.38f, 269.23f, 109.23f)
        quadTo(265.39f, 113.08f, 265.39f, 117.69f)
        lineTo(265.39f, 148.08f)
        close()
        moveTo(265.39f, 148.08f)
        lineTo(265.39f, 117.69f)
        quadTo(265.39f, 113.08f, 265.39f, 109.23f)
        quadTo(265.39f, 105.38f, 265.39f, 105.38f)
        lineTo(265.39f, 105.38f)
        quadTo(265.39f, 105.38f, 265.39f, 109.23f)
        quadTo(265.39f, 113.08f, 265.39f, 117.69f)
        lineTo(265.39f, 148.08f)
        lineTo(265.39f, 148.08f)
        close()
        moveTo(265.39f, 811.92f)
        lineTo(265.39f, 811.92f)
        lineTo(265.39f, 842.31f)
        quadTo(265.39f, 846.92f, 265.39f, 850.77f)
        quadTo(265.39f, 854.61f, 265.39f, 854.61f)
        lineTo(265.39f, 854.61f)
        quadTo(265.39f, 854.61f, 265.39f, 850.77f)
        quadTo(265.39f, 846.92f, 265.39f, 842.31f)
        lineTo(265.39f, 811.92f)
        close()
      }
    }.build()
    return _deviceUnknown!!
  }

@Suppress("ObjectPropertyName")
private var _deviceUnknown: ImageVector? = null

@HedvigPreview
@Composable
private fun PreviewHedvigDeviceUnknown() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Icon(Icons.Default.HedvigDeviceUnknown, null)
    }
  }
}
