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
val HedvigIcons.ProfileOutline: ImageVector
  get() {
    val current = _profileOutline
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.ProfileOutline",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M15.25 6.75 C15.25 8.54493 13.7949 10 12 10 C10.2051 10 8.75 8.54493 8.75 6.75 C8.75 4.95507 10.2051 3.5 12 3.5 C13.7949 3.5 15.25 4.95507 15.25 6.75Z M16.75 6.75 C16.75 9.37335 14.6234 11.5 12 11.5 C9.37665 11.5 7.25 9.37335 7.25 6.75 C7.25 4.12665 9.37665 2 12 2 C14.6234 2 16.75 4.12665 16.75 6.75Z M5.5 19.9 C5.5 17.0079 8.31016 14.5 12 14.5 C15.6898 14.5 18.5 17.0079 18.5 19.9 C18.5 20.0535 18.4439 20.1715 18.3086 20.2832 C18.1496 20.4145 17.9128 20.5 17.6889 20.5 H6.31111 C6.08724 20.5 5.85043 20.4145 5.69143 20.2832 C5.55613 20.1715 5.5 20.0535 5.5 19.9Z M4 19.9 C4 15.9426 7.74035 13 12 13 C16.2596 13 20 15.9426 20 19.9 C20 21.2065 18.8066 22 17.6889 22 H6.31111 C5.19335 22 4 21.2065 4 19.9Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 15.25 6.75
        moveTo(x = 15.25f, y = 6.75f)
        // C 15.25 8.54493 13.7949 10 12 10
        curveTo(
          x1 = 15.25f,
          y1 = 8.54493f,
          x2 = 13.7949f,
          y2 = 10.0f,
          x3 = 12.0f,
          y3 = 10.0f,
        )
        // C 10.2051 10 8.75 8.54493 8.75 6.75
        curveTo(
          x1 = 10.2051f,
          y1 = 10.0f,
          x2 = 8.75f,
          y2 = 8.54493f,
          x3 = 8.75f,
          y3 = 6.75f,
        )
        // C 8.75 4.95507 10.2051 3.5 12 3.5
        curveTo(
          x1 = 8.75f,
          y1 = 4.95507f,
          x2 = 10.2051f,
          y2 = 3.5f,
          x3 = 12.0f,
          y3 = 3.5f,
        )
        // C 13.7949 3.5 15.25 4.95507 15.25 6.75z
        curveTo(
          x1 = 13.7949f,
          y1 = 3.5f,
          x2 = 15.25f,
          y2 = 4.95507f,
          x3 = 15.25f,
          y3 = 6.75f,
        )
        close()
        // M 16.75 6.75
        moveTo(x = 16.75f, y = 6.75f)
        // C 16.75 9.37335 14.6234 11.5 12 11.5
        curveTo(
          x1 = 16.75f,
          y1 = 9.37335f,
          x2 = 14.6234f,
          y2 = 11.5f,
          x3 = 12.0f,
          y3 = 11.5f,
        )
        // C 9.37665 11.5 7.25 9.37335 7.25 6.75
        curveTo(
          x1 = 9.37665f,
          y1 = 11.5f,
          x2 = 7.25f,
          y2 = 9.37335f,
          x3 = 7.25f,
          y3 = 6.75f,
        )
        // C 7.25 4.12665 9.37665 2 12 2
        curveTo(
          x1 = 7.25f,
          y1 = 4.12665f,
          x2 = 9.37665f,
          y2 = 2.0f,
          x3 = 12.0f,
          y3 = 2.0f,
        )
        // C 14.6234 2 16.75 4.12665 16.75 6.75z
        curveTo(
          x1 = 14.6234f,
          y1 = 2.0f,
          x2 = 16.75f,
          y2 = 4.12665f,
          x3 = 16.75f,
          y3 = 6.75f,
        )
        close()
        // M 5.5 19.9
        moveTo(x = 5.5f, y = 19.9f)
        // C 5.5 17.0079 8.31016 14.5 12 14.5
        curveTo(
          x1 = 5.5f,
          y1 = 17.0079f,
          x2 = 8.31016f,
          y2 = 14.5f,
          x3 = 12.0f,
          y3 = 14.5f,
        )
        // C 15.6898 14.5 18.5 17.0079 18.5 19.9
        curveTo(
          x1 = 15.6898f,
          y1 = 14.5f,
          x2 = 18.5f,
          y2 = 17.0079f,
          x3 = 18.5f,
          y3 = 19.9f,
        )
        // C 18.5 20.0535 18.4439 20.1715 18.3086 20.2832
        curveTo(
          x1 = 18.5f,
          y1 = 20.0535f,
          x2 = 18.4439f,
          y2 = 20.1715f,
          x3 = 18.3086f,
          y3 = 20.2832f,
        )
        // C 18.1496 20.4145 17.9128 20.5 17.6889 20.5
        curveTo(
          x1 = 18.1496f,
          y1 = 20.4145f,
          x2 = 17.9128f,
          y2 = 20.5f,
          x3 = 17.6889f,
          y3 = 20.5f,
        )
        // H 6.31111
        horizontalLineTo(x = 6.31111f)
        // C 6.08724 20.5 5.85043 20.4145 5.69143 20.2832
        curveTo(
          x1 = 6.08724f,
          y1 = 20.5f,
          x2 = 5.85043f,
          y2 = 20.4145f,
          x3 = 5.69143f,
          y3 = 20.2832f,
        )
        // C 5.55613 20.1715 5.5 20.0535 5.5 19.9z
        curveTo(
          x1 = 5.55613f,
          y1 = 20.1715f,
          x2 = 5.5f,
          y2 = 20.0535f,
          x3 = 5.5f,
          y3 = 19.9f,
        )
        close()
        // M 4 19.9
        moveTo(x = 4.0f, y = 19.9f)
        // C 4 15.9426 7.74035 13 12 13
        curveTo(
          x1 = 4.0f,
          y1 = 15.9426f,
          x2 = 7.74035f,
          y2 = 13.0f,
          x3 = 12.0f,
          y3 = 13.0f,
        )
        // C 16.2596 13 20 15.9426 20 19.9
        curveTo(
          x1 = 16.2596f,
          y1 = 13.0f,
          x2 = 20.0f,
          y2 = 15.9426f,
          x3 = 20.0f,
          y3 = 19.9f,
        )
        // C 20 21.2065 18.8066 22 17.6889 22
        curveTo(
          x1 = 20.0f,
          y1 = 21.2065f,
          x2 = 18.8066f,
          y2 = 22.0f,
          x3 = 17.6889f,
          y3 = 22.0f,
        )
        // H 6.31111
        horizontalLineTo(x = 6.31111f)
        // C 5.19335 22 4 21.2065 4 19.9z
        curveTo(
          x1 = 5.19335f,
          y1 = 22.0f,
          x2 = 4.0f,
          y2 = 21.2065f,
          x3 = 4.0f,
          y3 = 19.9f,
        )
        close()
      }
    }.build().also { _profileOutline = it }
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
        imageVector = HedvigIcons.ProfileOutline,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _profileOutline: ImageVector? = null
