package com.hedvig.android.core.fileupload

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.network.clients.di.NativeNetworkMetroProviders
import com.hedvig.android.network.clients.di.NetworkMetroProviders
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.readByteArray

/** Supplies a mock-engine [HttpClient] in place of the network module's real ones. */
@DependencyGraph(AppScope::class, excludes = [NetworkMetroProviders::class, NativeNetworkMetroProviders::class])
internal interface NativeDownloadPdfTestGraph {
  val downloadPdfUseCase: DownloadPdfUseCase

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Provides @BaseHttpClient httpClient: HttpClient,
    ): NativeDownloadPdfTestGraph
  }
}

@OptIn(ExperimentalUuidApi::class)
class NativeDownloadPdfUseCaseTest {
  private val signedUrl = "https://example.com/certificate.pdf?X-Amz-Signature=secret-signature"
  private val directory = Path(SystemTemporaryDirectory, "native_download_pdf_test_${Uuid.random()}")
  private val logs = mutableListOf<String>()

  @BeforeTest
  fun setUp() {
    SystemFileSystem.createDirectories(directory)
    LogcatLogger.install(
      object : LogcatLogger {
        override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
          logs += "$priority $tag ${message()} ${throwable?.stackTraceToString()}"
        }
      },
    )
  }

  @AfterTest
  fun tearDown() {
    LogcatLogger.uninstall()
    SystemFileSystem.list(directory).forEach { SystemFileSystem.delete(it) }
    SystemFileSystem.delete(directory)
  }

  private fun client(handler: MockRequestHandler): HttpClient = HttpClient(MockEngine(handler))

  private fun assertNothingLoggedTheUrl() {
    assertTrue(logs.none { "example.com" in it || "secret-signature" in it }, "Logs quoted the URL: $logs")
  }

  @Test
  fun `the download use case resolves to the native implementation`() {
    val useCase = createGraphFactory<NativeDownloadPdfTestGraph.Factory>()
      .create(client { respond(ByteArray(0)) })
      .downloadPdfUseCase

    assertIs<NativeDownloadPdfUseCaseImpl>(useCase)
  }

  @Test
  fun `a successful response is written to the returned file`() = runTest {
    val bytes = Random(0).nextBytes(1024 * 1024)

    val downloadedFile = client { respond(bytes) }.downloadPdf(signedUrl, directory).getOrNull()!!

    val file = Path(downloadedFile.path)
    assertEquals(directory, file.parent)
    assertEquals(file.name, downloadedFile.name)
    assertTrue(downloadedFile.name.startsWith("hedvig_"))
    assertTrue(downloadedFile.name.endsWith(".pdf"))
    assertContentEquals(bytes, SystemFileSystem.source(file).buffered().use { it.readByteArray() })
    assertNothingLoggedTheUrl()
  }

  @Test
  fun `a non-success response returns an error and leaves no file without logging the url`() = runTest {
    val result = client { respondError(HttpStatusCode.Forbidden, "denied") }.downloadPdf(signedUrl, directory)

    assertTrue(result.isLeft())
    assertTrue(SystemFileSystem.list(directory).isEmpty())
    assertTrue(logs.any { "403" in it })
    assertNothingLoggedTheUrl()
  }

  @Test
  fun `a network exception quoting the url returns an error without logging the url`() = runTest {
    val result = client { request -> throw IOException("Connection reset for ${request.url}") }
      .downloadPdf(signedUrl, directory)

    assertTrue(result.isLeft())
    assertTrue(SystemFileSystem.list(directory).isEmpty())
    assertFalse(logs.isEmpty())
    assertNothingLoggedTheUrl()
  }

  @Test
  fun `a file that cannot be written returns an error without logging the url`() = runTest {
    val missingDirectory = Path(directory, "missing")

    val result = client { respond(ByteArray(16)) }.downloadPdf(signedUrl, missingDirectory)

    assertTrue(result.isLeft())
    assertFalse(SystemFileSystem.exists(missingDirectory))
    assertFalse(logs.isEmpty())
    assertNothingLoggedTheUrl()
  }
}
