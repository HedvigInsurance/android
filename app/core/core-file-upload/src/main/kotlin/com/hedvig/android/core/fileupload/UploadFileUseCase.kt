package com.hedvig.android.core.fileupload

import android.content.ContentResolver
import android.net.Uri
import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.streams.asInput
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface UploadFileUseCase {
  suspend fun invoke(url: String, uri: Uri): Either<ErrorMessage, UploadSuccess>

  suspend fun invoke(url: String, uris: List<Uri>): Either<ErrorMessage, UploadSuccess>
}

internal data class UploadFileUseCaseImpl(
  private val uploadFileService: UploadFileService,
  private val fileService: FileService,
) : UploadFileUseCase {
  override suspend fun invoke(url: String, uri: Uri): Either<ErrorMessage, UploadSuccess> = either {
    val claimId = Uri.parse(url).getQueryParameter("claimId") ?: raise(ErrorMessage("No claim id found in url"))
    val result = either {
      uploadFileService.uploadFile(claimId = claimId, uri = uri)
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
      uploadFileService.uploadFiles(claimId = claimId, uris = uris)
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

internal class UploadFileService(
  private val client: HttpClient,
  private val buildConstants: HedvigBuildConstants,
  private val contentResolver: ContentResolver,
  private val fileService: FileService,
) {
  context(_: Raise<ErrorMessage>)
  suspend fun uploadFile(claimId: String, uri: Uri): List<FileUploadResponse> {
    val fileName = fileService.getFileName(uri) ?: "media"
    val mimeType = fileService.getMimeType(uri)

    logcat { "UploadFileService: Uploading file: $fileName with mimeType: $mimeType for claimId: $claimId" }

    val response = client.post("${buildConstants.urlClaimsService}/api/claim-files/upload?claimId=$claimId") {
      setBody(
        MultiPartFormDataContent(
          formData {
            append(
              "files",
              InputProvider {
                val inputStream = contentResolver.openInputStream(uri)
                  ?: throw Exception("Could not open input stream for uri:$uri")
                inputStream.asInput()
              },
              Headers.build {
                append(HttpHeaders.ContentType, mimeType)
                append(HttpHeaders.ContentDisposition, """filename="$fileName"""")
              },
            )
          },
        ),
      )
    }

    return if (response.status.isSuccess()) {
      val responseBody = response.bodyAsText()
      logcat { "UploadFileService: Upload successful, response: $responseBody" }
      Json.decodeFromString<List<FileUploadResponse>>(responseBody)
    } else {
      val errorBody = response.bodyAsText()
      logcat(LogPriority.ERROR) {
        "UploadFileService failed with status ${response.status}: $errorBody"
      }
      raise(ErrorMessage("File upload failed with status ${response.status}: $errorBody"))
    }
  }

  context(_: Raise<ErrorMessage>)
  suspend fun uploadFiles(claimId: String, uris: List<Uri>): List<FileUploadResponse> {
    logcat { "UploadFileService: Uploading ${uris.size} files for claimId: $claimId" }

    val response = client.post("${buildConstants.urlClaimsService}/api/claim-files/upload?claimId=$claimId") {
      setBody(
        MultiPartFormDataContent(
          formData {
            uris.forEach { uri ->
              val fileName = fileService.getFileName(uri) ?: "media"
              val mimeType = fileService.getMimeType(uri)
              append(
                "files",
                InputProvider {
                  val inputStream = contentResolver.openInputStream(uri)
                    ?: throw Exception("Could not open input stream for uri:$uri")
                  inputStream.asInput()
                },
                Headers.build {
                  append(HttpHeaders.ContentType, mimeType)
                  append(HttpHeaders.ContentDisposition, """filename="$fileName"""")
                },
              )
            }
          },
        ),
      )
    }

    return if (response.status.isSuccess()) {
      val responseBody = response.bodyAsText()
      logcat { "UploadFileService: Upload successful, response: $responseBody" }
      Json.decodeFromString<List<FileUploadResponse>>(responseBody)
    } else {
      val errorBody = response.bodyAsText()
      logcat(LogPriority.ERROR) {
        "UploadFileService failed with status ${response.status}: $errorBody"
      }
      raise(ErrorMessage("File upload failed with status ${response.status}: $errorBody"))
    }
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
