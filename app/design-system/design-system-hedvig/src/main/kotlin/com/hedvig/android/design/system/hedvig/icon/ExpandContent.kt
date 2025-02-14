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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.ExpandContent: ImageVector
  get() {
    if (_Expand_content != null) {
      return _Expand_content!!
    }
    _Expand_content = ImageVector.Builder(
      name = "Expand_content",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 960f,
      viewportHeight = 960f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF5F6368)),
        fillAlpha = 1.0f,
        stroke = null,
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(200f, 760f)
        verticalLineToRelative(-240f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(160f)
        horizontalLineToRelative(160f)
        verticalLineToRelative(80f)
        horizontalLineTo(200f)
        close()
        moveToRelative(480f, -320f)
        verticalLineToRelative(-160f)
        horizontalLineTo(520f)
        verticalLineToRelative(-80f)
        horizontalLineToRelative(240f)
        verticalLineToRelative(240f)
        horizontalLineToRelative(-80f)
        close()
      }
    }.build()
    return _Expand_content!!
  }

private var _Expand_content: ImageVector? = null

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.ExpandContent,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}
