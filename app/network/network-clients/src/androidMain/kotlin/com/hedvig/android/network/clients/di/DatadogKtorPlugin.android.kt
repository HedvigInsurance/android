package com.hedvig.android.network.clients.di

import com.datadog.kmp.ktor.HttpRequestSnapshot
import com.datadog.kmp.ktor.RumResourceAttributesProvider
import com.datadog.kmp.ktor.TracingHeaderType
import com.datadog.kmp.ktor.datadogKtorPlugin
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.datadog.core.network.REQUEST_CANCELLED_RUM_ATTRIBUTE
import io.ktor.client.HttpClientConfig
import io.ktor.client.statement.HttpResponse
import kotlin.coroutines.cancellation.CancellationException

internal actual fun HttpClientConfig<*>.installDatadogKtorPlugin(hedvigBuildConstants: HedvigBuildConstants) {
  install(
    datadogKtorPlugin(
      tracedHosts = mapOf(
        hedvigBuildConstants.urlGraphqlOctopus.removePrefix("""https://""") to setOf(TracingHeaderType.DATADOG),
      ),
      traceSampleRate = 100f,
      rumResourceAttributesProvider = CancellationTaggingRumResourceAttributesProvider,
    ),
  )
}

/**
 * The Datadog plugin reports every throwable as a RUM resource error, including a cancellation, which only means we
 * abandoned the request on purpose, usually because the screen that made it was left. This tags those so the RUM error
 * event mapper can drop them.
 *
 * The check has to happen here, since this is the only place the real [Throwable] is available. The error event itself
 * only carries the class name, which R8 obfuscates in release builds.
 */
private object CancellationTaggingRumResourceAttributesProvider : RumResourceAttributesProvider {
  override fun onRequest(request: HttpRequestSnapshot): Map<String, Any?> = emptyMap()

  override fun onResponse(response: HttpResponse): Map<String, Any?> = emptyMap()

  override fun onError(request: HttpRequestSnapshot, throwable: Throwable): Map<String, Any?> {
    return if (throwable is CancellationException) {
      mapOf(REQUEST_CANCELLED_RUM_ATTRIBUTE to true)
    } else {
      emptyMap()
    }
  }
}
