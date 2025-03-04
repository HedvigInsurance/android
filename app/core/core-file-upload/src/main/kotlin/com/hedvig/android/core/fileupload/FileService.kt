package com.hedvig.android.core.fileupload

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.hedvig.android.logger.logcat
import java.util.Locale
import kotlin.math.max
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.IOException
import okio.source

class FileService(
  private val contentResolver: ContentResolver,
) {
  fun createFormData(uri: Uri): MultipartBody.Part = MultipartBody.Part.createFormData(
    name = "files",
    filename = getFileName(uri) ?: "media",
    body = object : RequestBody() {
      override fun contentType(): MediaType {
        return getMimeType(uri).toMediaType()
      }

      override fun writeTo(sink: BufferedSink) {
        requireFileSizeWithinBackendLimits(uri)
        contentResolver.openInputStream(uri)?.use { inputStream ->
          sink.writeAll(inputStream.source())
        } ?: throw IOException("Could not open input stream for uri:$uri")
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

  @Throws(IOException::class)
  private fun requireFileSizeWithinBackendLimits(uri: Uri) {
    val size = getFileSize(contentResolver, uri)
    logcat {
      "FileService: Size of the file: ${size / 1024 / 1024} Mb, " +
        "Backend limit: ${backendContentSizeLimit / 1024 / 1024} Mb"
    }
    if (size >= backendContentSizeLimit) {
      throw BackendFileLimitException(
        "Failed to upload with uri:$uri. Content size above backend limit:$backendContentSizeLimit",
      )
    }
  }
}

private fun getFileSize(contentResolver: ContentResolver, uri: Uri): Long {
  val statSize = contentResolver.openFileDescriptor(uri, "r")?.use {
    it.statSize
  } ?: -1

  val sizeFromCursor =
    contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.SIZE), null, null, null)?.use { cursor ->
      val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
      if (cursor.moveToFirst()) cursor.getLong(sizeIndex) else null
    } ?: -1
  logcat { "getFileSize for uri:$uri | statSize:$statSize | contentSize:$sizeFromCursor" }
  return max(statSize, sizeFromCursor)
}

class BackendFileLimitException(message: String) : IOException(message)

// TODO Revisit this when and if backend considers adjusting this limit
private const val backendContentSizeLimit = 48 * 1024 * 1024 // 48 Mb
