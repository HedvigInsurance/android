package com.hedvig.android.feature.travelcertificate.data

import android.os.Environment
import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.await
import com.hedvig.android.feature.travelcertificate.CoInsured
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.TravelCertificateCreateMutation
import octopus.type.TravelCertificateCreateCoInsured
import octopus.type.TravelCertificateCreateInput
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import slimber.log.e

private const val CERTIFICATE_NAME = "hedvigTravelCertificate.pdf"

class DownloadTravelCertificateUseCase() {

  suspend fun invoke(travelCertificateUri: TravelCertificateUrl): Either<ErrorMessage, TravelCertificateUri> =
    withContext(Dispatchers.IO) {
      either {
        val request = Request.Builder()
          .url(travelCertificateUri.uri)
          .build()

        val downloadedFile = File(
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
          CERTIFICATE_NAME,
        )

        try {
          val response = OkHttpClient().newCall(request).await()
          val buffer = downloadedFile.sink().buffer()
          buffer.writeAll(response.body!!.source())
          buffer.close()

          TravelCertificateUri(downloadedFile.absolutePath)
        } catch (exception: IOException) {
          e(exception)
          raise(ErrorMessage("Could not download travel certificate"))
        }
      }
    }
}

@JvmInline
@Serializable
value class TravelCertificateUri(val uri: String)
