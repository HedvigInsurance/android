package com.hedvig.android.design.system.hedvig.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.ID: ImageVector
  get() {
    val current = _iD
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.ID",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M21.75 8 C21.75 6.48122 20.5188 5.25 19 5.25 H5 C3.48122 5.25 2.25 6.48122 2.25 8 V16 C2.25 17.5188 3.48122 18.75 5 18.75 L19 18.75 C20.5188 18.75 21.75 17.5188 21.75 16 V8Z M19 6.75 C19.6904 6.75 20.25 7.30964 20.25 8 V16 C20.25 16.6904 19.6904 17.25 19 17.25 L5 17.25 C4.30964 17.25 3.75 16.6904 3.75 16 L3.75 8 C3.75 7.30964 4.30964 6.75 5 6.75 L19 6.75Z M7.5 11.5 C8.32843 11.5 9 10.8284 9 10 C9 9.17157 8.32843 8.5 7.5 8.5 C6.67157 8.5 6 9.17157 6 10 C6 10.8284 6.67157 11.5 7.5 11.5Z M5.85299 12.9801 C6.3065 12.6683 6.89058 12.5 7.5 12.5 C8.10942 12.5 8.6935 12.6683 9.14701 12.9801 C9.59091 13.2853 10 13.8029 10 14.5 C10 14.9209 9.75372 15.1886 9.57304 15.3128 C9.38275 15.4436 9.1623 15.5 8.95455 15.5 H6.04545 C5.8377 15.5 5.61725 15.4436 5.42696 15.3128 C5.24629 15.1886 5 14.9209 5 14.5 C5 13.8029 5.40909 13.2853 5.85299 12.9801Z M12 8.75 C11.5858 8.75 11.25 9.08579 11.25 9.5 C11.25 9.91421 11.5858 10.25 12 10.25 H18 C18.4142 10.25 18.75 9.91421 18.75 9.5 C18.75 9.08579 18.4142 8.75 18 8.75 H12Z M11.25 12 C11.25 11.5858 11.5858 11.25 12 11.25 H18 C18.4142 11.25 18.75 11.5858 18.75 12 C18.75 12.4142 18.4142 12.75 18 12.75 H12 C11.5858 12.75 11.25 12.4142 11.25 12Z M15.5 13.75 C15.0858 13.75 14.75 14.0858 14.75 14.5 C14.75 14.9142 15.0858 15.25 15.5 15.25 H18 C18.4142 15.25 18.75 14.9142 18.75 14.5 C18.75 14.0858 18.4142 13.75 18 13.75 H15.5Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 21.75 8
        moveTo(x = 21.75f, y = 8.0f)
        // C 21.75 6.48122 20.5188 5.25 19 5.25
        curveTo(
          x1 = 21.75f,
          y1 = 6.48122f,
          x2 = 20.5188f,
          y2 = 5.25f,
          x3 = 19.0f,
          y3 = 5.25f,
        )
        // H 5
        horizontalLineTo(x = 5.0f)
        // C 3.48122 5.25 2.25 6.48122 2.25 8
        curveTo(
          x1 = 3.48122f,
          y1 = 5.25f,
          x2 = 2.25f,
          y2 = 6.48122f,
          x3 = 2.25f,
          y3 = 8.0f,
        )
        // V 16
        verticalLineTo(y = 16.0f)
        // C 2.25 17.5188 3.48122 18.75 5 18.75
        curveTo(
          x1 = 2.25f,
          y1 = 17.5188f,
          x2 = 3.48122f,
          y2 = 18.75f,
          x3 = 5.0f,
          y3 = 18.75f,
        )
        // L 19 18.75
        lineTo(x = 19.0f, y = 18.75f)
        // C 20.5188 18.75 21.75 17.5188 21.75 16
        curveTo(
          x1 = 20.5188f,
          y1 = 18.75f,
          x2 = 21.75f,
          y2 = 17.5188f,
          x3 = 21.75f,
          y3 = 16.0f,
        )
        // V 8z
        verticalLineTo(y = 8.0f)
        close()
        // M 19 6.75
        moveTo(x = 19.0f, y = 6.75f)
        // C 19.6904 6.75 20.25 7.30964 20.25 8
        curveTo(
          x1 = 19.6904f,
          y1 = 6.75f,
          x2 = 20.25f,
          y2 = 7.30964f,
          x3 = 20.25f,
          y3 = 8.0f,
        )
        // V 16
        verticalLineTo(y = 16.0f)
        // C 20.25 16.6904 19.6904 17.25 19 17.25
        curveTo(
          x1 = 20.25f,
          y1 = 16.6904f,
          x2 = 19.6904f,
          y2 = 17.25f,
          x3 = 19.0f,
          y3 = 17.25f,
        )
        // L 5 17.25
        lineTo(x = 5.0f, y = 17.25f)
        // C 4.30964 17.25 3.75 16.6904 3.75 16
        curveTo(
          x1 = 4.30964f,
          y1 = 17.25f,
          x2 = 3.75f,
          y2 = 16.6904f,
          x3 = 3.75f,
          y3 = 16.0f,
        )
        // L 3.75 8
        lineTo(x = 3.75f, y = 8.0f)
        // C 3.75 7.30964 4.30964 6.75 5 6.75
        curveTo(
          x1 = 3.75f,
          y1 = 7.30964f,
          x2 = 4.30964f,
          y2 = 6.75f,
          x3 = 5.0f,
          y3 = 6.75f,
        )
        // L 19 6.75z
        lineTo(x = 19.0f, y = 6.75f)
        close()
        // M 7.5 11.5
        moveTo(x = 7.5f, y = 11.5f)
        // C 8.32843 11.5 9 10.8284 9 10
        curveTo(
          x1 = 8.32843f,
          y1 = 11.5f,
          x2 = 9.0f,
          y2 = 10.8284f,
          x3 = 9.0f,
          y3 = 10.0f,
        )
        // C 9 9.17157 8.32843 8.5 7.5 8.5
        curveTo(
          x1 = 9.0f,
          y1 = 9.17157f,
          x2 = 8.32843f,
          y2 = 8.5f,
          x3 = 7.5f,
          y3 = 8.5f,
        )
        // C 6.67157 8.5 6 9.17157 6 10
        curveTo(
          x1 = 6.67157f,
          y1 = 8.5f,
          x2 = 6.0f,
          y2 = 9.17157f,
          x3 = 6.0f,
          y3 = 10.0f,
        )
        // C 6 10.8284 6.67157 11.5 7.5 11.5z
        curveTo(
          x1 = 6.0f,
          y1 = 10.8284f,
          x2 = 6.67157f,
          y2 = 11.5f,
          x3 = 7.5f,
          y3 = 11.5f,
        )
        close()
        // M 5.85299 12.9801
        moveTo(x = 5.85299f, y = 12.9801f)
        // C 6.3065 12.6683 6.89058 12.5 7.5 12.5
        curveTo(
          x1 = 6.3065f,
          y1 = 12.6683f,
          x2 = 6.89058f,
          y2 = 12.5f,
          x3 = 7.5f,
          y3 = 12.5f,
        )
        // C 8.10942 12.5 8.6935 12.6683 9.14701 12.9801
        curveTo(
          x1 = 8.10942f,
          y1 = 12.5f,
          x2 = 8.6935f,
          y2 = 12.6683f,
          x3 = 9.14701f,
          y3 = 12.9801f,
        )
        // C 9.59091 13.2853 10 13.8029 10 14.5
        curveTo(
          x1 = 9.59091f,
          y1 = 13.2853f,
          x2 = 10.0f,
          y2 = 13.8029f,
          x3 = 10.0f,
          y3 = 14.5f,
        )
        // C 10 14.9209 9.75372 15.1886 9.57304 15.3128
        curveTo(
          x1 = 10.0f,
          y1 = 14.9209f,
          x2 = 9.75372f,
          y2 = 15.1886f,
          x3 = 9.57304f,
          y3 = 15.3128f,
        )
        // C 9.38275 15.4436 9.1623 15.5 8.95455 15.5
        curveTo(
          x1 = 9.38275f,
          y1 = 15.4436f,
          x2 = 9.1623f,
          y2 = 15.5f,
          x3 = 8.95455f,
          y3 = 15.5f,
        )
        // H 6.04545
        horizontalLineTo(x = 6.04545f)
        // C 5.8377 15.5 5.61725 15.4436 5.42696 15.3128
        curveTo(
          x1 = 5.8377f,
          y1 = 15.5f,
          x2 = 5.61725f,
          y2 = 15.4436f,
          x3 = 5.42696f,
          y3 = 15.3128f,
        )
        // C 5.24629 15.1886 5 14.9209 5 14.5
        curveTo(
          x1 = 5.24629f,
          y1 = 15.1886f,
          x2 = 5.0f,
          y2 = 14.9209f,
          x3 = 5.0f,
          y3 = 14.5f,
        )
        // C 5 13.8029 5.40909 13.2853 5.85299 12.9801z
        curveTo(
          x1 = 5.0f,
          y1 = 13.8029f,
          x2 = 5.40909f,
          y2 = 13.2853f,
          x3 = 5.85299f,
          y3 = 12.9801f,
        )
        close()
        // M 12 8.75
        moveTo(x = 12.0f, y = 8.75f)
        // C 11.5858 8.75 11.25 9.08579 11.25 9.5
        curveTo(
          x1 = 11.5858f,
          y1 = 8.75f,
          x2 = 11.25f,
          y2 = 9.08579f,
          x3 = 11.25f,
          y3 = 9.5f,
        )
        // C 11.25 9.91421 11.5858 10.25 12 10.25
        curveTo(
          x1 = 11.25f,
          y1 = 9.91421f,
          x2 = 11.5858f,
          y2 = 10.25f,
          x3 = 12.0f,
          y3 = 10.25f,
        )
        // H 18
        horizontalLineTo(x = 18.0f)
        // C 18.4142 10.25 18.75 9.91421 18.75 9.5
        curveTo(
          x1 = 18.4142f,
          y1 = 10.25f,
          x2 = 18.75f,
          y2 = 9.91421f,
          x3 = 18.75f,
          y3 = 9.5f,
        )
        // C 18.75 9.08579 18.4142 8.75 18 8.75
        curveTo(
          x1 = 18.75f,
          y1 = 9.08579f,
          x2 = 18.4142f,
          y2 = 8.75f,
          x3 = 18.0f,
          y3 = 8.75f,
        )
        // H 12z
        horizontalLineTo(x = 12.0f)
        close()
        // M 11.25 12
        moveTo(x = 11.25f, y = 12.0f)
        // C 11.25 11.5858 11.5858 11.25 12 11.25
        curveTo(
          x1 = 11.25f,
          y1 = 11.5858f,
          x2 = 11.5858f,
          y2 = 11.25f,
          x3 = 12.0f,
          y3 = 11.25f,
        )
        // H 18
        horizontalLineTo(x = 18.0f)
        // C 18.4142 11.25 18.75 11.5858 18.75 12
        curveTo(
          x1 = 18.4142f,
          y1 = 11.25f,
          x2 = 18.75f,
          y2 = 11.5858f,
          x3 = 18.75f,
          y3 = 12.0f,
        )
        // C 18.75 12.4142 18.4142 12.75 18 12.75
        curveTo(
          x1 = 18.75f,
          y1 = 12.4142f,
          x2 = 18.4142f,
          y2 = 12.75f,
          x3 = 18.0f,
          y3 = 12.75f,
        )
        // H 12
        horizontalLineTo(x = 12.0f)
        // C 11.5858 12.75 11.25 12.4142 11.25 12z
        curveTo(
          x1 = 11.5858f,
          y1 = 12.75f,
          x2 = 11.25f,
          y2 = 12.4142f,
          x3 = 11.25f,
          y3 = 12.0f,
        )
        close()
        // M 15.5 13.75
        moveTo(x = 15.5f, y = 13.75f)
        // C 15.0858 13.75 14.75 14.0858 14.75 14.5
        curveTo(
          x1 = 15.0858f,
          y1 = 13.75f,
          x2 = 14.75f,
          y2 = 14.0858f,
          x3 = 14.75f,
          y3 = 14.5f,
        )
        // C 14.75 14.9142 15.0858 15.25 15.5 15.25
        curveTo(
          x1 = 14.75f,
          y1 = 14.9142f,
          x2 = 15.0858f,
          y2 = 15.25f,
          x3 = 15.5f,
          y3 = 15.25f,
        )
        // H 18
        horizontalLineTo(x = 18.0f)
        // C 18.4142 15.25 18.75 14.9142 18.75 14.5
        curveTo(
          x1 = 18.4142f,
          y1 = 15.25f,
          x2 = 18.75f,
          y2 = 14.9142f,
          x3 = 18.75f,
          y3 = 14.5f,
        )
        // C 18.75 14.0858 18.4142 13.75 18 13.75
        curveTo(
          x1 = 18.75f,
          y1 = 14.0858f,
          x2 = 18.4142f,
          y2 = 13.75f,
          x3 = 18.0f,
          y3 = 13.75f,
        )
        // H 15.5z
        horizontalLineTo(x = 15.5f)
        close()
      }
    }.build().also { _iD = it }
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
        imageVector = HedvigIcons.ID,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _iD: ImageVector? = null
