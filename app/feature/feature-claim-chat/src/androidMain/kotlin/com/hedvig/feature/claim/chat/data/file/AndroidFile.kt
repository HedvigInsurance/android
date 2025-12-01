package com.hedvig.feature.claim.chat.data.file

import android.content.ContentResolver
import android.net.Uri as AndroidUri
import android.provider.OpenableColumns
import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUri
import com.hedvig.android.logger.logcat
import kotlin.math.max
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.readByteArray

class AndroidFile(
  override val fileName: String,
  override val description: String,
  private val getSource: () -> Source,
) : CommonFile {
  override fun source(): Source {
    return getSource()
  }

  override fun readBytes(): ByteArray {
    return source().readByteArray()
  }
}

internal class AndroidFileService(
  val contentResolver: ContentResolver,
) : FileService {
  override fun convertToCommonFile(uri: Uri): CommonFile {
    val androidUri = uri.toAndroidUri()
    return AndroidFile(
      fileName = getFileName(androidUri) ?: "media",
      description = "description",
      getSource = {
        requireFileSizeWithinBackendLimits(androidUri)
        contentResolver.openInputStream(androidUri)?.asSource()?.buffered()
          ?: throw IOException("Could not open input stream for uri:$uri")
      },
    )
  }

  private fun getFileName(uri: AndroidUri): String? {
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

  @Throws(IOException::class)
  private fun requireFileSizeWithinBackendLimits(uri: AndroidUri) {
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

  private fun getFileSize(contentResolver: ContentResolver, uri: AndroidUri): Long {
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
}

private class BackendFileLimitException(message: String) : IOException(message)

private const val backendContentSizeLimit = 512 * 1024 * 1024 // 512 Mb
