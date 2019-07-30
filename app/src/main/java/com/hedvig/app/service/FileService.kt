package com.hedvig.app.service

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap

class FileService(
    private val context: Context
) {
    fun getFileName(uri: Uri): String? {
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor?.moveToFirst() == true) {
                    return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }

        val cut = uri.path?.lastIndexOf('/')

        cut?.let { c ->
            if (c != -1) {
                return uri.path?.substring(c)
            }
        }
        return uri.path
    }

    fun getMimeType(uri: Uri): String? {
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            return context.contentResolver.getType(uri)
        }

        return getMimeType(uri.toString())
    }

    fun getMimeType(path: String): String? {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(path)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
    }
}
