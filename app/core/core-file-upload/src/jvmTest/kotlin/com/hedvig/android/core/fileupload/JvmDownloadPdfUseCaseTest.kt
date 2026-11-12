package com.hedvig.android.core.fileupload

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.logger.TestLogcatLoggingRule
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
import java.io.File
import java.io.IOException
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Rule

/** Supplies a mock-engine [HttpClient] in place of the network module's real one. */
@DependencyGraph(AppScope::class, excludes = [NetworkMetroProviders::class])
internal interface DownloadPdfTestGraph {
  val downloadPdfUseCase: DownloadPdfUseCase

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Provides @BaseHttpClient httpClient: HttpClient,
    ): DownloadPdfTestGraph
  }
}

class JvmDownloadPdfUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val tempDirectory = File(System.getProperty("java.io.tmpdir"))

  private fun downloadPdfUseCase(handler: MockRequestHandler): DownloadPdfUseCase {
    return createGraphFactory<DownloadPdfTestGraph.Factory>()
      .create(HttpClient(MockEngine(handler)))
      .downloadPdfUseCase
  }

  private fun downloadedFilesInTemp(): Set<String> {
    return tempDirectory.listFiles { file -> file.name.startsWith("hedvig_") && file.name.endsWith(".pdf") }
      .orEmpty()
      .map { it.name }
      .toSet()
  }

  @Test
  fun `the download use case resolves to the JVM implementation`() {
    assertIs<JvmDownloadPdfUseCaseImpl>(downloadPdfUseCase { respond(ByteArray(0)) })
  }

  @Test
  fun `a successful response is written to the returned file`() = runTest {
    val bytes = Random(0).nextBytes(1024 * 1024)
    val useCase = downloadPdfUseCase { respond(bytes) }

    val downloadedFile = useCase.invoke("https://example.com/certificate.pdf").getOrNull()!!

    val file = File(downloadedFile.path)
    try {
      assertTrue(file.isFile)
      assertEquals(file.name, downloadedFile.name)
      assertTrue(downloadedFile.name.startsWith("hedvig_"))
      assertTrue(downloadedFile.name.endsWith(".pdf"))
      assertContentEquals(bytes, file.readBytes())
    } finally {
      file.delete()
    }
  }

  @Test
  fun `a non-success response returns an error and leaves no file behind`() = runTest {
    val filesBefore = downloadedFilesInTemp()
    val useCase = downloadPdfUseCase { respondError(HttpStatusCode.NotFound, "not found") }

    val result = useCase.invoke("https://example.com/certificate.pdf")

    assertTrue(result.isLeft())
    assertEquals(filesBefore, downloadedFilesInTemp())
  }

  @Test
  fun `a network exception returns an error and leaves no file behind`() = runTest {
    val filesBefore = downloadedFilesInTemp()
    val useCase = downloadPdfUseCase { throw IOException("connection reset") }

    val result = useCase.invoke("https://example.com/certificate.pdf")

    assertTrue(result.isLeft())
    assertEquals(filesBefore, downloadedFilesInTemp())
  }
}
