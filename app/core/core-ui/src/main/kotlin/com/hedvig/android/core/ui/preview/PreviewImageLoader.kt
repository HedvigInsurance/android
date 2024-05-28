package com.hedvig.android.core.ui.preview

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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

/**
 * A fake ImageLoader to be used inside compose @Previews to satisfy the demands of the composables that need it.
 * Do *not* call from production code.
 */
class PreviewImageLoader(private val context: Context) : ImageLoader {
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
    return ImageLoader.Builder(context)
  }

  override fun shutdown() {}
}

/**
 * A fake ImageLoader to be used inside compose @Previews to satisfy the demands of the composables that need it.
 * Do *not* call from production code.
 */
@Composable
fun rememberPreviewImageLoader(): PreviewImageLoader {
  val context = LocalContext.current
  return remember { PreviewImageLoader(context) }
}
