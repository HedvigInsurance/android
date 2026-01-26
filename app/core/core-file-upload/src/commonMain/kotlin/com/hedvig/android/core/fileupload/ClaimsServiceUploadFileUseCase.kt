package com.hedvig.android.core.fileupload

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.eygraber.uri.Uri
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface ClaimsServiceUploadFileUseCase {
  suspend fun invoke(url: String, uri: Uri): Either<ErrorMessage, UploadSuccess>

  suspend fun invoke(url: String, uris: List<Uri>): Either<ErrorMessage, UploadSuccess>
}

internal data class ClaimsServiceUploadFileUseCaseImpl(
  private val fileUploadService: FileUploadService,
  private val buildConstants: HedvigBuildConstants,
  private val fileService: FileService,
) : ClaimsServiceUploadFileUseCase {
  override suspend fun invoke(url: String, uri: Uri): Either<ErrorMessage, UploadSuccess> {
    return invoke(url, listOf(uri))
  }

  override suspend fun invoke(url: String, uris: List<Uri>): Either<ErrorMessage, UploadSuccess> = either {
    val claimId = url.substringAfter("claimId=", "").substringBefore("&")
    if (claimId.isEmpty()) {
      raise(ErrorMessage("No claim id found in url"))
    }

    val result = either {
      uploadFiles(claimId = claimId, uris = uris)
    }
      .onLeft {
        logcat(LogPriority.ERROR) { "Failed to upload file. Error:$it" }
      }
      .bind()

    handleResult(result)
  }

  context(_: Raise<ErrorMessage>)
  private suspend fun uploadFiles(claimId: String, uris: List<Uri>): List<FileUploadResponse> {
    // Convert URIs to CommonFiles
    val files = uris.map { uri -> fileService.convertToCommonFile(uri) }

    return fileUploadService.uploadFiles(
      url = "${buildConstants.urlClaimsService}/api/claim-files/upload?claimId=$claimId",
      files = files,
      validateFileSize = false, // Claims service doesn't require file size validation
      deserializer = { responseBody ->
        Json.decodeFromString<List<FileUploadResponse>>(responseBody)
      },
    )
  }

  private fun Raise<ErrorMessage>.handleResult(result: List<FileUploadResponse>): UploadSuccess {
    val fileUploadResponse = result.firstOrNull() ?: raise(ErrorMessage("No file upload response"))
    ensureNotNull(fileUploadResponse.file) {
      ErrorMessage("Backend responded with an empty list as a response$result")
    }

    return UploadSuccess(fileIds = result.mapNotNull { it.file.fileId })
  }
}

@Serializable
internal data class FileUploadResponse(val file: FileResponse, val error: String?)

@Serializable
internal data class FileResponse(
  val fileId: String?,
  val mimeType: String?,
  val name: String?,
  val url: String?,
)

data class UploadSuccess(
  val fileIds: List<String>,
)
