package com.hedvig.feature.claim.chat.data.file

import com.eygraber.uri.Uri

internal fun interface FileService {
  fun convertToCommonFile(uri: Uri): CommonFile
}
