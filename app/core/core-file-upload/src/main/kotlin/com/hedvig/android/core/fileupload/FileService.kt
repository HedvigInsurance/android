package com.hedvig.android.core.fileupload

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import com.hedvig.android.logger.logcat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.IllegalStateException
import java.nio.channels.FileChannel


import java.util.Locale
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.IOException
import okio.buffer
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
        val size = getFileSize(contentResolver, uri)
        val maxMemory = Runtime.getRuntime().maxMemory()
        if (size< maxMemory) {
          contentResolver.openInputStream(uri)?.use { inputStream ->
            val buffer = ByteArray(4 * 1024)
            var bytesRead: Int
            val outputStream = sink.outputStream()
            try {
              while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                outputStream.flush()
              }
            } catch (e: OutOfMemoryError) {
              System.gc()
              throw OutOfMemoryError("Could not open input stream for uri:$uri with OutOfMemoryError: $e")
            }
          } ?: error("Could not open input stream for uri:$uri")
        } else {
          throw OutOfMemoryError("Could not open input stream for uri:$uri not enough memory")
        }
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


fun getFileSize(contentResolver: ContentResolver, uri: Uri): Long {
  return contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.SIZE), null, null, null)?.use { cursor ->
    val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
    if (cursor.moveToFirst()) cursor.getLong(sizeIndex) else -1
  } ?: -1
}
