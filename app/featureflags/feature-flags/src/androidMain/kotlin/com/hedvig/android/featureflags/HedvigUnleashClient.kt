package com.hedvig.android.featureflags

import android.content.Context
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.logger.logcat
import io.getunleash.android.DefaultUnleash
import io.getunleash.android.UnleashConfig
import io.getunleash.android.data.UnleashContext
import io.getunleash.android.events.HeartbeatEvent
import io.getunleash.android.events.UnleashFetcherHeartbeatListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val PRODUCTION_CLIENT_KEY = "*:production.21d6af57ae16320fde3a3caf024162db19cc33bf600ab7439c865c20"
private const val DEVELOPMENT_CLIENT_KEY = "*:development.f2455340ac9d599b5816fa879d079f21dd0eb03e4315130deb5377b6"
private const val UNLEASH_URL = "https://eu.app.unleash-hosted.com/eubb1047/api/frontend"
private const val APP_NAME = "android"

class HedvigUnleashClient(
  private val androidContext: Context,
  private val isProduction: Boolean,
  private val appVersionName: String,
  coroutineScope: CoroutineScope,
  private val memberIdService: MemberIdService,
) {
  val client = DefaultUnleash(
    androidContext = androidContext,
    unleashConfig = createConfig(),
    unleashContext = createContext(
      appVersion = appVersionName,
      memberId = null,
    ),
  )
  val featureUpdatedFlow: Flow<Unit> = callbackFlow {
    trySend(Unit)
    val listener = object : UnleashFetcherHeartbeatListener {
      override fun onError(event: HeartbeatEvent) {
      }

      override fun togglesChecked() {
      }

      override fun togglesUpdated() {
        trySend(Unit)
      }
    }

    client.addUnleashEventListener(listener = listener)
    awaitClose {
      client.removeUnleashEventListener(listener = listener)
    }
  }

  init {
    coroutineScope.launch {
      memberIdService.getMemberId().collectLatest { memberId: String? ->
        client.setContextAsync(
          createContext(
            appVersion = appVersionName,
            memberId = memberId,
          ),
        )
      }
    }
    client.start()
  }

  private fun createConfig(): UnleashConfig {
    val clientKey = if (isProduction) {
      PRODUCTION_CLIENT_KEY
    } else {
      DEVELOPMENT_CLIENT_KEY
    }

    return UnleashConfig.newBuilder(APP_NAME)
      .proxyUrl(UNLEASH_URL)
      .clientKey(clientKey)
      .pollingStrategy.interval(2000)
      .metricsStrategy.interval(2000)
      .build()
  }

  private fun createContext(appVersion: String, memberId: String?): UnleashContext {
    return UnleashContext.newBuilder()
      .properties(
        buildMap {
          put("appVersion", appVersion)
          put("appName", APP_NAME)
          put("market", "SE")
          if (memberId != null) {
            put("memberId", memberId)
          }
        }.toMutableMap(),
      )
      .build()
  }
}
