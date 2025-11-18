package com.hedvig.feature.claim.chat.data

import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import com.hedvig.feature.claim.chat.data.file.AudioFileReference
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class UploadAudioUseCase(private val client: HttpClient) {
  context(_: Raise<ErrorMessage>)
  suspend fun invoke(
    fileReference: AudioFileReference,
    uploadUrl: String,
  ): FileUploadResponse {
    // todo URL for prod/staging
    val response = client.post("https://gateway.test.hedvig.com$uploadUrl") {
      setBody(
        MultiPartFormDataContent(
          formData {
            append("description", "Audio File")
            append(
              "audio",
              InputProvider { fileReference.source() },
              Headers.build {
                append(HttpHeaders.ContentType, "multipart/form-data")
                append(HttpHeaders.ContentDisposition, """filename="${fileReference.fileName}"""")
              },
            )
          },
        ),
      )
      onUpload { bytesSentTotal, contentLength ->
        logcat { "UploadAudioUseCase bytesSentTotal:$bytesSentTotal contentLength:$contentLength" }
      }
    }

    return if (response.status.isSuccess()) {
      val jsonResponse = Json.encodeToJsonElement(response.bodyAsText())
      val fileId = jsonResponse
        .jsonObject["fileIds"]
        ?.jsonArray
        ?.map { jsonElement ->
          jsonElement.jsonPrimitive.content
        }
        ?.firstOrNull()
        ?: raise(ErrorMessage("UploadAudioUseCase jsonResponse$jsonResponse did not contain file IDs"))
      FileUploadResponse(AudioFileId(fileId))
    } else {
      raise(ErrorMessage("UploadAudioUseCase failed with error ${response.status} | ${response.bodyAsText()}"))
    }
  }
}

internal data class FileUploadResponse(
  val fileId: AudioFileId,
)
