package com.hedvig.android.feature.chat.data

import arrow.core.raise.Raise
import com.eygraber.uri.Uri
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.fileupload.FileService
import com.hedvig.android.core.fileupload.FileUploadService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class BotServiceService(
  private val fileUploadService: FileUploadService,
  private val fileService: FileService,
  private val buildConstants: HedvigBuildConstants,
) {
  context(_: Raise<ErrorMessage>)
  suspend fun uploadFile(uri: Uri): List<FileUploadResponse> {
    return uploadFiles(uris = listOf(uri))
  }

  context(_: Raise<ErrorMessage>)
  suspend fun uploadFiles(uris: List<Uri>): List<FileUploadResponse> {
    val files = uris.map { uri -> fileService.convertToCommonFile(uri) }
    return fileUploadService.uploadFiles(
      url = "${buildConstants.urlBotService}/api/files/upload",
      files = files,
      validateFileSize = true, // Bot service requires file size validation
      deserializer = { responseBody ->
        Json.decodeFromString<List<FileUploadResponse>>(responseBody)
      },
    )
  }
}

@Serializable
internal data class FileUploadResponse(val uploadToken: String)
