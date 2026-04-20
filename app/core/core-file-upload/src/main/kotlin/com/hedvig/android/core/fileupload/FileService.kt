package com.hedvig.android.core.fileupload

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.hedvig.android.logger.logcat
import java.util.Locale
import kotlin.math.max
import okio.IOException

class FileService(
  private val contentResolver: ContentResolver,
) {
  fun getFileName(uri: Uri): String? {
    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
      val cursor = contentResolver.query(uri, null, null, null, null)
      cursor.use { c ->
        if (c?.moveToFirst() == true) {
          val columnIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (columnIndex >= 0) {
            return c.getString(columnIndex)
          }
        }
      }
    }

    val cut = uri.path?.lastIndexOf('/')

    cut?.let { c ->
      if (c != -1) {
        return uri.path?.substring(c + 1)
      }
    }
    return uri.path
  }

  fun isFileSizeWithinBackendLimits(uri: Uri): Boolean {
    val size = getFileSize(uri)
    logcat {
      "FileService: Size of the file: ${size / 1024 / 1024} Mb, " +
        "Backend limit: ${backendContentSizeLimit / 1024 / 1024} Mb"
    }
    return size < backendContentSizeLimit
  }

  fun getMimeType(uri: Uri): String {
    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
      val resolvedMimeType = contentResolver.getType(uri)
      if (resolvedMimeType != null) {
        return resolvedMimeType
      }
    }

    return getMimeType(uri.toString())
  }

  private fun getFileSize(uri: Uri): Long {
    val statSize = contentResolver.openFileDescriptor(uri, "r")?.use {
      it.statSize
    } ?: -1

    val sizeFromCursor =
      contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) cursor.getLong(sizeIndex) else null
      } ?: -1
    logcat { "getFileSize for uri:$uri | statSize:$statSize | contentSize:$sizeFromCursor" }
    return max(statSize, sizeFromCursor)
  }

  private fun getMimeType(path: String): String {
    val fileExtension = getFileExtension(path)
    return MimeTypeMap.getSingleton()
      .getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
      ?: ""
  }

  private fun getFileExtension(path: String): String = MimeTypeMap.getFileExtensionFromUrl(path)
}

class BackendFileLimitException(message: String) : IOException(message) {
  constructor(uri: Uri) :
    this("Failed to upload with uri:$uri. Content size above backend limit:$backendContentSizeLimit")
}

private const val backendContentSizeLimit = 512 * 1024 * 1024 // 512 Mb
