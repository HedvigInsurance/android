package com.hedvig.feature.claim.chat.data.file

import com.eygraber.uri.Uri

internal interface FileService {
  fun convertToCommonFile(uri: Uri): CommonFile
  fun getMimeType(path: String): String
  fun getFileName(uriString: String): String?
}
