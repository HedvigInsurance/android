package com.hedvig.android.feature.imageviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.icon.ArrowLeft
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.mxalbert.zoomable.OverZoomConfig
import com.mxalbert.zoomable.Zoomable
import com.mxalbert.zoomable.rememberZoomableState

@Composable
internal fun ImageViewerDestination(imageLoader: ImageLoader, imageUrl: String, navigateUp: () -> Unit) {
  Box(
    Modifier
      .fillMaxSize()
      .background(Color.Black),
  ) {
    Zoomable(
      state = rememberZoomableState(
        overZoomConfig = OverZoomConfig(1f, 4f),
      ),
      modifier = Modifier.matchParentSize(),
    ) {
      AsyncImage(
        imageLoader = imageLoader,
        model = ImageRequest.Builder(LocalContext.current)
          .data(imageUrl)
          .diskCacheKey(imageUrl)
          .memoryCacheKey(imageUrl)
          .build(),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
      )
    }
    HedvigTheme(darkTheme = true) {
      IconButton(
        onClick = navigateUp,
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
          .padding(horizontal = 4.dp)
          .height(64.dp)
          .wrapContentHeight(),
      ) {
        Icon(
          imageVector = HedvigIcons.ArrowLeft,
          contentDescription = null,
          modifier = Modifier.size(24.dp),
        )
      }
    }
  }
}
