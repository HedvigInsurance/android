package com.hedvig.android.core.fileupload

import arrow.core.Either
import arrow.core.left
import arrow.core.right
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
import io.ktor.utils.io.readRemaining
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory

/**
 * Downloads into a fresh file in the app's temporary directory, which the OS reclaims.
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class NativeDownloadPdfUseCaseImpl(
  @BaseHttpClient private val httpClient: HttpClient,
) : DownloadPdfUseCase {
  override suspend fun invoke(url: String): Either<ErrorMessage, DownloadedFile> {
    return httpClient.downloadPdf(url, SystemTemporaryDirectory)
  }
}

/**
 * Writes the body of a successful GET of [url] to a new `hedvig_*.pdf` file in [directory], and removes the file again
 * on any failure. The URL is often signed, so neither it nor an exception message that may quote it is logged.
 */
@OptIn(ExperimentalUuidApi::class)
internal suspend fun HttpClient.downloadPdf(url: String, directory: Path): Either<ErrorMessage, DownloadedFile> {
  return withContext(Dispatchers.IO) {
    val fileName = "hedvig_${Uuid.random()}.pdf"
    val file = Path(directory, fileName)
    try {
      val isSuccess = prepareGet(url).execute { response ->
        if (!response.status.isSuccess()) {
          logcat(LogPriority.ERROR) { "Could not download pdf, response status ${response.status.value}" }
          return@execute false
        }
        val channel = response.bodyAsChannel()
        SystemFileSystem.sink(file).buffered().use { sink ->
          while (!channel.isClosedForRead) {
            channel.readRemaining(DOWNLOAD_CHUNK_SIZE).transferTo(sink)
          }
        }
        true
      }
      if (isSuccess) {
        DownloadedFile(path = file.toString(), name = fileName).right()
      } else {
        ErrorMessage("Could not download pdf").left()
      }
    } catch (exception: Exception) {
      runCatching { SystemFileSystem.delete(file, mustExist = false) }
      if (exception is CancellationException) {
        throw exception
      }
      logcat(LogPriority.ERROR) { "Could not download pdf: ${exception::class.simpleName}" }
      ErrorMessage("Could not download pdf").left()
    }
  }
}

private const val DOWNLOAD_CHUNK_SIZE = 8192L
