package com.hedvig.android.core.fileupload

import com.eygraber.uri.Uri
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UriToPathTest {
  private val directory = createTempDirectory("uri-to-path-test").toFile()

  @Test
  fun `a unix file uri becomes its path`() {
    assertEquals(Paths.get("/tmp/receipt.pdf"), Uri.parse("file:/tmp/receipt.pdf").toPathOrNull())
    assertEquals(Paths.get("/tmp/receipt.pdf"), Uri.parse("file:///tmp/receipt.pdf").toPathOrNull())
  }

  @Test
  fun `a file uri of a name with a space and non-ascii characters becomes the file's path`() {
    val file = File(directory, "kvitto åäö.pdf").apply { writeBytes(byteArrayOf(1)) }

    val path = Uri.parse(file.toURI().toString()).toPathOrNull()

    assertEquals(file.toPath(), path)
    assertTrue(path!!.toFile().isFile)
  }

  @Test
  fun `a percent-encoded hash is decoded into the file name`() {
    assertEquals(Paths.get("/tmp/50% off #1.png"), Uri.parse("file:/tmp/50%25%20off%20%231.png").toPathOrNull())
  }

  @Test
  fun `a malformed file uri has no path`() {
    assertNull(Uri.parse("file:/tmp/50% off.png").toPathOrNull())
    assertNull(Uri.parse("file:relative/receipt.pdf").toPathOrNull())
  }

  @Test
  fun `a non-file uri has no path`() {
    assertNull(Uri.parse("https://example.com/receipt.pdf").toPathOrNull())
    assertNull(Uri.parse("content://media/external/images/1").toPathOrNull())
  }

  /**
   * Off Windows this only shows that the Windows form parses into a path ending in the file name; that the path is
   * `C:\Users\a\b.txt` holds only on a Windows file system.
   */
  @Test
  fun `a windows file uri parses into a path`() {
    val path = Uri.parse("file:/C:/Users/a/b.txt").toPathOrNull()

    assertNotNull(path)
    assertEquals("b.txt", path.fileName.toString())
  }
}
