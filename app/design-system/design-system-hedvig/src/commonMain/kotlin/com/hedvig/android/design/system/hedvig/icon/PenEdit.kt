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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.PenEdit: ImageVector
  get() {
    if (_PenEdit != null) {
      return _PenEdit!!
    }
    _PenEdit = ImageVector.Builder(
      name = "PenEdit",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(10f, 5.5f)
        horizontalLineTo(6.5f)
        curveTo(5.395f, 5.5f, 4.5f, 6.395f, 4.5f, 7.5f)
        verticalLineTo(17.5f)
        curveTo(4.5f, 18.605f, 5.395f, 19.5f, 6.5f, 19.5f)
        horizontalLineTo(16.5f)
        curveTo(17.605f, 19.5f, 18.5f, 18.605f, 18.5f, 17.5f)
        verticalLineTo(14f)
      }
      path(
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(8.49f, 16.085f)
        curveTo(8.152f, 16.141f, 7.859f, 15.848f, 7.915f, 15.509f)
        lineTo(8.203f, 13.782f)
        curveTo(8.237f, 13.576f, 8.335f, 13.387f, 8.482f, 13.239f)
        lineTo(17.066f, 4.656f)
        curveTo(17.673f, 4.048f, 18.661f, 4.048f, 19.268f, 4.656f)
        lineTo(19.344f, 4.732f)
        curveTo(19.952f, 5.339f, 19.952f, 6.327f, 19.344f, 6.934f)
        lineTo(10.761f, 15.518f)
        curveTo(10.613f, 15.665f, 10.424f, 15.763f, 10.218f, 15.797f)
        lineTo(8.49f, 16.085f)
        close()
      }
      path(
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(15.8f, 6.2f)
        lineTo(17.8f, 8.2f)
      }
    }.build()

    return _PenEdit!!
  }

@Suppress("ObjectPropertyName")
private var _PenEdit: ImageVector? = null

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.PenEdit,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}
