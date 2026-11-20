package com.hedvig.android.umbrella

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.iosdata.bridge.IosGraphHolder
import com.hedvig.android.iosdata.bridge.ReadFailureKind
import com.hedvig.android.iosdata.bridge.ReadFailures
import com.hedvig.android.iosdata.bridge.component
import com.hedvig.android.iosdata.bridge.iosGraphHolder
import com.hedvig.android.iosdata.bridge.iosRead
import com.hedvig.android.logger.IosLogcatLogger
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeSuspend
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import kotlin.concurrent.AtomicReference
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import octopus.PayingMemberPhoneNumberQuery
import platform.Foundation.NSError

/** A feature's reads, written the way a feature module writes them. */
@Inject
class ExampleReads(private val apolloClient: ApolloClient) {
  @NativeCoroutines
  suspend fun memberId(): String = iosRead {
    apolloClient.query(PayingMemberPhoneNumberQuery()).safeExecute().bind().currentMember.id
  }
}

@ContributesTo(AppScope::class)
interface ExampleReadsComponent {
  val exampleReads: ExampleReads
}

fun IosGraphHolder.exampleReads(): ExampleReads = component<ExampleReadsComponent>().exampleReads

/** The read reaches Swift as the `NativeSuspend` the plugin generates, which Swift awaits with `asyncFunction(for:)`. */
class ExampleReadsTest {
  @BeforeTest
  fun installSink() {
    IosLogcatLogger.install(RecordingSink(), LogPriority.VERBOSE)
  }

  @AfterTest
  fun uninstallLogger() {
    LogcatLogger.uninstall()
  }

  @Test
  fun `a read delivers its value`() {
    val backend = RecordingBackend(mapOf(OPERATION to MEMBER_JSON))
    val holder = iosGraphHolder { FakeInputs().mockedGraph(backend) }

    val delivered = holder.exampleReads().memberIdNative().await()

    assertThat(delivered.value).isEqualTo("member-1")
    assertThat(backend.sent().single().headers["Authorization"]).isEqualTo("Bearer $ACCESS_TOKEN")
  }

  @Test
  fun `a failed read throws a ReadFailure that Swift reads from the error`() {
    val holder = iosGraphHolder { FakeInputs().mockedGraph(RecordingBackend()) }

    val delivered = holder.exampleReads().memberIdNative().await()

    assertThat(delivered.value).isNull()
    assertThat(ReadFailures.from(delivered.error!!)?.kind).isEqualTo(ReadFailureKind.OTHER)
  }

  @Test
  fun `a token fetch that throws fails the read as NETWORK`() {
    val inputs = FakeInputs(accessTokenFetcher = FakeAccessTokenFetcher(throws = true))
    val holder = iosGraphHolder { inputs.mockedGraph(RecordingBackend(mapOf(OPERATION to MEMBER_JSON))) }

    val delivered = holder.exampleReads().memberIdNative().await()

    assertThat(delivered.error).isNotNull()
    assertThat(ReadFailures.from(delivered.error!!)?.kind).isEqualTo(ReadFailureKind.NETWORK)
  }
}

private class Delivered<T>(val value: T?, val error: NSError?)

/** Starts the read as Swift does and waits for its callback. */
private fun <T> NativeSuspend<T>.await(): Delivered<T> {
  val delivered = AtomicReference<Delivered<T>?>(null)
  this(
    { value, unit ->
      delivered.value = Delivered(value, null)
      unit
    },
    { error, unit ->
      delivered.value = Delivered(null, error)
      unit
    },
    { error, unit ->
      delivered.value = Delivered(null, error)
      unit
    },
  )
  assertThat(pumpMainLoopUntil { delivered.value != null }).isTrue()
  return delivered.value!!
}

private const val OPERATION = "PayingMemberPhoneNumber"

private val MEMBER_JSON = """
  {"data": {"currentMember": {"__typename": "Member", "id": "member-1", "phoneNumber": null}}}
""".trimIndent()
