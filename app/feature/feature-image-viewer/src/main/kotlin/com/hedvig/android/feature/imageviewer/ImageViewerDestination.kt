package com.hedvig.android.feature.imageviewer

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ArrowLeft
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.mxalbert.zoomable.OverZoomConfig
import com.mxalbert.zoomable.Zoomable
import com.mxalbert.zoomable.rememberZoomableState

@Composable
internal fun ImageViewerDestination(
  imageLoader: ImageLoader,
  imageUrl: String,
  cacheKey: String,
  navigateUp: () -> Unit,
) {
  Box(
    Modifier
      .fillMaxSize()
      .background(Color.Black),
  ) {
    val zoomableState = rememberZoomableState(overZoomConfig = OverZoomConfig(1f, 4f))
    Zoomable(
      state = zoomableState,
      modifier = Modifier.matchParentSize(),
    ) {
      AsyncImage(
        imageLoader = imageLoader,
        model = ImageRequest.Builder(LocalContext.current)
          .data(imageUrl)
          .diskCacheKey(cacheKey)
          .memoryCacheKey(cacheKey)
          .build(),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
      )
    }
    HedvigTheme(darkTheme = true) {
      Crossfade(
        targetState = zoomableState.isZooming,
      ) { isZooming ->
        if (!isZooming) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
              .padding(horizontal = 4.dp)
              .fillMaxWidth()
              .height(64.dp),
          ) {
            IconButton(onClick = navigateUp) {
              Icon(
                imageVector = HedvigIcons.ArrowLeft,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
              )
            }
            if (imageUrl.startsWith("https")) {
              Spacer(Modifier.weight(1f))
              IconButton(
                onClick = with(LocalUriHandler.current) { dropUnlessResumed { openUri(imageUrl) } },
              ) {
                Icon(
                  imageVector = HedvigIcons.ArrowNorthEast,
                  contentDescription = null,
                  modifier = Modifier.size(24.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewImageViewerDestination() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ImageViewerDestination(rememberPreviewImageLoader(), "https", "https", {})
    }
  }
}
