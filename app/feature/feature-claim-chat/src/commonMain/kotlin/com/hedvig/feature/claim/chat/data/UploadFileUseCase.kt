package com.hedvig.feature.claim.chat.data

import com.hedvig.feature.claim.chat.data.file.AudioFileReference
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

internal class UploadAudioUseCase(private val client: HttpClient) {

  suspend fun invoke(
    fileReference: AudioFileReference,
    uploadUrl: String,
  ): String {

    val fileBytes = fileReference.readBytes()

    val response = client.post(uploadUrl) {
      setBody(
        formData {
          append(
            key = "files",
            value = fileBytes,
            headers = Headers.build {
              this.append(HttpHeaders.ContentType, "multipart/form-data")
            },
          )
        },
      )
    }

    return response.status.toString()
  }
}
