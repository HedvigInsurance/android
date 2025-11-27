package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.hedvig.android.design.system.hedvig.placeholder.fade
import com.hedvig.android.design.system.hedvig.placeholder.hedvigPlaceholder
import com.hedvig.android.placeholder.PlaceholderHighlight
import hedvig.resources.Res
import hedvig.resources.FILE_UPLOAD_IMAGE
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileContainer(
  model: Any,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  cacheKey: String? = null,
) {
  val loadedImageIntrinsicSize = remember { mutableStateOf<IntSize?>(null) }
  val placeholderPainter: Painter = rememberShapedColorPainter(HedvigTheme.colorScheme.textSecondary)
  Surface(
    shape = HedvigTheme.shapes.cornerMedium,
    color = Color.Transparent,
    modifier = modifier,
  ) {
    AsyncImage(
      model = ImageRequest.Builder(LocalPlatformContext.current)
        .data(model)
        .apply {
          if (cacheKey != null) {
            diskCacheKey(cacheKey).memoryCacheKey(cacheKey)
          }
        }
        .build(),
      contentDescription = stringResource(Res.string.FILE_UPLOAD_IMAGE),
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
              state.result.image.width,
              state.result.image.height,
            )
            state
          }
        }
      },
      modifier = Modifier
        .height(109.dp)
        .hedvigPlaceholder(
          visible = loadedImageIntrinsicSize.value == null,
          shape = HedvigTheme.shapes.cornerMedium,
          highlight = PlaceholderHighlight.fade(),
        ),
    )
  }
}
