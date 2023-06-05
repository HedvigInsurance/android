package com.hedvig.android.feature.travelcertificate.data

import android.content.Context
import android.os.Environment
import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import slimber.log.e
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val CERTIFICATE_NAME = "hedvigTravelCertificate_"
private const val FILE_EXT = ".pdf"

internal class DownloadTravelCertificateUseCase(
  private val context: Context,
) {

  suspend fun invoke(travelCertificateUri: TravelCertificateUrl): Either<ErrorMessage, TravelCertificateUri> =
    withContext(Dispatchers.IO) {
      either {
        val request = Request.Builder()
          .url(travelCertificateUri.uri)
          .build()

        val downloadedFile = File(
          context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
          CERTIFICATE_NAME + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + FILE_EXT,
        )

        try {
          val response = OkHttpClient().newCall(request).await()
          val buffer = downloadedFile.sink().buffer()
          buffer.writeAll(response.body!!.source())
          buffer.close()

          TravelCertificateUri(downloadedFile)
        } catch (exception: IOException) {
          e(exception)
          raise(ErrorMessage("Could not download travel certificate"))
        }
      }
    }
}

@JvmInline
internal value class TravelCertificateUri(val uri: File)
