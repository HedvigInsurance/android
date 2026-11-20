package com.hedvig.android.umbrella

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.matches
import com.apollographql.cache.normalized.apolloStore
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.core.tracking.logError
import com.hedvig.android.iosdata.bridge.IosGraphHolder
import com.hedvig.android.iosdata.bridge.component
import com.hedvig.android.logger.IosLogcatLogger
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.network.clients.NetworkLogPolicy
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.http.HttpHeaders
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class IosGraphTest {
  private val sink = RecordingSink()

  @BeforeTest
  fun installSink() {
    IosLogcatLogger.install(sink, LogPriority.VERBOSE)
  }

  @AfterTest
  fun uninstallLoggers() {
    LogcatLogger.uninstall()
    RumLogger.uninstall()
  }

  private val IosGraphHolder.accessors: IosGraphAccessors get() = component()

  @Test
  fun `creating the holder builds nothing and calls no input and installs no logger`() {
    LogcatLogger.uninstall()
    val inputs = FakeInputs()

    inputs.realHolder()

    assertThat(inputs.inputCalls()).isEqualTo(0)
    assertThat(LogcatLogger.isInstalled).isFalse()
    assertThat(RumLogger.isInstalled).isFalse()
  }

  @Test
  fun `the first use installs a log fallback and a RUM logger when the iOS app installed none`() {
    LogcatLogger.uninstall()
    val holder = FakeInputs().realHolder()

    holder.accessors

    assertThat(LogcatLogger.isInstalled).isTrue()
    assertThat(RumLogger.isInstalled).isTrue()
  }

  @Test
  fun `the first use keeps the iOS app's sink and logs how long the graph took to build`() {
    val holder = FakeInputs().realHolder()

    holder.accessors
    holder.accessors

    val built = sink.entries().filter { it.second.startsWith("Built the shared Kotlin graph") }
    assertThat(built.map { it.first }).containsExactly(LogPriority.INFO)
    assertThat(built.single().second).matches(Regex("""Built the shared Kotlin graph in \d+ ms"""))
  }

  @Test
  fun `a RumLogger installed before the graph is kept`() {
    val installed = RecordingRumLogger()
    RumLogger.install(installed)

    FakeInputs().realHolder().accessors
    logError("an error", ErrorSource.SOURCE)

    assertThat(installed.errors).containsExactly("an error")
  }

  @Test
  fun `a production build never logs bodies or GraphQL data whatever the iOS app passes and keeps error text`() {
    val inputs = FakeInputs(
      appFlavor = Flavor.Production,
      networkLogPolicy = NetworkLogPolicy(logsHttpBodies = true, logsGraphqlData = true, logsGraphqlErrors = true),
    )

    val policy = inputs.realHolder().accessors.networkLogPolicy

    assertThat(listOf(policy.logsHttpBodies, policy.logsGraphqlData, policy.logsGraphqlErrors))
      .containsExactly(false, false, true)
  }

  @Test
  fun `a staging build logs what the iOS app asks for`() {
    val inputs = FakeInputs(
      networkLogPolicy = NetworkLogPolicy(logsHttpBodies = true, logsGraphqlData = true, logsGraphqlErrors = false),
    )

    val policy = inputs.realHolder().accessors.networkLogPolicy

    assertThat(listOf(policy.logsHttpBodies, policy.logsGraphqlData, policy.logsGraphqlErrors))
      .containsExactly(true, true, false)
  }

  @Test
  fun `both Apollo clients only log and have no normalized cache`() {
    val accessors = FakeInputs().realHolder().accessors

    for (client in listOf(accessors.apolloClient, accessors.publicApolloClient)) {
      assertThat(client.interceptors.map { it::class.simpleName }).containsExactly("LoggingInterceptor")
      assertFailure { client.apolloStore }.isInstanceOf(IllegalStateException::class)
    }
    assertThat(accessors.apolloClient === accessors.publicApolloClient).isFalse()
  }

  @Test
  fun `the real public client sends no token while a member is logged in`() {
    val accessors = FakeInputs().realHolder().accessors
    val memberAuthorizations = accessors.httpClient.authorizationsStoppedBeforeTheNetwork()
    val publicAuthorizations = accessors.baseHttpClient.authorizationsStoppedBeforeTheNetwork()

    accessors.apolloClient.query()
    accessors.publicApolloClient.query()

    assertThat(memberAuthorizations).containsExactly("Bearer $ACCESS_TOKEN")
    assertThat(publicAuthorizations).containsExactly("")
  }

  @Test
  fun `the build is read from the bundle and the device and only the flavor comes from Swift`() {
    val production = IosAppBuildConfig(Flavor.Production)
    val staging = IosAppBuildConfig(Flavor.Staging)

    assertThat(listOf(production.buildType, production.debug, production.platformName))
      .containsExactly("release", false, "ios")
    assertThat(listOf(staging.buildType, staging.debug)).containsExactly("debug", true)
    assertThat(production.osSdkVersion > 0).isTrue()
    assertThat(production.model.isNotEmpty()).isTrue()
  }
}

private class RecordingRumLogger : RumLogger {
  val errors = mutableListOf<String>()

  override fun logAction(type: ActionType, name: String, attributes: Map<String, Any?>) {}

  override fun logError(
    message: String,
    source: ErrorSource,
    attributes: Map<String, Any?>,
    throwable: Throwable?,
    stacktrace: String?,
  ) {
    errors += message
  }
}

/**
 * Records the `Authorization` header of every request [this] sends, "" where there was none, and fails the request
 * before it reaches the Darwin engine. The interceptor is the client's last, so it sees the token the client added.
 */
private fun HttpClient.authorizationsStoppedBeforeTheNetwork(): List<String> {
  val authorizations = mutableListOf<String>()
  plugin(HttpSend).intercept { request ->
    authorizations += request.headers[HttpHeaders.Authorization].orEmpty()
    throw IllegalStateException("Stopped before the network")
  }
  return authorizations
}
