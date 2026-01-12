package com.hedvig.android.core.fileupload

import android.content.ContentResolver
import android.net.Uri
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
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

class FileUploadService(
  private val client: HttpClient,
  private val contentResolver: ContentResolver,
  private val fileService: FileService,
) {
  /**
   * Uploads files to a backend service with optional file size validation.
   *
   * @param url The full URL including any query parameters
   * @param uris List of file URIs to upload
   * @param validateFileSize Whether to validate file sizes before upload (default: false)
   * @return The raw JSON response body as a String
   */
  context(_: Raise<ErrorMessage>)
  suspend fun <T> uploadFiles(
    url: String,
    uris: List<Uri>,
    validateFileSize: Boolean = false,
    deserializer: (String) -> T,
  ): T {
    logcat { "FileUploadService: Uploading ${uris.size} file(s) to $url" }

    // Validate file sizes if requested
    if (validateFileSize) {
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
    }

    val response = client.post(url) {
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
