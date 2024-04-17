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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val HedvigIcons.HelipadOutline: ImageVector
  get() {
    val current = _helipadOutline
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.HelipadOutline",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M20 12 C20 16.4183 16.4183 20 12 20 C7.58172 20 4 16.4183 4 12 C4 7.58172 7.58172 4 12 4 C16.4183 4 20 7.58172 20 12Z M21.5 12 C21.5 17.2467 17.2467 21.5 12 21.5 C6.75329 21.5 2.5 17.2467 2.5 12 C2.5 6.75329 6.75329 2.5 12 2.5 C17.2467 2.5 21.5 6.75329 21.5 12Z M9.75 7.5 H8.25 V16.5 H9.75 V12.75 H14.25 V16.5 H15.75 V7.5 H14.25 V11.25 H9.75 V7.5Z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 20 12
        moveTo(x = 20.0f, y = 12.0f)
        // C 20 16.4183 16.4183 20 12 20
        curveTo(
          x1 = 20.0f,
          y1 = 16.4183f,
          x2 = 16.4183f,
          y2 = 20.0f,
          x3 = 12.0f,
          y3 = 20.0f,
        )
        // C 7.58172 20 4 16.4183 4 12
        curveTo(
          x1 = 7.58172f,
          y1 = 20.0f,
          x2 = 4.0f,
          y2 = 16.4183f,
          x3 = 4.0f,
          y3 = 12.0f,
        )
        // C 4 7.58172 7.58172 4 12 4
        curveTo(
          x1 = 4.0f,
          y1 = 7.58172f,
          x2 = 7.58172f,
          y2 = 4.0f,
          x3 = 12.0f,
          y3 = 4.0f,
        )
        // C 16.4183 4 20 7.58172 20 12z
        curveTo(
          x1 = 16.4183f,
          y1 = 4.0f,
          x2 = 20.0f,
          y2 = 7.58172f,
          x3 = 20.0f,
          y3 = 12.0f,
        )
        close()
        // M 21.5 12
        moveTo(x = 21.5f, y = 12.0f)
        // C 21.5 17.2467 17.2467 21.5 12 21.5
        curveTo(
          x1 = 21.5f,
          y1 = 17.2467f,
          x2 = 17.2467f,
          y2 = 21.5f,
          x3 = 12.0f,
          y3 = 21.5f,
        )
        // C 6.75329 21.5 2.5 17.2467 2.5 12
        curveTo(
          x1 = 6.75329f,
          y1 = 21.5f,
          x2 = 2.5f,
          y2 = 17.2467f,
          x3 = 2.5f,
          y3 = 12.0f,
        )
        // C 2.5 6.75329 6.75329 2.5 12 2.5
        curveTo(
          x1 = 2.5f,
          y1 = 6.75329f,
          x2 = 6.75329f,
          y2 = 2.5f,
          x3 = 12.0f,
          y3 = 2.5f,
        )
        // C 17.2467 2.5 21.5 6.75329 21.5 12z
        curveTo(
          x1 = 17.2467f,
          y1 = 2.5f,
          x2 = 21.5f,
          y2 = 6.75329f,
          x3 = 21.5f,
          y3 = 12.0f,
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
    }.build().also { _helipadOutline = it }
  }

@Preview
@Composable
private fun IconPreview() {
  com.hedvig.android.design.system.hedvig.HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.HelipadOutline,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _helipadOutline: ImageVector? = null
