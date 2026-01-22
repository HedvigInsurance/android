package com.hedvig.android.core.fileupload

import android.content.ContentResolver
import android.net.Uri as AndroidUri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUri
import com.hedvig.android.logger.logcat
import java.util.Locale
import kotlin.math.max

class AndroidFileService(
  private val contentResolver: ContentResolver,
) : FileService {

  override fun convertToCommonFile(uri: Uri): CommonFile {
    val androidUri = uri.toAndroidUri()

    return AndroidFile(
      fileName = getFileName(uri) ?: "media",
      mimeType = getMimeType(uri),
      contentResolver = contentResolver,
      androidUri = androidUri,
    )
  }

  override fun getFileName(uri: Uri): String? {
    val androidUri = uri.toAndroidUri()

    if (androidUri.scheme == ContentResolver.SCHEME_CONTENT) {
      val cursor = contentResolver.query(androidUri, null, null, null, null)
      cursor.use { c ->
        if (c?.moveToFirst() == true) {
          val columnIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (columnIndex >= 0) {
            return c.getString(columnIndex)
          }
        }
      }
    }

    val path = androidUri.path
    return path?.substringAfterLast('/') ?: path
  }

  override fun getMimeType(uri: Uri): String {
    val androidUri = uri.toAndroidUri()

    if (androidUri.scheme == ContentResolver.SCHEME_CONTENT) {
      val resolvedMimeType = contentResolver.getType(androidUri)
      if (resolvedMimeType != null) {
        return resolvedMimeType
      }
    }

    return getMimeTypeFromPath(uri.toString())
  }

  override fun isFileSizeWithinBackendLimits(uri: Uri): Boolean {
    val androidUri = uri.toAndroidUri()
    val size = getFileSize(androidUri)
    logcat {
      "FileService: Size of the file: ${size / 1024 / 1024} Mb, " +
        "Backend limit: ${BACKEND_CONTENT_SIZE_LIMIT / 1024 / 1024} Mb"
    }
    return size < BACKEND_CONTENT_SIZE_LIMIT
  }

  private fun getFileSize(androidUri: AndroidUri): Long {
    val statSize = contentResolver.openFileDescriptor(androidUri, "r")?.use {
      it.statSize
    } ?: -1

    val sizeFromCursor = contentResolver.query(
      androidUri,
      arrayOf(OpenableColumns.SIZE),
      null,
      null,
      null,
    )?.use { cursor ->
      val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
      if (cursor.moveToFirst()) cursor.getLong(sizeIndex) else null
    } ?: -1

    logcat { "getFileSize for uri:$androidUri | statSize:$statSize | contentSize:$sizeFromCursor" }
    return max(statSize, sizeFromCursor)
  }

  private fun getMimeTypeFromPath(path: String): String {
    val fileExtension = MimeTypeMap.getFileExtensionFromUrl(path)
    return MimeTypeMap.getSingleton()
      .getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
      ?: ""
  }
}
