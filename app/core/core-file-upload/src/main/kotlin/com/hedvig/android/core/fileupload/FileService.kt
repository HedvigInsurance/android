package com.hedvig.android.core.fileupload

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.util.Locale
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

class FileService(
  private val contentResolver: ContentResolver,
) {
  fun createFormData(uri: Uri) = MultipartBody.Part.createFormData(
    name = "files",
    filename = getFileName(uri) ?: "media",
    body = object : RequestBody() {
      override fun contentType(): MediaType {
        return getMimeType(uri).toMediaType()
      }

      override fun writeTo(sink: BufferedSink) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
          sink.writeAll(inputStream.source())
        } ?: error("Could not open input stream for uri:$uri")
      }
    },
  )

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

  private fun getMimeType(path: String): String {
    val fileExtension = getFileExtension(path)
    return MimeTypeMap.getSingleton()
      .getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
      ?: ""
  }

  private fun getFileExtension(path: String): String = MimeTypeMap.getFileExtensionFromUrl(path)
}
