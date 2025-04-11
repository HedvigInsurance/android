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
val HedvigIcons.ProfileFilled: ImageVector
  get() {
    val current = _profileFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.ProfileFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 11.5 C14.6234 11.5 16.75 9.37335 16.75 6.75 C16.75 4.12665 14.6234 2 12 2 C9.37665 2 7.25 4.12665 7.25 6.75 C7.25 9.37335 9.37665 11.5 12 11.5Z M12 13 C7.74035 13 4 15.9426 4 19.9 C4 21.2065 5.19335 22 6.31111 22 H17.6889 C18.8066 22 20 21.2065 20 19.9 C20 15.9426 16.2596 13 12 13Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 12 11.5
        moveTo(x = 12.0f, y = 11.5f)
        // C 14.6234 11.5 16.75 9.37335 16.75 6.75
        curveTo(
          x1 = 14.6234f,
          y1 = 11.5f,
          x2 = 16.75f,
          y2 = 9.37335f,
          x3 = 16.75f,
          y3 = 6.75f,
        )
        // C 16.75 4.12665 14.6234 2 12 2
        curveTo(
          x1 = 16.75f,
          y1 = 4.12665f,
          x2 = 14.6234f,
          y2 = 2.0f,
          x3 = 12.0f,
          y3 = 2.0f,
        )
        // C 9.37665 2 7.25 4.12665 7.25 6.75
        curveTo(
          x1 = 9.37665f,
          y1 = 2.0f,
          x2 = 7.25f,
          y2 = 4.12665f,
          x3 = 7.25f,
          y3 = 6.75f,
        )
        // C 7.25 9.37335 9.37665 11.5 12 11.5z
        curveTo(
          x1 = 7.25f,
          y1 = 9.37335f,
          x2 = 9.37665f,
          y2 = 11.5f,
          x3 = 12.0f,
          y3 = 11.5f,
        )
        close()
        // M 12 13
        moveTo(x = 12.0f, y = 13.0f)
        // C 7.74035 13 4 15.9426 4 19.9
        curveTo(
          x1 = 7.74035f,
          y1 = 13.0f,
          x2 = 4.0f,
          y2 = 15.9426f,
          x3 = 4.0f,
          y3 = 19.9f,
        )
        // C 4 21.2065 5.19335 22 6.31111 22
        curveTo(
          x1 = 4.0f,
          y1 = 21.2065f,
          x2 = 5.19335f,
          y2 = 22.0f,
          x3 = 6.31111f,
          y3 = 22.0f,
        )
        // H 17.6889
        horizontalLineTo(x = 17.6889f)
        // C 18.8066 22 20 21.2065 20 19.9
        curveTo(
          x1 = 18.8066f,
          y1 = 22.0f,
          x2 = 20.0f,
          y2 = 21.2065f,
          x3 = 20.0f,
          y3 = 19.9f,
        )
        // C 20 15.9426 16.2596 13 12 13z
        curveTo(
          x1 = 20.0f,
          y1 = 15.9426f,
          x2 = 16.2596f,
          y2 = 13.0f,
          x3 = 12.0f,
          y3 = 13.0f,
        )
        close()
      }
    }.build().also { _profileFilled = it }
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
        imageVector = HedvigIcons.ProfileFilled,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _profileFilled: ImageVector? = null
