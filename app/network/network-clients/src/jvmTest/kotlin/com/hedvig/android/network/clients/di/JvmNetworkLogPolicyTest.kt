package com.hedvig.android.network.clients.di

import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.network.clients.CapturingLogcatLogger
import com.hedvig.android.network.clients.FakeDeviceIdFetcher
import com.hedvig.android.network.clients.FakeHedvigBuildConstants
import com.hedvig.android.network.clients.FakeLanguageService
import com.hedvig.android.network.clients.NoopExtraKtorClientConfiguration
import com.hedvig.android.network.clients.mockEngineFactory
import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

/** Pins the policy Android and desktop builds are given, and the HTTP logging it turns on. */
class JvmNetworkLogPolicyTest {
  @AfterTest
  fun uninstall() {
    LogcatLogger.uninstall()
  }

  @Test
  fun `HTTP bodies are logged outside production builds and GraphQL data in every build`() {
    val provided = buildConfigs.mapValues { (_, appBuildConfig) ->
      val dataDirectory = createTempDirectory("network-log-policy-test").toFile()
      try {
        createGraphFactory<NetworkTestGraph.Factory>().create(dataDirectory, appBuildConfig).networkLogPolicy
          .let { it.logsHttpBodies to it.logsGraphqlData }
      } finally {
        dataDirectory.deleteRecursively()
      }
    }

    assertEquals(
      mapOf(
        "Android debug" to (true to true),
        "Android staging" to (true to true),
        "Android release" to (false to true),
        "desktop staging" to (true to true),
        "desktop production" to (false to true),
      ),
      provided,
    )
  }

  @Test
  fun `the HTTP client logs bodies at VERBOSE outside production and nothing in production`() = runTest {
    val ktorLines = listOf(false, true).associateWith { isProduction ->
      val logs = CapturingLogcatLogger()
      LogcatLogger.install(logs)
      val client = buildKtorClient(
        engineFactory = mockEngineFactory { respond("response-body-sentinel") },
        hedvigBuildConstants = FakeHedvigBuildConstants(isProduction),
        languageService = FakeLanguageService,
        deviceIdFetcher = FakeDeviceIdFetcher,
        extraKtorClientConfiguration = NoopExtraKtorClientConfiguration(),
        networkLogPolicy = object : ApolloClientMetroProviders {}
          .provideNetworkLogPolicy(FakeHedvigBuildConstants(isProduction)),
      )
      client.post("https://example.test") { setBody("request-body-sentinel") }.bodyAsText()
      client.close()
      LogcatLogger.uninstall()
      logs.entries().filter { it.tag == "Ktor" }
    }

    val nonProduction = ktorLines.getValue(false)
    assertEquals(setOf(LogPriority.VERBOSE), nonProduction.map { it.priority }.toSet())
    assertEquals(
      listOf(true, true),
      listOf("request-body-sentinel", "response-body-sentinel").map { body ->
        nonProduction.any { body in it.message }
      },
    )
    assertEquals(emptyList(), ktorLines.getValue(true))
  }
}

private val buildConfigs: Map<String, AppBuildConfig> = mapOf(
  "Android debug" to TestBuildConfig("com.hedvig.dev.app", "debug", Flavor.Develop),
  "Android staging" to TestBuildConfig("com.hedvig.app", "staging", Flavor.Staging),
  "Android release" to TestBuildConfig("com.hedvig.app", "release", Flavor.Production),
  "desktop staging" to TestBuildConfig("com.hedvig.dev.app", "debug", Flavor.Develop),
  "desktop production" to TestBuildConfig("com.hedvig.app", "release", Flavor.Production),
)

private class TestBuildConfig(
  override val applicationId: String,
  override val buildType: String,
  override val appFlavor: Flavor,
) : AppBuildConfig {
  override val debug: Boolean = buildType == "debug"
  override val versionCode: Int = 1
  override val versionName: String = "1.0.0"
  override val osReleaseVersion: String = "1"
  override val osSdkVersion: Int = 1
  override val brand: String = "brand"
  override val model: String = "model"
  override val device: String = "device"
  override val manufacturer: String = "manufacturer"
}
