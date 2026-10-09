package com.hedvig.android.network.clients

import io.ktor.client.HttpClientConfig

interface ExtraKtorClientConfiguration {
  fun configure(config: HttpClientConfig<*>)
}

internal class NoopExtraKtorClientConfiguration : ExtraKtorClientConfiguration {
  override fun configure(config: HttpClientConfig<*>) {}
}
