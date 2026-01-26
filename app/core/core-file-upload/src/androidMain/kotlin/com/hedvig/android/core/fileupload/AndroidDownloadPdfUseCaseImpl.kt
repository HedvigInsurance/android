package com.hedvig.android.core.fileupload

import android.content.Context
import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import java.io.File
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import okio.buffer
import okio.sink

private const val FILE_NAME = "hedvig_"
private const val FILE_EXT = ".pdf"

internal class AndroidDownloadPdfUseCaseImpl(
  private val context: Context,
  private val clock: Clock,
  private val httpClient: HttpClient,
) : DownloadPdfUseCase {
  override suspend fun invoke(url: String): Either<ErrorMessage, DownloadedFile> = withContext(Dispatchers.IO) {
    either {
      try {
        val now = DateTimeFormatter.ISO_DATE_TIME.format(
          clock.now()
            .toLocalDateTime(TimeZone.UTC)
            .toJavaLocalDateTime(),
        )

        val downloadedFile = File(context.filesDir, FILE_NAME + now + FILE_EXT)

        httpClient.prepareGet(url).execute { response ->
          val channel = response.bodyAsChannel()
          downloadedFile.sink().buffer().use { fileSink ->
            val buffer = ByteArray(8192)
            while (true) {
              val bytesRead = channel.readAvailable(buffer)
              if (bytesRead == -1) break
              fileSink.write(buffer, 0, bytesRead)
            }
          }
        }

        DownloadedFile(
          path = downloadedFile.absolutePath,
          name = downloadedFile.name,
        )
      } catch (exception: Exception) {
        if (exception is CancellationException) {
          throw exception
        }
        logcat(LogPriority.ERROR, exception) { "Could not download pdf with: $exception" }
        raise(ErrorMessage("Could not download pdf"))
      }
    }
  }
}
