package com.hedvig.android.network.clients.di

import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import io.ktor.client.HttpClientConfig

internal actual fun HttpClientConfig<*>.installDatadogKtorPlugin(hedvigBuildConstants: HedvigBuildConstants) {
  // no-op
}
