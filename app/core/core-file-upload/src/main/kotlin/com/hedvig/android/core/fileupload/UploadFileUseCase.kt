package com.hedvig.android.core.fileupload

import android.content.ContentResolver
import android.net.Uri
import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.retrofit.adapter.either.networkhandling.CallError
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.retrofit.toErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface UploadFileUseCase {
  suspend fun invoke(url: String, uri: Uri): Either<ErrorMessage, UploadSuccess>

  suspend fun invoke(url: String, uris: List<Uri>): Either<ErrorMessage, UploadSuccess>
}

internal data class UploadFileUseCaseImpl(
  private val uploadFileService: UploadFileService,
  private val fileService: FileService,
  private val contentResolver: ContentResolver,
) : UploadFileUseCase {
  override suspend fun invoke(url: String, uri: Uri): Either<ErrorMessage, UploadSuccess> = either {
    val claimId = Uri.parse(url).getQueryParameter("claimId") ?: raise(ErrorMessage("No claim id found in url"))
    val result = uploadFileService.uploadFile(
      claimId = claimId,
      file = fileService.createFormData(uri),
    )
      .onLeft {
        logcat(LogPriority.ERROR) { "Failed to upload file. Error:$it" }
      }
      .mapLeft(CallError::toErrorMessage)
      .bind()

    handleResult(result)
  }

  override suspend fun invoke(url: String, uris: List<Uri>): Either<ErrorMessage, UploadSuccess> = either {
    val claimId = Uri.parse(url).getQueryParameter("claimId") ?: raise(ErrorMessage("No claim id found in url"))
    val result = uploadFileService.uploadFiles(
      claimId = claimId,
      files = uris.map {
        fileService.createFormData(it)
      },
    )
      .onLeft {
        logcat(LogPriority.ERROR) { "Failed to upload file. Error:$it" }
      }
      .mapLeft(CallError::toErrorMessage)
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

internal interface UploadFileService {
  @Multipart
  @POST("claim-files/upload")
  suspend fun uploadFile(
    @Query("claimId") claimId: String,
    @Part file: MultipartBody.Part,
  ): Either<CallError, List<FileUploadResponse>>

  @Multipart
  @POST("claim-files/upload")
  suspend fun uploadFiles(
    @Query("claimId") claimId: String,
    @Part files: List<MultipartBody.Part>,
  ): Either<CallError, List<FileUploadResponse>>
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
