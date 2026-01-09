package com.hedvig.android.core.fileupload

import android.net.Uri
import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
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
  private val claimsServiceUploadFileService: ClaimsServiceUploadFileService,
  private val fileService: FileService,
) : ClaimsServiceUploadFileUseCase {
  override suspend fun invoke(url: String, uri: Uri): Either<ErrorMessage, UploadSuccess> = either {
    val claimId = Uri.parse(url).getQueryParameter("claimId") ?: raise(ErrorMessage("No claim id found in url"))
    val result = either {
      claimsServiceUploadFileService.uploadFile(claimId = claimId, uri = uri)
    }
      .onLeft {
        logcat(LogPriority.ERROR) { "Failed to upload file. Error:$it" }
      }
      .bind()

    handleResult(result)
  }

  override suspend fun invoke(url: String, uris: List<Uri>): Either<ErrorMessage, UploadSuccess> = either {
    val claimId = Uri.parse(url).getQueryParameter("claimId") ?: raise(ErrorMessage("No claim id found in url"))
    val result = either {
      claimsServiceUploadFileService.uploadFiles(claimId = claimId, uris = uris)
    }
      .onLeft {
        logcat(LogPriority.ERROR) { "Failed to upload file. Error:$it" }
      }
      .bind()

    handleResult(result)
  }

  private fun Raise<ErrorMessage>.handleResult(result: List<FileUploadResponse>): UploadSuccess {
    val fileUploadResponse = result.firstOrNull() ?: raise(ErrorMessage("No file upload response"))
    ensureNotNull(fileUploadResponse.file) {
      ErrorMessage("Backend responded with an empty list as a response$result")
    }

    return UploadSuccess(fileIds = result.mapNotNull { it.file.fileId })
  }
}

internal class ClaimsServiceUploadFileService(
  private val fileUploadService: FileUploadService,
  private val buildConstants: HedvigBuildConstants,
) {
  context(_: Raise<ErrorMessage>)
  suspend fun uploadFile(claimId: String, uri: Uri): List<FileUploadResponse> {
    return uploadFiles(claimId = claimId, uris = listOf(uri))
  }

  context(_: Raise<ErrorMessage>)
  suspend fun uploadFiles(claimId: String, uris: List<Uri>): List<FileUploadResponse> {
    return fileUploadService.uploadFiles(
      url = "${buildConstants.urlClaimsService}/api/claim-files/upload?claimId=$claimId",
      uris = uris,
      validateFileSize = false,
      deserializer = { responseBody ->
        Json.decodeFromString<List<FileUploadResponse>>(responseBody)
      },
    )
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
