package com.hedvig.android.core.fileupload

import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.network.clients.NetworkError
import com.hedvig.android.network.clients.safePost
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

class FileUploadService(
  private val client: HttpClient,
) {
  /**
   * Uploads files to a backend service with optional file size validation.
   *
   * @param url The full URL including any query parameters
   * @param files List of CommonFiles to upload
   * @param validateFileSize Whether to validate file sizes before upload (default: false)
   * @param deserializer Function to deserialize the response body
   * @return The deserialized response
   */
  context(_: Raise<ErrorMessage>)
  suspend fun <T> uploadFiles(
    url: String,
    files: List<CommonFile>,
    validateFileSize: Boolean = false,
    deserializer: (String) -> T,
  ): T {
    logcat { "FileUploadService: Uploading ${files.size} file(s) to $url" }

    // Validate file sizes if requested
    if (validateFileSize) {
      files.forEach { file ->
        val size = file.getSize()
        if (size >= BACKEND_CONTENT_SIZE_LIMIT) {
          raise(
            ErrorMessage(
              "File ${file.fileName} exceeds backend size limit",
              BackendFileLimitException(file.fileName, BACKEND_CONTENT_SIZE_LIMIT),
            ),
          )
        }
      }
    }

    return uploadWithCustomFormData(
      url = url,
      formDataBuilder = {
        files.forEach { file ->
          append(
            "files",
            InputProvider { file.source() },
            Headers.build {
              append(HttpHeaders.ContentType, file.mimeType)
              append(HttpHeaders.ContentDisposition, """filename="${file.fileName}"""")
            },
          )
        }
      },
      deserializer = deserializer,
    )
  }

  /**
   * Uploads a file with custom form data builder.
   *
   * @param url The full URL including any query parameters
   * @param formDataBuilder Lambda to build custom form data with files and additional fields
   * @param deserializer Function to deserialize the response body
   * @return The deserialized response
   */
  context(_: Raise<ErrorMessage>)
  suspend fun <T> uploadWithCustomFormData(
    url: String,
    formDataBuilder: FormBuilder.() -> Unit,
    deserializer: (String) -> T,
  ): T {
    logcat { "FileUploadService: Uploading to $url with custom form data" }

    val response: HttpResponse = client
      .safePost(url) {
        setBody(MultiPartFormDataContent(formData(formDataBuilder)))
      }
      .fold(
        ifLeft = { error ->
          raise(
            when (error) {
              is NetworkError.IOError -> ErrorMessage("Network error: ${error.message}", error.throwable)
              is NetworkError.UnknownError -> ErrorMessage(error.message, error.throwable)
            },
          )
        },
        ifRight = { it },
      )

    return if (response.status.isSuccess()) {
      val responseBody = response.bodyAsText()
      logcat { "FileUploadService: Upload successful, response: $responseBody" }
      deserializer(responseBody)
    } else {
      val errorBody = response.bodyAsText()
      logcat(LogPriority.ERROR) {
        "FileUploadService failed with status ${response.status}: $errorBody"
      }
      raise(ErrorMessage("File upload failed with status ${response.status}: $errorBody"))
    }
  }
}
