package com.hedvig.android.core.fileupload

import android.content.ContentResolver
import android.net.Uri as AndroidUri
import android.provider.OpenableColumns
import kotlin.math.max
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered

class AndroidFile(
  override val fileName: String,
  override val mimeType: String,
  private val contentResolver: ContentResolver,
  private val androidUri: AndroidUri,
) : CommonFile {
  override fun source(): Source {
    val inputStream = contentResolver.openInputStream(androidUri)
      ?: throw IOException("Could not open input stream for uri:$androidUri")
    return inputStream.asSource().buffered()
  }

  override fun getSize(): Long {
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

    return max(statSize, sizeFromCursor)
  }
}
