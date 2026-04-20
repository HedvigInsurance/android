package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheSpan
import androidx.media3.datasource.cache.ContentMetadata
import androidx.media3.datasource.cache.ContentMetadataMutations
import java.io.File
import java.util.NavigableSet

@UnstableApi
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
