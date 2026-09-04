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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.DocumentPlus: ImageVector
  get() {
    if (_DocumentPlus != null) {
      return _DocumentPlus!!
    }
    _DocumentPlus = ImageVector.Builder(
      name = "DocumentPlus",
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
        moveTo(11f, 5f)
        lineTo(7f, 5f)
        curveTo(5.895f, 5f, 5f, 5.895f, 5f, 7f)
        lineTo(5f, 19f)
        curveTo(5f, 20.105f, 5.895f, 21f, 7f, 21f)
        horizontalLineTo(15f)
        curveTo(16.105f, 21f, 17f, 20.105f, 17f, 19f)
        verticalLineTo(13f)
        verticalLineTo(11f)
      }
      path(
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(14f, 11.5f)
        lineTo(8f, 11.5f)
      }
      path(
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(14f, 9f)
        lineTo(8f, 9f)
      }
      path(
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(14f, 14f)
        lineTo(8f, 14f)
      }
      path(
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(10.5f, 16.5f)
        horizontalLineTo(8f)
      }
      path(
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(17f, 2f)
        lineTo(17f, 8f)
      }
      path(
        stroke = SolidColor(Color(0xFF121212)),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(20f, 5f)
        lineTo(14f, 5f)
      }
    }.build()

    return _DocumentPlus!!
  }

@Suppress("ObjectPropertyName")
private var _DocumentPlus: ImageVector? = null

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.DocumentPlus,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}
