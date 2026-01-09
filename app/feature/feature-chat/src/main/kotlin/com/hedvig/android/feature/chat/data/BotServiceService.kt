package com.hedvig.android.feature.chat.data

import android.content.ContentResolver
import android.net.Uri
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.fileupload.BackendFileLimitException
import com.hedvig.android.core.fileupload.FileService
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

internal class BotServiceService(
  private val client: HttpClient,
  private val buildConstants: HedvigBuildConstants,
  private val contentResolver: ContentResolver,
  private val fileService: FileService,
) {
  context(_: Raise<ErrorMessage>)
  suspend fun uploadFile(uri: Uri): List<FileUploadResponse> {
    return uploadFiles(uris = listOf(uri))
  }

  context(_: Raise<ErrorMessage>)
  suspend fun uploadFiles(uris: List<Uri>): List<FileUploadResponse> {
    logcat { "BotServiceService: Uploading ${uris.size} file(s)" }

    // Check file sizes before uploading
    uris.forEach { uri ->
      if (!fileService.isFileSizeWithinBackendLimits(uri)) {
        raise(
          ErrorMessage(
            "Failed to upload with uri:$uri. Content size above backend limit",
            BackendFileLimitException(uri),
          ),
        )
      }
    }

    val response = client.post("${buildConstants.urlBotService}/api/files/upload") {
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
      logcat { "BotServiceService: Upload successful, response: $responseBody" }
      Json.decodeFromString<List<FileUploadResponse>>(responseBody)
    } else {
      val errorBody = response.bodyAsText()
      logcat(LogPriority.ERROR) {
        "BotServiceService failed with status ${response.status}: $errorBody"
      }
      raise(ErrorMessage("File upload failed with status ${response.status}: $errorBody"))
    }
  }
}

@Serializable
internal data class FileUploadResponse(val uploadToken: String)
