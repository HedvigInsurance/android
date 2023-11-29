package com.hedvig.android.feature.chat.data

import arrow.core.Either
import arrow.retrofit.adapter.either.networkhandling.CallError
import java.io.File
import kotlinx.serialization.Serializable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

internal interface BotServiceService {
  @Multipart
  @POST("files/upload")
  suspend fun uploadFile(
    @Part file: MultipartBody.Part,
  ): Either<CallError, List<FileUploadResponse>>
}

internal suspend fun BotServiceService.uploadFile(
  file: File,
  contentType: MediaType,
): Either<CallError, List<FileUploadResponse>> {
  return uploadFile(
    MultipartBody.Part.createFormData(
      // https://github.com/HedvigInsurance/bot-service/blob/0320fa16ecce5dd2e71f97fff3d02793a6ae032f/src/main/kotlin/com/hedvig/botService/web/FileUploadController.kt#L36
      name = "files",
      filename = file.name,
      body = file.asRequestBody(contentType),
    ),
  )
}

@Serializable
internal data class FileUploadResponse(val uploadToken: String)
