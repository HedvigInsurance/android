package com.hedvig.feature.claim.chat.data.file

import android.content.ContentResolver
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUri
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.readByteArray
import java.io.File
import java.util.Locale

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

  companion object {
    fun fromFile(file: File, description: String = "File"): AndroidFile {
      return AndroidFile(
        fileName = file.name,
        description = description,
        getSource = { file.inputStream().asSource().buffered() },
      )
    }
  }
}

internal class AndroidFileService(
  private val coreFileService: com.hedvig.android.core.fileupload.FileService,
  private val contentResolver: ContentResolver,
) : FileService {
  override fun convertToCommonFile(uri: Uri): CommonFile {
    val androidUri = uri.toAndroidUri()

    val fileName = coreFileService.getFileName(androidUri) ?: "media"

    return AndroidFile(
      fileName = fileName,
      description = "description",
      getSource = {
        contentResolver.openInputStream(androidUri)?.asSource()?.buffered()
          ?: throw IOException("Could not open input stream for uri:$uri")
      },
    )
  }

  override fun getMimeType(path: String): String {
    val fileExtension = getFileExtension(path)
    return MimeTypeMap.getSingleton()
      .getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
      ?: ""
  }
  private fun getFileExtension(path: String): String = MimeTypeMap.getFileExtensionFromUrl(path)

  override fun getFileName(uriString: String): String? {
    val uri = android.net.Uri.parse(uriString)
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

}
