package com.hedvig.android.core.fileupload

import com.eygraber.uri.Uri
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.io.File
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class JvmFileService : FileService {
  override fun convertToCommonFile(uri: Uri): CommonFile {
    return JvmFile(
      fileName = getFileName(uri) ?: "media",
      mimeType = getMimeType(uri),
      path = uri.toPathOrNull(),
    )
  }

  override fun getFileName(uri: Uri): String? {
    return uri.path?.substringAfterLast('/')
  }

  override fun getMimeType(uri: Uri): String {
    val probedMimeType = uri.toPathOrNull()
      ?.takeIf { Files.isRegularFile(it) }
      ?.let { runCatching { Files.probeContentType(it) }.getOrNull() }
    return probedMimeType
      ?: getFileName(uri)?.let { URLConnection.guessContentTypeFromName(it) }
      ?: ""
  }

  override fun isFileSizeWithinBackendLimits(uri: Uri): Boolean {
    return sizeOf(uri.toPathOrNull()) < BACKEND_CONTENT_SIZE_LIMIT
  }
}

/** The local file [this] points at, or null for anything but a `file:` URI. */
private fun Uri.toPathOrNull(): Path? {
  if (scheme != "file") return null
  return path?.let { File(it).toPath() }
}

/** The size of the file at [path] in bytes, or -1 when there is no readable file there. */
internal fun sizeOf(path: Path?): Long {
  if (path == null || !Files.isRegularFile(path)) return -1
  return runCatching { Files.size(path) }.getOrDefault(-1)
}
