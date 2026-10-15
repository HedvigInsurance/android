package com.hedvig.android.featureflags

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.logger.TestLogcatLoggingRule
import io.getunleash.android.data.Payload
import io.getunleash.android.data.Toggle
import io.getunleash.android.data.UnleashContext
import io.getunleash.android.data.UnleashState
import io.getunleash.android.data.Variant
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileToggleStateStoreTest {
  @get:Rule
  val temporaryFolder = TemporaryFolder()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val file: File by lazy { File(temporaryFolder.root, "state.json") }

  @Test
  fun `variants, payloads and every context field survive the round trip`() = runTest {
    val saved = UnleashState(
      context = UnleashContext(
        userId = "user",
        sessionId = "session",
        remoteAddress = "127.0.0.1",
        properties = mapOf("memberId" to "member", "appVersion" to "14.0.0"),
      ),
      toggles = toggles(
        Toggle(
          name = "enable_x",
          enabled = true,
          impressionData = true,
          variant = Variant(
            name = "blue",
            enabled = true,
            featureEnabled = true,
            payload = Payload(type = "string", value = "payload"),
          ),
        ),
        Toggle(name = "enable_y", enabled = false),
      ),
    )
    FileToggleStateStore(file).save(saved)

    assertThat(FileToggleStateStore(file).load()).isEqualTo(saved)
  }

  @Test
  fun `a later save replaces the earlier one`() = runTest {
    val store = FileToggleStateStore(file)
    store.save(UnleashState(UnleashContext(), toggles(Toggle("enable_x", enabled = true))))
    val later = UnleashState(UnleashContext(), toggles(Toggle("enable_y", enabled = true)))
    store.save(later)

    assertThat(FileToggleStateStore(file).load()).isEqualTo(later)
  }

  @Test
  fun `nothing loads on a first install`() = runTest {
    assertThat(FileToggleStateStore(file).load()).isNull()
  }

  @Test
  fun `an unreadable file loads nothing`() = runTest {
    file.writeText("not json")

    assertThat(FileToggleStateStore(file).load()).isNull()
  }

  private fun toggles(vararg toggles: Toggle): Map<String, Toggle> = toggles.associateBy { it.name }
}
