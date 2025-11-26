package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import coil.ComponentRegistry
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.DefaultRequestOptions
import coil.request.Disposable
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.ImageResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

@Preview(
  name = "00_lightMode portrait",
)
@Preview(
  name = "01_nightMode portrait",
)
annotation class HedvigPreview

@Preview(
  name = "lightMode landscape",
  locale = "en",
  device = "spec:parent=pixel_5,orientation=landscape",
)
@Preview(
  name = "darkMode landscape",
  locale = "en",
  device = "spec:parent=pixel_5,orientation=landscape",
)
private annotation class HedvigLandscapePreview

@Preview(
  name = "02_lightMode landscape",
  locale = "en",
  device = "spec:parent=pixel_5,orientation=landscape",
)
private annotation class HedvigOnlyLightLandscapePreview

@Preview(
  name = "lightMode tablet portrait",
  device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait",
)
@Preview(
  name = "darkMode tablet portrait",
  device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait",
)
private annotation class HedvigTabletPreview

@Preview(
  name = "lightMode tablet landscape",
  device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Preview(
  name = "darkMode tablet landscape",
  device = "spec:width=1280dp,height=800dp,dpi=240",
)
annotation class HedvigTabletLandscapePreview

@Preview(
  name = "03_lightMode small screen portrait",
  device = "spec:width=300dp,height=240dp,dpi=240,orientation=portrait",
)
annotation class HedvigVerySmallScreenPreview

@HedvigPreview
@HedvigLandscapePreview
@HedvigTabletPreview
@HedvigTabletLandscapePreview
@HedvigVerySmallScreenPreview
annotation class HedvigMultiScreenPreview

@HedvigPreview
@HedvigOnlyLightLandscapePreview
@HedvigVerySmallScreenPreview
annotation class HedvigShortMultiScreenPreview

/**
 * A fake ImageLoader to be used inside compose @Previews to satisfy the demands of the composables that need it.
 * Do *not* call from production code.
 */
class PreviewImageLoader : ImageLoader {
  override val components: ComponentRegistry = ComponentRegistry()
  override val defaults: DefaultRequestOptions = DefaultRequestOptions()
  override val diskCache: DiskCache? = null
  override val memoryCache: MemoryCache? = null

  override fun enqueue(request: ImageRequest): Disposable {
    return object : Disposable {
      override val isDisposed: Boolean
        get() = true
      override val job: Deferred<ImageResult>
        get() = CompletableDeferred()

      override fun dispose() {}
    }
  }

  override suspend fun execute(request: ImageRequest): ImageResult {
    return ErrorResult(null, request, Throwable())
  }

  override fun newBuilder(): ImageLoader.Builder {
    error("PreviewImageLoader cannot be rebuilt")
  }

  override fun shutdown() {}
}

/**
 * A fake ImageLoader to be used inside compose @Previews to satisfy the demands of the composables that need it.
 * Do *not* call from production code.
 */
@Composable
fun rememberPreviewImageLoader(): PreviewImageLoader {
  return remember { PreviewImageLoader() }
}
