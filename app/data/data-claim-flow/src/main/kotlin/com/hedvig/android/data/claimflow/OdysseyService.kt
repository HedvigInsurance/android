package com.hedvig.android.data.claimflow

import arrow.core.raise.Raise
import arrow.core.raise.context.raise
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
import java.io.File
import java.io.FileInputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class OdysseyService(
  private val httpClient: HttpClient,
  private val hedvigBuildConstants: HedvigBuildConstants,
) {
  private val baseUrl: String
    get() = "${hedvigBuildConstants.urlOdyssey}/api/flows/"

  context(_: Raise<ErrorMessage>)
  suspend fun uploadAudioRecordingFile(flowId: String, file: File): UploadAudioRecordingResult {
    logcat { "OdysseyService: Uploading audio file ${file.name} for flowId: $flowId" }

    val response = httpClient.post("$baseUrl$flowId/audio-recording") {
      setBody(
        MultiPartFormDataContent(
          formData {
            append(
              file.name,
              InputProvider {
                FileInputStream(file).asInput()
              },
              Headers.build {
                append(HttpHeaders.ContentType, "audio/mp4")
                append(HttpHeaders.ContentDisposition, """filename="${file.name}"""")
              },
            )
          },
        ),
      )
    }

    return if (response.status.isSuccess()) {
      val responseBody = response.bodyAsText()
      logcat { "OdysseyService: Upload successful, response: $responseBody" }
      Json.decodeFromString<UploadAudioRecordingResult>(responseBody)
    } else {
      val errorBody = response.bodyAsText()
      logcat(LogPriority.ERROR) {
        "OdysseyService failed with status ${response.status}: $errorBody"
      }
      raise(ErrorMessage("Audio file upload failed with status ${response.status}: $errorBody"))
    }
  }
}

@Serializable
data class UploadAudioRecordingResult(val audioUrl: String)
