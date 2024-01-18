package com.hedvig.android.featureflags

import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import io.getunleash.UnleashClient
import io.getunleash.UnleashConfig
import io.getunleash.UnleashContext
import io.getunleash.polling.AutoPollingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val PRODUCTION_CLIENT_KEY = "*:production.d1f58888019fbbb45c3c62fe2f1999dd5295008e1f4f63edbb45d82a"
private const val DEVELOPMENT_CLIENT_KEY = "*:development.f54c919d1c96360290328d53eeda65d7c4bddaeb24e31dc0d413437d"
private const val UNLEASH_URL = "https://eu.app.unleash-hosted.com/eubb1047/api/frontend"
private const val APP_NAME = "android"

class UnleashClientProvider(
  private val isProduction: Boolean,
  private val appVersionName: String,
  marketManager: MarketManager,
  coroutineScope: CoroutineScope,
) {

  private var client = createClient(marketManager.market.value)

  init {
    marketManager.market.map {
      client = createClient(it)
    }.stateIn(
      scope = coroutineScope,
      started = SharingStarted.Eagerly,
      initialValue = null,
    )
  }

  fun provideUnleashClient() = client

  private fun createClient(market: Market) = UnleashClient(
    unleashConfig = createConfig(),
    unleashContext = createContext(
      market = market.name,
      appVersion = appVersionName,
    ),
  )

  private fun createConfig(): UnleashConfig {
    val clientKey = if (isProduction) {
      PRODUCTION_CLIENT_KEY
    } else {
      DEVELOPMENT_CLIENT_KEY
    }

    val environmentContext = clientKey.replace("*", "").split(".").first()

    return UnleashConfig.newBuilder()
      .proxyUrl(UNLEASH_URL)
      .clientKey(clientKey)
      .environment(environmentContext)
      .appName(APP_NAME)
      .pollingMode(AutoPollingMode(pollRateDuration = 2000))
      .build()
  }

  private fun createContext(
    market: String,
    appVersion: String,
  ) = UnleashContext.newBuilder()
    .properties(
      mutableMapOf(
        "appVersion" to appVersion,
        "market" to market,
      ),
    )
    .build()
}
