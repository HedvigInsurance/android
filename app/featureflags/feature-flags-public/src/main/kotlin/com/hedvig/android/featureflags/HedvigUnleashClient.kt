package com.hedvig.android.featureflags

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import io.getunleash.UnleashClient
import io.getunleash.UnleashConfig
import io.getunleash.UnleashContext
import io.getunleash.polling.AutoPollingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private const val PRODUCTION_CLIENT_KEY = "*:production.21d6af57ae16320fde3a3caf024162db19cc33bf600ab7439c865c20"
private const val DEVELOPMENT_CLIENT_KEY = "*:development.f2455340ac9d599b5816fa879d079f21dd0eb03e4315130deb5377b6"
private const val UNLEASH_URL = "https://eu.app.unleash-hosted.com/eubb1047/api/frontend"
private const val APP_NAME = "android"

class HedvigUnleashClient(
  private val isProduction: Boolean,
  private val appVersionName: String,
  marketManager: MarketManager,
  val coroutineScope: CoroutineScope,
  private val memberIdService: MemberIdService
) {
  val client = UnleashClient(
    unleashConfig = createConfig(),
    unleashContext = createContext(
      market = marketManager.market.value.name,
      appVersion = appVersionName,
      memberId = memberIdService.getMemberId().value ?: "" //todo: what if we get null?
    ),
  )
  val featureUpdatedFlow: Flow<Unit> = callbackFlow {
    trySend(Unit)
    client.addTogglesUpdatedListener {
      trySend(Unit)
    }
    awaitClose {}
  }

  init {
    coroutineScope.launch {
      marketManager.market.combine(memberIdService.getMemberId()) { market: Market, memberId: String? ->
        logcat { "mariia: memberId$memberId" }
        Pair(market,memberId) // todo: is it okay??
      }.collectLatest {
        client.updateContext(
          createContext(
            market = it.first.name,
            appVersion = appVersionName,
            memberId = it.second ?: "" //todo: what if we get null?
          ),
        )
      }
    }
  }

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
      .enableMetrics()
      .appName(APP_NAME)
      .pollingMode(AutoPollingMode(pollRateDuration = 2000))
      .build()
  }

  private fun createContext(market: String, appVersion: String, memberId: String): UnleashContext {
    return UnleashContext.newBuilder()
      .appName(APP_NAME)
      .properties(
        mutableMapOf(
          "appVersion" to appVersion,
          "appName" to APP_NAME,
          "market" to market,
          "memberId" to memberId
        ),
      )
      .build()
  }
}
