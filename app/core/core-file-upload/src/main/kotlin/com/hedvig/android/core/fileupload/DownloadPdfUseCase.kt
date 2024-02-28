package com.hedvig.android.core.fileupload

import android.content.Context
import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.await
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import java.io.File
import java.io.IOException
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink

private const val FILE_NAME = "hedvig_"
private const val FILE_EXT = ".pdf"

interface DownloadPdfUseCase {
  suspend fun invoke(url: String): Either<ErrorMessage, File>
}

internal class DownloadPdfUseCaseImpl(
  private val context: Context,
  private val clock: Clock,
) : DownloadPdfUseCase {
  override suspend fun invoke(url: String): Either<ErrorMessage, File> = withContext(Dispatchers.IO) {
    either {
      val request = Request.Builder()
        .url(url)
        .build()

      val now = DateTimeFormatter.ISO_DATE_TIME.format(
        clock.now()
          .toLocalDateTime(TimeZone.UTC)
          .toJavaLocalDateTime(),
      )

      val downloadedFile = File(context.filesDir, FILE_NAME + now + FILE_EXT)

      try {
        val response = OkHttpClient().newCall(request).await()
        val buffer = downloadedFile.sink().buffer()
        buffer.writeAll(response.body!!.source())
        buffer.close()

        downloadedFile
      } catch (exception: IOException) {
        logcat(LogPriority.ERROR, exception) { "Could not download pdf" }
        raise(ErrorMessage("Could not download pdf"))
      }
    }
  }
}
