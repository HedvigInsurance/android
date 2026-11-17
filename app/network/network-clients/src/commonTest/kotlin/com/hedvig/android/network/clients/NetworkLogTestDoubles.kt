package com.hedvig.android.network.clients

import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.CompiledField
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.json.JsonWriter
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.core.locale.CommonLocale
import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandler
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal data class CapturedLog(val priority: LogPriority, val tag: String?, val message: String)

/** Records every line with its throwable appended. Lines arrive from request threads too. */
internal class CapturingLogcatLogger : LogcatLogger {
  private val lock = SynchronizedObject()
  private val captured = mutableListOf<CapturedLog>()

  fun entries(): List<CapturedLog> = synchronized(lock) { captured.toList() }

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    val text = message() + (throwable?.let { "\n" + it.stackTraceToString() } ?: "")
    synchronized(lock) { captured += CapturedLog(priority, tag, text) }
  }
}

internal data class CapturedRumError(val message: String, val source: ErrorSource, val attributes: Map<String, Any?>)

internal class CapturingRumLogger : RumLogger {
  private val lock = SynchronizedObject()
  private val captured = mutableListOf<CapturedRumError>()

  fun errors(): List<CapturedRumError> = synchronized(lock) { captured.toList() }

  override fun logAction(type: ActionType, name: String, attributes: Map<String, Any?>) {}

  override fun logError(
    message: String,
    source: ErrorSource,
    attributes: Map<String, Any?>,
    throwable: Throwable?,
    stacktrace: String?,
  ) {
    synchronized(lock) { captured += CapturedRumError(message, source, attributes) }
  }
}

/** A query whose responses are queued on a test transport, so nothing ever parses or caches it. */
internal object SentinelQuery : Query<SentinelQuery.Data> {
  data class Data(val value: String) : Query.Data

  override fun document(): String = "query Sentinel { __typename }"

  override fun name(): String = "Sentinel"

  override fun id(): String = "Sentinel"

  override fun adapter(): Adapter<Data> = error("Queued responses are never parsed")

  override fun serializeVariables(
    writer: JsonWriter,
    customScalarAdapters: CustomScalarAdapters,
    withDefaultValues: Boolean,
  ) {}

  override fun rootField(): CompiledField = error("The client has no cache")
}

internal class FakeHedvigBuildConstants(override val isProduction: Boolean) : HedvigBuildConstants {
  override val urlGraphqlOctopus: String = "https://example.test/graphql"
  override val urlBaseWeb: String = ""
  override val urlOdyssey: String = ""
  override val urlHedvigGateway: String = ""
  override val urlAuthService: String = ""
  override val urlBotService: String = ""
  override val urlClaimsService: String = ""
  override val deepLinkHosts: List<String> = emptyList()
  override val appVersionName: String = "1.0.0"
  override val appVersionCode: String = "1"
  override val appPackageId: String = "com.hedvig.test"
  override val isDebug: Boolean = true
  override val buildApiVersion: Int = 1
  override val platformName: String = "test"
  override val model: String = "model"
  override val userAgent: String = "user-agent"
}

internal object FakeLanguageService : LanguageService {
  override val language: StateFlow<Language> = MutableStateFlow(Language.EN_SE)

  override fun setLanguage(language: Language) {}

  override fun getLocale(): CommonLocale = error("Not read by the HTTP client")
}

internal object FakeDeviceIdFetcher : DeviceIdFetcher {
  override suspend fun fetch(): String? = "device-id"
}

/** A factory for a [MockEngine] answering every request with [handler]. */
internal fun mockEngineFactory(handler: MockRequestHandler): HttpClientEngineFactory<MockEngineConfig> =
  object : HttpClientEngineFactory<MockEngineConfig> {
    override fun create(block: MockEngineConfig.() -> Unit): HttpClientEngine =
      MockEngine(MockEngineConfig().apply(block).apply { addHandler(handler) })
  }
