package com.hedvig.android.featureflags

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isSameInstanceAs
import assertk.assertions.isTrue
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.flags.unleashKey
import com.hedvig.android.logger.TestLogcatLoggingRule
import io.getunleash.android.data.Toggle
import io.getunleash.android.data.UnleashContext
import io.getunleash.android.data.UnleashState
import io.getunleash.android.data.Variant
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HedvigUnleashClientTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private class Setup(val client: HedvigUnleashClient, val unleash: FakeUnleash)

  private fun TestScope.setup(memberIds: Flow<String?>, store: ToggleStateStore = FakeToggleStateStore()): Setup {
    val toggleStateCache = ToggleStateCache(store)
    val unleash = FakeUnleash(toggleStateCache)
    val client = HedvigUnleashClient(
      client = unleash,
      toggleStateCache = toggleStateCache,
      appVersionName = APP_VERSION,
      coroutineScope = backgroundScope,
      memberIds = memberIds,
    )
    runCurrent()
    return Setup(client, unleash)
  }

  @Test
  fun `the first fetch already carries the member id, with userId set to it`() = runTest {
    val setup = setup(MutableStateFlow(MEMBER_A))

    assertThat(setup.unleash.started).isTrue()
    assertThat(setup.unleash.contextAtStart?.properties?.get("memberId")).isEqualTo(MEMBER_A)
    assertThat(setup.unleash.contextAtStart?.userId).isEqualTo(MEMBER_A)
  }

  @Test
  fun `a member id read that never answers starts Unleash without a member after the timeout`() = runTest {
    val setup = setup(flow { awaitCancellation() })
    assertThat(setup.unleash.started).isFalse()

    advanceTimeBy(3.seconds)
    runCurrent()

    assertThat(setup.unleash.started).isTrue()
    assertThat(setup.unleash.contextAtStart?.properties?.get("memberId")).isNull()
  }

  @Test
  fun `a member id read that fails starts Unleash without a member`() = runTest {
    val setup = setup(flow { throw IllegalStateException("token store unreadable") })

    assertThat(setup.unleash.started).isTrue()
    assertThat(setup.unleash.contextAtStart?.properties?.get("memberId")).isNull()
  }

  @Test
  fun `a member id read that fails is retried, so a later login still reaches the context`() = runTest {
    val memberIds = MutableStateFlow<String?>(null)
    var reads = 0
    val setup = setup(
      flow {
        // The first read only decides the context Unleash starts with, so fail the next one too.
        if (reads++ < 2) throw IllegalStateException("token store unreadable")
        emitAll(memberIds)
      },
    )
    assertThat(setup.unleash.started).isTrue()

    advanceTimeBy(31.seconds)
    memberIds.value = MEMBER_A
    runCurrent()

    assertThat(setup.unleash.contexts.last().userId).isEqualTo(MEMBER_A)
  }

  @Test
  fun `toggles fetched without the member do not count as ready for them`() = runTest {
    val setup = setup(MutableStateFlow(MEMBER_A))
    val ready = backgroundScope.async { setup.client.awaitReady() }

    setup.unleash.deliver(context(memberId = null), toggle(EXPERIMENT, "DISCOUNT_50"))
    runCurrent()
    assertThat(ready.isCompleted).isFalse()
    assertThat(setup.client.variantOf(EXPERIMENT)).isNull()

    setup.unleash.deliver(context(MEMBER_A), toggle(EXPERIMENT, "NO_DISCOUNT"))
    runCurrent()
    assertThat(ready.isCompleted).isTrue()
    assertThat(setup.client.variantOf(EXPERIMENT)).isEqualTo("NO_DISCOUNT")
  }

  @Test
  fun `after a login the previous toggles are not served as the new member's variant`() = runTest {
    val memberIds = MutableStateFlow<String?>(null)
    val setup = setup(memberIds)
    setup.unleash.deliver(context(memberId = null))
    runCurrent()
    assertThat(setup.client.hasStateForCurrentMember()).isTrue()

    memberIds.value = MEMBER_A
    runCurrent()
    assertThat(setup.client.hasStateForCurrentMember()).isFalse()
    assertThat(setup.client.variantOf(EXPERIMENT)).isNull()

    setup.unleash.deliver(context(MEMBER_A), toggle(EXPERIMENT, "DISCOUNT_50"))
    runCurrent()
    assertThat(setup.client.variantOf(EXPERIMENT)).isEqualTo("DISCOUNT_50")
  }

  @Test
  fun `after a logout the previous member's variant is not served`() = runTest {
    val memberIds = MutableStateFlow<String?>(MEMBER_A)
    val setup = setup(memberIds)
    setup.unleash.deliver(context(MEMBER_A), toggle(EXPERIMENT, "NO_DISCOUNT"))
    runCurrent()

    memberIds.value = null
    runCurrent()

    assertThat(setup.client.variantOf(EXPERIMENT)).isNull()
  }

  @Test
  fun `a late response for an earlier context makes the member's state unknown again`() = runTest {
    val setup = setup(MutableStateFlow(MEMBER_A))
    setup.unleash.deliver(context(MEMBER_A), toggle(EXPERIMENT, "NO_DISCOUNT"))
    runCurrent()

    setup.unleash.deliver(context(memberId = null))
    runCurrent()

    assertThat(setup.client.hasStateForCurrentMember()).isFalse()
    assertThat(setup.client.variantOf(EXPERIMENT)).isNull()
  }

  @Test
  fun `a 304 for the member's context adopts the toggles in hand as theirs`() = runTest {
    val store = FakeToggleStateStore()
    val memberIds = MutableStateFlow<String?>(null)
    val setup = setup(memberIds, store)
    setup.unleash.deliver(context(memberId = null), toggle(EXPERIMENT, "DISCOUNT_50"))
    runCurrent()

    memberIds.value = MEMBER_A
    runCurrent()
    val ready = backgroundScope.async { setup.client.awaitReady() }
    runCurrent()
    assertThat(ready.isCompleted).isFalse()

    setup.unleash.deliverNotModified()
    runCurrent()

    assertThat(ready.isCompleted).isTrue()
    assertThat(setup.client.variantOf(EXPERIMENT)).isEqualTo("DISCOUNT_50")
    assertThat(store.stored?.context?.properties?.get("memberId")).isEqualTo(MEMBER_A)
  }

  @Test
  fun `a 304 while the toggles are already the member's own changes nothing`() = runTest {
    val store = FakeToggleStateStore()
    val setup = setup(MutableStateFlow(MEMBER_A), store)
    setup.unleash.deliver(context(MEMBER_A), toggle(EXPERIMENT, "NO_DISCOUNT"))
    runCurrent()
    val stored = store.stored

    setup.unleash.deliverNotModified()
    runCurrent()

    assertThat(store.stored).isSameInstanceAs(stored)
    assertThat(setup.client.variantOf(EXPERIMENT)).isEqualTo("NO_DISCOUNT")
  }

  @Test
  fun `a 304 before any toggles are in hand leaves the member without state`() = runTest {
    val setup = setup(MutableStateFlow(MEMBER_A))

    setup.unleash.deliverNotModified()
    runCurrent()

    assertThat(setup.client.hasStateForCurrentMember()).isFalse()
  }

  @Test
  fun `a member's own state stored by an older app version answers at once`() = runTest {
    val store = FakeToggleStateStore(
      UnleashState(context(MEMBER_A, appVersion = "0.9.0"), mapOf(EXPERIMENT to toggle(EXPERIMENT, "DISCOUNT_50"))),
    )

    val setup = setup(MutableStateFlow(MEMBER_A), store)

    assertThat(setup.client.hasStateForCurrentMember()).isTrue()
    assertThat(setup.client.variantOf(EXPERIMENT)).isEqualTo("DISCOUNT_50")
  }

  @Test
  fun `the stored state is restored before Unleash starts`() = runTest {
    val store = FakeToggleStateStore(
      UnleashState(context(MEMBER_A), mapOf(EXPERIMENT to toggle(EXPERIMENT, "DISCOUNT_50"))),
    )

    val setup = setup(MutableStateFlow(MEMBER_A), store)

    assertThat(setup.unleash.togglesAtStart?.keys).isEqualTo(setOf(EXPERIMENT))
  }

  @Test
  fun `every fetched state is stored`() = runTest {
    val store = FakeToggleStateStore()
    val setup = setup(MutableStateFlow(MEMBER_A), store)

    setup.unleash.deliver(context(MEMBER_A), toggle(EXPERIMENT, "NO_DISCOUNT"))
    runCurrent()

    assertThat(store.stored?.toggles?.get(EXPERIMENT)?.variant?.name).isEqualTo("NO_DISCOUNT")
  }

  @Test
  fun `flag reads keep using the previous state while the member's own state is on its way`() = runTest {
    val memberIds = MutableStateFlow<String?>(null)
    val setup = setup(memberIds)
    setup.unleash.deliver(context(memberId = null), Toggle(Feature.DISABLE_PUPPY_GUIDE.unleashKey, enabled = true))
    runCurrent()

    memberIds.value = MEMBER_A
    runCurrent()

    assertThat(setup.client.valueOf(Feature.DISABLE_PUPPY_GUIDE)).isTrue()
  }

  @Test
  fun `before any state a kill switch reads its never-fetched default`() = runTest {
    val setup = setup(MutableStateFlow(MEMBER_A))

    assertThat(setup.client.valueOf(Feature.DISABLE_PUPPY_GUIDE)).isTrue()
    assertThat(setup.client.valueOf(Feature.ENABLE_CLAIM_INTENT_RESUME)).isFalse()
  }

  private fun context(memberId: String?, appVersion: String = APP_VERSION): UnleashContext {
    val properties = buildMap {
      put("appVersion", appVersion)
      put("appName", "android")
      put("market", "SE")
      if (memberId != null) put("memberId", memberId)
    }
    return UnleashContext(userId = memberId, properties = properties)
  }

  private fun toggle(name: String, variant: String) =
    Toggle(name = name, enabled = true, variant = Variant(name = variant, enabled = true, featureEnabled = true))

  private companion object {
    const val APP_VERSION = "1.0.0"
    const val MEMBER_A = "666227461"
    const val EXPERIMENT = "accident_discount_experiment"
  }
}
