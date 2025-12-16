package com.hedvig.android.shareddi

import com.datadog.kmp.ktor.TracingHeaderType
import com.datadog.kmp.ktor.datadogKtorPlugin
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import io.ktor.client.HttpClientConfig

internal actual fun HttpClientConfig<*>.installDatadogKtorPlugin(hedvigBuildConstants: HedvigBuildConstants) {
  install(
    datadogKtorPlugin(
      tracedHosts = mapOf(
        hedvigBuildConstants.urlGraphqlOctopus.removePrefix("""https://""") to setOf(TracingHeaderType.DATADOG),
      ),
      traceSampleRate = 100f,
    ),
  )
}
