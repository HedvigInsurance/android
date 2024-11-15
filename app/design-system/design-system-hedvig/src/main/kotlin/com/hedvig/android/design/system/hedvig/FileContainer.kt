package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.hedvig.android.design.system.hedvig.placeholder.PlaceholderHighlight
import com.hedvig.android.design.system.hedvig.placeholder.fade
import com.hedvig.android.design.system.hedvig.placeholder.hedvigPlaceholder

@Composable
internal fun FileContainer(
  model: Any,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  cacheKey: String? = null,
) {
  val loadedImageIntrinsicSize = remember { mutableStateOf<IntSize?>(null) }
  val placeholderPainter: Painter = rememberShapedColorPainter(HedvigTheme.colorScheme.textSecondary)
  AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
      .data(model)
      .apply {
        if (cacheKey != null) {
          diskCacheKey(cacheKey).memoryCacheKey(cacheKey)
        }
      }
      .build(),
    contentDescription = null,
    imageLoader = imageLoader,
    contentScale = ContentScale.Crop,
    transform = { state ->
      when (state) {
        is AsyncImagePainter.State.Loading -> {
          state.copy(painter = placeholderPainter)
        }

        is AsyncImagePainter.State.Error -> {
          state
        }

        AsyncImagePainter.State.Empty -> state
        is AsyncImagePainter.State.Success -> {
          loadedImageIntrinsicSize.value = IntSize(
            state.result.drawable.intrinsicWidth,
            state.result.drawable.intrinsicHeight,
          )
          state
        }
      }
    },
    modifier = modifier
      .height(109.dp)
      .hedvigPlaceholder(visible = loadedImageIntrinsicSize.value == null, highlight = PlaceholderHighlight.fade())
      .clip(HedvigTheme.shapes.cornerMedium),
  )
}
