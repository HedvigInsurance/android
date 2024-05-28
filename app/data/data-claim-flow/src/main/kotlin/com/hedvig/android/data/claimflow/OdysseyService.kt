package com.hedvig.android.data.claimflow

import arrow.core.Either
import arrow.retrofit.adapter.either.networkhandling.CallError
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface OdysseyService {
  @Multipart
  @POST("{flowId}/audio-recording")
  suspend fun uploadAudioRecordingFile(
    @Path("flowId") flowId: String,
    @Part file: MultipartBody.Part,
  ): Either<CallError, UploadAudioRecordingResult>
}

@Serializable
data class UploadAudioRecordingResult(val audioUrl: String)
