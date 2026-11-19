package com.hedvig.android.core.fileupload

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Downloads into a fresh file in the system temp directory, which the OS reclaims and which is created readable by the
 * current user only.
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class JvmDownloadPdfUseCaseImpl(
  @BaseHttpClient private val httpClient: HttpClient,
) : DownloadPdfUseCase {
  override suspend fun invoke(url: String): Either<ErrorMessage, DownloadedFile> = withContext(Dispatchers.IO) {
    either {
      var downloadedFile: Path? = null
      try {
        val file = Files.createTempFile("hedvig_", ".pdf")
        downloadedFile = file
        httpClient.prepareGet(url).execute { response ->
          if (!response.status.isSuccess()) {
            throw IOException("Unexpected response status ${response.status}")
          }
          Files.newOutputStream(file).use { outputStream ->
            response.bodyAsChannel().copyTo(outputStream)
          }
        }
        DownloadedFile(
          path = file.toAbsolutePath().toString(),
          name = file.fileName.toString(),
        )
      } catch (exception: Exception) {
        downloadedFile?.let { runCatching { Files.deleteIfExists(it) } }
        if (exception is CancellationException) {
          throw exception
        }
        logcat(LogPriority.ERROR, exception) { "Could not download pdf with: $exception" }
        raise(ErrorMessage("Could not download pdf"))
      }
    }
  }
}
