package com.hedvig.android.featureflags

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.logger.TestLogcatLoggingRule
import io.getunleash.android.data.Payload
import io.getunleash.android.data.Toggle
import io.getunleash.android.data.UnleashContext
import io.getunleash.android.data.UnleashState
import io.getunleash.android.data.Variant
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PersistentToggleCacheTest {
  @get:Rule
  val temporaryFolder = TemporaryFolder()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val file: File by lazy { File(temporaryFolder.root, "state.json") }

  @Test
  fun `a new process restores the state written by the previous one, with its context`() {
    val written = UnleashState(memberContext(), toggles(Toggle("enable_x", enabled = true)))
    PersistentToggleCache(file).write(written)

    val nextProcess = PersistentToggleCache(file)
    nextProcess.restore()

    assertThat(nextProcess.lastFetchedState.value).isEqualTo(written)
    assertThat(nextProcess.get("enable_x")?.enabled).isEqualTo(true)
  }

  @Test
  fun `a toggle change for an unchanged context reaches disk`() {
    val cache = PersistentToggleCache(file)
    cache.write(UnleashState(memberContext(), toggles(Toggle("enable_x", enabled = true))))
    val changed = UnleashState(memberContext(), toggles(Toggle("enable_y", enabled = true)))
    cache.write(changed)

    val nextProcess = PersistentToggleCache(file)
    nextProcess.restore()

    assertThat(nextProcess.lastFetchedState.value).isEqualTo(changed)
    assertThat(nextProcess.get("enable_x")).isNull()
  }

  @Test
  fun `restoring does not replace state already fetched in this process`() {
    PersistentToggleCache(file).write(UnleashState(memberContext(), toggles(Toggle("enable_x", enabled = true))))
    val cache = PersistentToggleCache(file)
    val fetched = UnleashState(memberContext(), toggles(Toggle("enable_y", enabled = true)))
    cache.write(fetched)

    cache.restore()

    assertThat(cache.lastFetchedState.value).isEqualTo(fetched)
  }

  @Test
  fun `nothing is restored on a first install`() {
    val cache = PersistentToggleCache(file)

    cache.restore()

    assertThat(cache.lastFetchedState.value).isNull()
    assertThat(cache.read()).isEmpty()
  }

  @Test
  fun `an unreadable file restores nothing`() {
    file.writeText("not json")
    val cache = PersistentToggleCache(file)

    cache.restore()

    assertThat(cache.lastFetchedState.value).isNull()
  }

  @Test
  fun `variants, payloads and every context field survive the round trip`() {
    val written = UnleashState(
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
    PersistentToggleCache(file).write(written)

    val nextProcess = PersistentToggleCache(file)
    nextProcess.restore()

    assertThat(nextProcess.lastFetchedState.value).isEqualTo(written)
  }

  private fun memberContext() = UnleashContext(
    properties = mapOf(
      "appVersion" to "14.0.0",
      "appName" to "android",
      "market" to "SE",
      "memberId" to "member",
    ),
  )

  private fun toggles(vararg toggles: Toggle): Map<String, Toggle> = toggles.associateBy { it.name }
}
