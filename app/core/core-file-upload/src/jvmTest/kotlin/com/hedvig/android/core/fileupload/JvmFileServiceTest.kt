package com.hedvig.android.core.fileupload

import com.eygraber.uri.Uri
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.io.IOException
import kotlinx.io.readByteArray

@DependencyGraph(AppScope::class)
internal interface FileServiceTestGraph {
  val fileService: FileService
}

class JvmFileServiceTest {
  private val directory = createTempDirectory("file-service-test").toFile()
  private val fileService = createGraph<FileServiceTestGraph>().fileService

  @Test
  fun `the file service resolves to the JVM implementation`() {
    assertIs<JvmFileService>(fileService)
  }

  @Test
  fun `an existing file exposes its name, mime type, size and bytes`() {
    val bytes = byteArrayOf(1, 2, 3, 4)
    val file = File(directory, "receipt.pdf").apply { writeBytes(bytes) }
    val uri = file.toUri()

    val commonFile = fileService.convertToCommonFile(uri)

    assertEquals("receipt.pdf", fileService.getFileName(uri))
    assertEquals("application/pdf", fileService.getMimeType(uri))
    assertTrue(fileService.isFileSizeWithinBackendLimits(uri))
    assertEquals("receipt.pdf", commonFile.fileName)
    assertEquals("application/pdf", commonFile.mimeType)
    assertEquals(4L, commonFile.getSize())
    assertContentEquals(bytes, commonFile.source().readByteArray())
  }

  @Test
  fun `a file name with spaces is decoded`() {
    val file = File(directory, "my photo.jpg").apply { writeBytes(byteArrayOf(0)) }

    assertEquals("my photo.jpg", fileService.getFileName(file.toUri()))
    assertEquals("image/jpeg", fileService.getMimeType(file.toUri()))
  }

  @Test
  fun `a missing file reports an unknown size and fails to open with an IOException`() {
    val uri = File(directory, "missing.pdf").toUri()

    val commonFile = fileService.convertToCommonFile(uri)

    assertEquals("missing.pdf", fileService.getFileName(uri))
    assertEquals(-1L, commonFile.getSize())
    assertFailsWith<IOException> { commonFile.source() }
  }

  @Test
  fun `a non-file uri has no file behind it`() {
    val uri = Uri.parse("https://example.com/receipt.pdf")

    val commonFile = fileService.convertToCommonFile(uri)

    assertEquals(-1L, commonFile.getSize())
    assertFailsWith<IOException> { commonFile.source() }
  }

  private fun File.toUri(): Uri = Uri.parse(toURI().toString())
}
