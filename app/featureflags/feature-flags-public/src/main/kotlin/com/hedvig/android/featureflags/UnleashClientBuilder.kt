package com.hedvig.android.featureflags

import io.getunleash.UnleashClient
import io.getunleash.UnleashConfig
import io.getunleash.polling.AutoPollingMode

class UnleashClientBuilder(
  private val isProduction: Boolean,
) {
  val client = UnleashClient(createConfig())

  private fun createConfig(): UnleashConfig {
    val clientKey = if (isProduction) {
      "*:production.21d6af57ae16320fde3a3caf024162db19cc33bf600ab7439c865c20"
    } else {
      "*:development.f2455340ac9d599b5816fa879d079f21dd0eb03e4315130deb5377b6"
    }

    val environmentContext = clientKey.replace("*", "").split(".").first()

    return UnleashConfig.newBuilder()
      .proxyUrl("https://eu.app.unleash-hosted.com/eubb1047/api/frontend")
      .clientKey(clientKey)
      .environment(environmentContext)
      .appName("android")
      .pollingMode(AutoPollingMode(pollRateDuration = 60 * 60))
      .build()
  }
}
