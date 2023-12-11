package com.hedvig.android.feature.chat

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.util.Locale

internal class FileService(
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

  fun getMimeType(uri: Uri): String {
    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
      val resolvedMimeType = contentResolver.getType(uri)
      if (resolvedMimeType != null) {
        return resolvedMimeType
      }
    }

    return getMimeType(uri.toString())
  }

  fun getMimeType(path: String): String {
    val fileExtension = getFileExtension(path)
    return MimeTypeMap.getSingleton()
      .getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
      ?: ""
  }

  fun getFileExtension(path: String): String = MimeTypeMap.getFileExtensionFromUrl(path)
}
