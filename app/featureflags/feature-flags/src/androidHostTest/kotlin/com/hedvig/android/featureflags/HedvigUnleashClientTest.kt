package com.hedvig.android.featureflags

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.TestLogcatLoggingRule
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class HedvigUnleashClientTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val clientScopes = mutableListOf<CoroutineScope>()

  @After
  fun cancelClientScopes() = clientScopes.forEach(CoroutineScope::cancel)

  /**
   * The client gets a scope of its own, sharing the test scheduler, rather than the test's: its
   * member id collection never completes, so the test coroutine would never finish waiting for it.
   * A background scope would not do either, since virtual time is not advanced for background work
   * alone and the client waits out a timeout when no member id arrives.
   */
  private fun TestScope.client(unleash: FakeUnleash, memberIds: Flow<String?>): HedvigUnleashClient {
    val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
    clientScopes.add(scope)
    return HedvigUnleashClient(
      client = unleash,
      appVersionName = "14.4.6",
      coroutineScope = scope,
      memberIds = memberIds,
    )
  }

  @Test
  fun `while no toggle set has been delivered a flag reads its never-fetched default`() = runTest {
    val unleash = FakeUnleash()
    val client = client(unleash, MutableStateFlow("member-1"))
    runCurrent()

    assertThat(client.valueOf(Feature.DISABLE_ANALYTICS)).isTrue()
    assertThat(client.valueOf(Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS)).isTrue()
  }

  @Test
  fun `while no toggle set has been delivered a flag without a never-fetched default reads false`() = runTest {
    val unleash = FakeUnleash()
    val client = client(unleash, MutableStateFlow("member-1"))
    runCurrent()

    assertThat(client.valueOf(Feature.ENABLE_CLAIM_INTENT_RESUME)).isFalse()
  }

  @Test
  fun `an empty toggle set leaves the client unready, so flags keep their never-fetched defaults`() = runTest {
    val unleash = FakeUnleash()
    val client = client(unleash, MutableStateFlow("member-1"))
    runCurrent()

    unleash.completeFetch(enabledToggles = emptySet())

    assertThat(client.valueOf(Feature.DISABLE_ANALYTICS)).isTrue()
  }

  @Test
  fun `a flag left out of a delivered toggle set reads false, not its never-fetched default`() = runTest {
    val unleash = FakeUnleash()
    val client = client(unleash, MutableStateFlow("member-1"))
    runCurrent()

    unleash.completeFetch(enabledToggles = setOf("disable_onboarding"))

    assertThat(client.valueOf(Feature.DISABLE_ANALYTICS)).isFalse()
    assertThat(client.valueOf(Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS)).isFalse()
    assertThat(client.valueOf(Feature.DISABLE_ONBOARDING)).isTrue()
  }

  @Test
  fun `the first fetch carries the member, so member-sticky strategies resolve for them`() = runTest {
    val unleash = FakeUnleash()
    client(unleash, MutableStateFlow("member-1"))
    runCurrent()

    val contextAtStart = unleash.contexts.last()
    assertThat(contextAtStart.userId).isEqualTo("member-1")
    assertThat(contextAtStart.properties["memberId"]).isEqualTo("member-1")
    assertThat(unleash.started).isTrue()
  }

  @Test
  fun `a logged out member starts with no member in context, without waiting out the timeout`() = runTest {
    val unleash = FakeUnleash()
    client(unleash, MutableStateFlow(null))
    runCurrent()

    assertThat(unleash.started).isTrue()
    assertThat(unleash.contexts.last().userId).isNull()
    assertThat(unleash.contexts.last().properties["memberId"]).isNull()
    assertThat(currentTime).isEqualTo(0L)
  }

  @Test
  fun `a member id that never arrives still starts Unleash once the wait expires`() = runTest {
    val unleash = FakeUnleash()
    client(unleash, MutableSharedFlow())
    advanceUntilIdle()

    assertThat(unleash.started).isTrue()
  }

  @Test
  fun `a member id that cannot be read still starts Unleash`() = runTest {
    val unleash = FakeUnleash()
    client(unleash, flow { throw IOException("Token store unreadable") })
    advanceUntilIdle()

    assertThat(unleash.started).isTrue()
    assertThat(unleash.contexts.last().userId).isNull()
  }

  @Test
  fun `awaitReady returns once the toggle set for the member in context lands`() = runTest {
    val unleash = FakeUnleash()
    val client = client(unleash, MutableStateFlow("member-1"))
    runCurrent()

    var returned = false
    backgroundScope.launch {
      client.awaitReady()
      returned = true
    }
    runCurrent()
    assertThat(returned).isFalse()

    unleash.completeFetch(enabledToggles = setOf("disable_analytics"))
    runCurrent()

    assertThat(returned).isTrue()
  }

  @Test
  fun `awaitReady does not return on a toggle set fetched before the member logged in`() = runTest {
    val unleash = FakeUnleash()
    val memberIds = MutableStateFlow<String?>(null)
    val client = client(unleash, memberIds)
    runCurrent()
    // Logged out, so the set fetched anonymously is the right answer and readiness is satisfied.
    unleash.completeFetch(enabledToggles = setOf("disable_onboarding"))
    runCurrent()

    memberIds.value = "member-1"
    runCurrent()
    // The member-sticky kill switch is absent from the set in hand purely because the fetch that
    // produced it named no member, which is what awaitReady must not let a caller act on.
    assertThat(client.valueOf(Feature.DISABLE_ANALYTICS)).isFalse()

    var returned = false
    backgroundScope.launch {
      client.awaitReady()
      returned = true
    }
    runCurrent()
    assertThat(returned).isFalse()

    unleash.completeFetch(enabledToggles = setOf("disable_onboarding", "disable_analytics"))
    runCurrent()

    assertThat(returned).isTrue()
    assertThat(client.valueOf(Feature.DISABLE_ANALYTICS)).isTrue()
  }

  @Test
  fun `awaitReady returns immediately for a logged out member once a toggle set lands`() = runTest {
    val unleash = FakeUnleash()
    val client = client(unleash, MutableStateFlow(null))
    runCurrent()
    unleash.completeFetch(enabledToggles = setOf("disable_onboarding"))

    var returned = false
    backgroundScope.launch {
      client.awaitReady()
      returned = true
    }
    runCurrent()

    assertThat(returned).isTrue()
  }
}
