package com.hedvig.feature.claim.chat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
internal actual fun PlatformBlurContainer(modifier: Modifier, radius: Int, content: @Composable (() -> Unit)) {
  val radiusPx = with(LocalDensity.current) { radius.dp.toPx() }

  Box(
    modifier = modifier.graphicsLayer {
      renderEffect = BlurEffect(
        radiusX = radiusPx,
        radiusY = radiusPx,
        edgeTreatment = TileMode.Decal,
      )
    },
  ) {
    content()
  }
}
