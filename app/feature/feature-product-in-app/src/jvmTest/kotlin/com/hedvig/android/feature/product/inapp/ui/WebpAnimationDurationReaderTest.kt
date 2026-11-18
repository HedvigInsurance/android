package com.hedvig.android.feature.product.inapp.ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import okio.Buffer
import org.junit.Test

class WebpAnimationDurationReaderTest {
  @Test
  fun `sums the durations of every frame`() {
    val webp = webp(
      chunk("VP8X", ByteArray(10)),
      chunk("ANIM", ByteArray(6)),
      frame(durationMillis = 40, payloadSize = 7),
      frame(durationMillis = 70_000, payloadSize = 4),
    )

    assertThat(animatedWebpDurationMillis(webp)).isEqualTo(70_040)
  }

  @Test
  fun `a still WebP has no duration`() {
    assertThat(animatedWebpDurationMillis(webp(chunk("VP8 ", ByteArray(5))))).isNull()
  }

  @Test
  fun `a file that is not a WebP has no duration`() {
    assertThat(animatedWebpDurationMillis(Buffer().writeUtf8("\u0089PNG\r\n\u001a\n0000"))).isNull()
  }

  @Test
  fun `a truncated WebP has no duration`() {
    val truncated = Buffer().apply {
      writeUtf8("RIFF")
      writeIntLe(100)
      writeUtf8("WEBP")
      writeUtf8("ANMF")
      writeIntLe(40)
      write(ByteArray(5))
    }

    assertThat(animatedWebpDurationMillis(truncated)).isNull()
  }

  private fun webp(vararg chunks: ByteArray): Buffer {
    val body = Buffer().writeUtf8("WEBP").apply { chunks.forEach { write(it) } }
    return Buffer().writeUtf8("RIFF").writeIntLe(body.size.toInt()).apply { writeAll(body) }
  }

  private fun frame(durationMillis: Int, payloadSize: Int): ByteArray {
    val header = Buffer()
      .write(ByteArray(12))
      .writeByte(durationMillis and 0xFF)
      .writeByte((durationMillis shr 8) and 0xFF)
      .writeByte((durationMillis shr 16) and 0xFF)
      .writeByte(0)
    return chunk("ANMF", header.readByteArray() + ByteArray(payloadSize))
  }

  private fun chunk(fourCc: String, payload: ByteArray): ByteArray {
    return Buffer()
      .writeUtf8(fourCc)
      .writeIntLe(payload.size)
      .write(payload)
      .apply { if (payload.size % 2 == 1) writeByte(0) }
      .readByteArray()
  }
}
