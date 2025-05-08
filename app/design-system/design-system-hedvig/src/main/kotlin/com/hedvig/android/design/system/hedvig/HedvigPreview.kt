package com.hedvig.android.design.system.hedvig

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheSpan
import androidx.media3.datasource.cache.ContentMetadata
import androidx.media3.datasource.cache.ContentMetadataMutations
import coil.ComponentRegistry
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.DefaultRequestOptions
import coil.request.Disposable
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.ImageResult
import java.io.File
import java.util.NavigableSet
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

class PreviewCache() : Cache {
  override fun getUid(): Long = 0.toLong()

  override fun release() {
  }

  override fun addListener(key: String, listener: Cache.Listener): NavigableSet<CacheSpan> {
    return setOf<CacheSpan>() as NavigableSet<CacheSpan>
  }

  override fun removeListener(key: String, listener: Cache.Listener) {}

  override fun getCachedSpans(key: String): NavigableSet<CacheSpan> {
    return setOf<CacheSpan>() as NavigableSet<CacheSpan>
  }

  override fun getKeys(): Set<String> = setOf()

  override fun getCacheSpace(): Long = 0.toLong()

  override fun startReadWrite(key: String, position: Long, length: Long): CacheSpan {
    return CacheSpan("", 0.toLong(), 0.toLong())
  }

  override fun startReadWriteNonBlocking(key: String, position: Long, length: Long): CacheSpan? = null

  override fun startFile(key: String, position: Long, length: Long): File {
    return File("")
  }

  override fun commitFile(file: File, length: Long) {}

  override fun releaseHoleSpan(holeSpan: CacheSpan) {}

  override fun removeResource(key: String) {}

  override fun removeSpan(span: CacheSpan) {}

  override fun isCached(key: String, position: Long, length: Long): Boolean = true

  override fun getCachedLength(key: String, position: Long, length: Long): Long = 0.toLong()

  override fun getCachedBytes(key: String, position: Long, length: Long): Long = 0.toLong()

  override fun applyContentMetadataMutations(key: String, mutations: ContentMetadataMutations) {
  }

  override fun getContentMetadata(key: String): ContentMetadata {
    return object : ContentMetadata {
      override fun get(key: String, defaultValue: ByteArray?): ByteArray? = null

      override fun get(key: String, defaultValue: String?): String? = null

      override fun get(key: String, defaultValue: Long): Long {
        return 0.toLong()
      }

      override fun contains(key: String): Boolean {
        return true
      }
    }
  }
}

@Composable
fun rememberPreviewSimpleCache(): Cache {
  return remember { PreviewCache() }
}

@Preview(
  name = "lightMode portrait",
  uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
)
@Preview(
  name = "nightMode portrait",
  uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
annotation class HedvigPreview

@Preview(
  name = "lightMode landscape",
  locale = "en",
  uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:parent=pixel_5,orientation=landscape",
)
@Preview(
  name = "darkMode landscape",
  locale = "en",
  uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:parent=pixel_5,orientation=landscape",
)
private annotation class HedvigLandscapePreview

@Preview(
  name = "lightMode tablet portrait",
  uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait",
)
@Preview(
  name = "darkMode tablet portrait",
  uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait",
)
private annotation class HedvigTabletPreview

@Preview(
  name = "lightMode tablet landscape",
  uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Preview(
  name = "darkMode tablet landscape",
  uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:width=1280dp,height=800dp,dpi=240",
)
annotation class HedvigTabletLandscapePreview

@Preview(
  name = "lightMode small screen portrait",
  uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:width=300dp,height=240dp,dpi=240,orientation=portrait",
)
annotation class HedvigVerySmallScreenPreview

@HedvigPreview
@HedvigLandscapePreview
@HedvigTabletPreview
@HedvigTabletLandscapePreview
@HedvigVerySmallScreenPreview
annotation class HedvigMultiScreenPreview
