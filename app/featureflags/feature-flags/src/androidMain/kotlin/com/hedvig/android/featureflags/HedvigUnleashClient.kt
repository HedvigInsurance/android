package com.hedvig.android.featureflags

import android.content.Context
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.flags.unleashKey
import io.getunleash.android.DefaultUnleash
import io.getunleash.android.UnleashConfig
import io.getunleash.android.data.UnleashContext
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val PRODUCTION_CLIENT_KEY = "*:production.21d6af57ae16320fde3a3caf024162db19cc33bf600ab7439c865c20"
private const val DEVELOPMENT_CLIENT_KEY = "*:development.f2455340ac9d599b5816fa879d079f21dd0eb03e4315130deb5377b6"
private const val UNLEASH_URL = "https://eu.app.unleash-hosted.com/eubb1047/api/frontend"
private const val APP_NAME = "android"
private const val PERSISTED_STATE_FILE_NAME = "unleash_last_fetched_state.json"

/**
 * Values used until the client holds real toggle state, for the flags whose natural absent-toggle
 * default is wrong. These are kill switches that must read as on, keeping their feature hidden,
 * until a fetch or the persisted state says otherwise. An app-gating flag must never get an entry
 * here: defaulting one into its blocking state locks out members who are offline on first launch.
 */
private val neverFetchedDefaults: Map<Feature, Boolean> = mapOf(
  Feature.DISABLE_ANALYTICS to true,
  Feature.DISABLE_PUPPY_GUIDE to true,
  Feature.DISABLE_TERMINATION_REDIRECTION to true,
  Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS to true,
)

class HedvigUnleashClient(
  private val androidContext: Context,
  private val isProduction: Boolean,
  private val appVersionName: String,
  coroutineScope: CoroutineScope,
  private val memberIdService: MemberIdService,
) {
  private val toggleCache = PersistentToggleCache(File(androidContext.noBackupFilesDir, PERSISTED_STATE_FILE_NAME))

  private val client = DefaultUnleash(
    androidContext = androidContext,
    unleashConfig = createConfig(),
    unleashContext = createContext(
      appVersion = appVersionName,
      memberId = null,
    ),
    cacheImpl = toggleCache,
  )

  /**
   * Emits whenever the toggle state changes, whether from a network fetch or from the state persisted
   * by a previous process being restored, so a flag read while offline still updates once the
   * last-known-good state loads.
   */
  val featureUpdatedFlow: Flow<Unit> = toggleCache.lastFetchedState.map { }

  /**
   * The current value of [feature], falling back to [neverFetchedDefaults] until the client holds
   * real toggle state. An absent toggle reads as false, so the fallback is what gives a flag whose
   * natural polarity default is wrong the value it needs in that window.
   */
  fun valueOf(feature: Feature): Boolean {
    return if (toggleCache.lastFetchedState.value != null) {
      client.isEnabled(feature.unleashKey)
    } else {
      neverFetchedDefaults[feature] ?: false
    }
  }

  /**
   * Suspends until the client holds real toggle state, either fetched in this process or persisted by a
   * previous one, returning immediately if it already does. Never completes while the app has never
   * fetched and cannot reach Unleash, so callers must impose a timeout.
   *
   * Toggle state seeded through the SDK's `bootstrap` parameter would also satisfy this, since it is
   * written into the same cache as fetched state, and it would then be persisted as if it had been
   * fetched. Defaults for the never-fetched window therefore live in [neverFetchedDefaults], outside
   * the cache.
   */
  suspend fun awaitReady() {
    toggleCache.lastFetchedState.filterNotNull().first()
  }

  init {
    coroutineScope.launch(Dispatchers.IO) {
      toggleCache.restore()
    }
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
      // The SDK's backup writes only when the context changes and restores only an identical context,
      // and the context carries the app version. toggleCache persists every fetched state instead.
      .localStorageConfig.enabled(false)
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
