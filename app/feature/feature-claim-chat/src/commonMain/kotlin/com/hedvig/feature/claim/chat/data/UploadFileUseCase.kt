package com.hedvig.feature.claim.chat.data

import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.fileupload.CommonFile
import com.hedvig.android.core.fileupload.FileUploadService
import io.ktor.client.request.forms.InputProvider
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class UploadFileUseCase(
  private val fileUploadService: FileUploadService,
  private val buildConstants: HedvigBuildConstants,
) {
  context(_: Raise<ErrorMessage>)
  suspend fun invoke(commonFile: CommonFile, uploadUrl: String): FileUploadResponse {
    val fullUrl = "${buildConstants.urlHedvigGateway}$uploadUrl"
    val responseBody = fileUploadService.uploadWithCustomFormData(
      url = fullUrl,
      formDataBuilder = {
        commonFile.description?.let { description ->
          append("description", description)
        }
        append(
          "files",
          InputProvider { commonFile.source() },
          Headers.build {
            append(HttpHeaders.ContentType, commonFile.mimeType)
            append(HttpHeaders.ContentDisposition, """filename="${commonFile.fileName}"""")
          },
        )
      },
      deserializer = { it },
    )

    val jsonResponse = Json.parseToJsonElement(responseBody)
    val fileId = jsonResponse
      .jsonObject["fileIds"]
      ?.jsonArray
      ?.map { jsonElement ->
        jsonElement.jsonPrimitive.content
      }
      ?.firstOrNull()
      ?: raise(ErrorMessage("UploadFileUseCase jsonResponse$jsonResponse did not contain file IDs"))
    return FileUploadResponse(CommonFileId(fileId))
  }
}

internal data class FileUploadResponse(
  val fileId: CommonFileId,
)
