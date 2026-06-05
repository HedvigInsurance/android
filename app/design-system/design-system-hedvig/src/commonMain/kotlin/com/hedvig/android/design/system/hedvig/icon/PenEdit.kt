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
      viewportHeight = 24f
    ).apply {
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd
      ) {
        moveTo(18.457f, 10.64f)
        lineTo(13.367f, 5.545f)
        lineTo(14.427f, 4.484f)
        lineTo(19.517f, 9.579f)
        lineTo(18.457f, 10.64f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd
      ) {
        moveTo(15.297f, 3.098f)
        curveTo(16.427f, 1.967f, 18.258f, 1.967f, 19.388f, 3.098f)
        lineTo(20.903f, 4.615f)
        curveTo(22.032f, 5.745f, 22.032f, 7.578f, 20.903f, 8.708f)
        lineTo(9.257f, 20.364f)
        curveTo(9.23f, 20.39f, 9.204f, 20.417f, 9.178f, 20.442f)
        curveTo(8.719f, 20.903f, 8.354f, 21.269f, 7.877f, 21.478f)
        curveTo(7.399f, 21.688f, 6.908f, 21.707f, 6.293f, 21.732f)
        curveTo(6.26f, 21.733f, 6.227f, 21.734f, 6.193f, 21.736f)
        curveTo(4.914f, 21.788f, 3.809f, 21.745f, 3.032f, 20.967f)
        curveTo(2.255f, 20.19f, 2.212f, 19.084f, 2.264f, 17.804f)
        curveTo(2.266f, 17.77f, 2.267f, 17.737f, 2.268f, 17.704f)
        curveTo(2.293f, 17.088f, 2.312f, 16.597f, 2.522f, 16.119f)
        curveTo(2.731f, 15.641f, 3.097f, 15.276f, 3.559f, 14.817f)
        curveTo(3.585f, 14.791f, 3.611f, 14.765f, 3.638f, 14.738f)
        lineTo(15.297f, 3.098f)
        curveTo(15.297f, 3.098f, 15.297f, 3.098f, 15.297f, 3.098f)
        close()
        moveTo(18.327f, 4.158f)
        curveTo(17.783f, 3.614f, 16.901f, 3.614f, 16.358f, 4.158f)
        lineTo(4.697f, 15.8f)
        curveTo(4.12f, 16.376f, 3.977f, 16.535f, 3.895f, 16.722f)
        curveTo(3.813f, 16.909f, 3.794f, 17.103f, 3.763f, 17.865f)
        curveTo(3.708f, 19.216f, 3.837f, 19.651f, 4.093f, 19.907f)
        curveTo(4.348f, 20.163f, 4.782f, 20.292f, 6.132f, 20.237f)
        curveTo(6.893f, 20.206f, 7.087f, 20.187f, 7.274f, 20.105f)
        curveTo(7.46f, 20.023f, 7.619f, 19.881f, 8.196f, 19.304f)
        lineTo(19.842f, 7.648f)
        curveTo(19.842f, 7.648f, 19.842f, 7.648f, 19.842f, 7.648f)
        curveTo(20.386f, 7.103f, 20.386f, 6.22f, 19.842f, 5.675f)
        lineTo(18.327f, 4.158f)
        close()
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
