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
val HedvigIcons.HelipadFilled: ImageVector
  get() {
    val current = _helipadFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.HelipadFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 21.5 C17.2467 21.5 21.5 17.2467 21.5 12 C21.5 6.75329 17.2467 2.5 12 2.5 C6.75329 2.5 2.5 6.75329 2.5 12 C2.5 17.2467 6.75329 21.5 12 21.5Z M9.75 7.5 H8.25 V16.5 H9.75 V12.75 H14.25 V16.5 H15.75 V7.5 H14.25 V11.25 H9.75 V7.5Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 12 21.5
        moveTo(x = 12.0f, y = 21.5f)
        // C 17.2467 21.5 21.5 17.2467 21.5 12
        curveTo(
          x1 = 17.2467f,
          y1 = 21.5f,
          x2 = 21.5f,
          y2 = 17.2467f,
          x3 = 21.5f,
          y3 = 12.0f,
        )
        // C 21.5 6.75329 17.2467 2.5 12 2.5
        curveTo(
          x1 = 21.5f,
          y1 = 6.75329f,
          x2 = 17.2467f,
          y2 = 2.5f,
          x3 = 12.0f,
          y3 = 2.5f,
        )
        // C 6.75329 2.5 2.5 6.75329 2.5 12
        curveTo(
          x1 = 6.75329f,
          y1 = 2.5f,
          x2 = 2.5f,
          y2 = 6.75329f,
          x3 = 2.5f,
          y3 = 12.0f,
        )
        // C 2.5 17.2467 6.75329 21.5 12 21.5z
        curveTo(
          x1 = 2.5f,
          y1 = 17.2467f,
          x2 = 6.75329f,
          y2 = 21.5f,
          x3 = 12.0f,
          y3 = 21.5f,
        )
        close()
        // M 9.75 7.5
        moveTo(x = 9.75f, y = 7.5f)
        // H 8.25
        horizontalLineTo(x = 8.25f)
        // V 16.5
        verticalLineTo(y = 16.5f)
        // H 9.75
        horizontalLineTo(x = 9.75f)
        // V 12.75
        verticalLineTo(y = 12.75f)
        // H 14.25
        horizontalLineTo(x = 14.25f)
        // V 16.5
        verticalLineTo(y = 16.5f)
        // H 15.75
        horizontalLineTo(x = 15.75f)
        // V 7.5
        verticalLineTo(y = 7.5f)
        // H 14.25
        horizontalLineTo(x = 14.25f)
        // V 11.25
        verticalLineTo(y = 11.25f)
        // H 9.75
        horizontalLineTo(x = 9.75f)
        // V 7.5z
        verticalLineTo(y = 7.5f)
        close()
      }
    }.build().also { _helipadFilled = it }
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
        imageVector = HedvigIcons.HelipadFilled,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _helipadFilled: ImageVector? = null
