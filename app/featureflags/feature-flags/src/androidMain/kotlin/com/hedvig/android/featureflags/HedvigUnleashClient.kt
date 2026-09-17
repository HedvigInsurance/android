package com.hedvig.android.featureflags

import android.content.Context
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.flags.unleashKey
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import io.getunleash.android.DefaultUnleash
import io.getunleash.android.Unleash
import io.getunleash.android.UnleashConfig
import io.getunleash.android.data.UnleashContext
import io.getunleash.android.events.UnleashReadyListener
import io.getunleash.android.events.UnleashStateListener
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

private const val PRODUCTION_CLIENT_KEY = "*:production.21d6af57ae16320fde3a3caf024162db19cc33bf600ab7439c865c20"
private const val DEVELOPMENT_CLIENT_KEY = "*:development.f2455340ac9d599b5816fa879d079f21dd0eb03e4315130deb5377b6"
private const val UNLEASH_URL = "https://eu.app.unleash-hosted.com/eubb1047/api/frontend"
private const val APP_NAME = "android"

/**
 * Values used until the client holds real toggle state, for the flags whose natural absent-toggle
 * default is wrong. These are kill switches that must read as on, keeping their feature hidden,
 * until a fetch or the local backup says otherwise. An app-gating flag must never get an entry
 * here: defaulting one into its blocking state locks out members who are offline on first launch.
 */
private val neverFetchedDefaults: Map<Feature, Boolean> = mapOf(
  Feature.DISABLE_ANALYTICS to true,
  Feature.DISABLE_PUPPY_GUIDE to true,
  Feature.DISABLE_TERMINATION_REDIRECTION to true,
  Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS to true,
)

/**
 * How long the first member id is waited for before starting Unleash without one. The token store
 * emits its current contents as soon as it is read, and a logged out member's is an immediate null,
 * so no launch spends this waiting: it exists only so a read that never completes cannot stop
 * Unleash from starting. A member id arriving afterwards is applied by the ongoing collection.
 */
private val INITIAL_MEMBER_ID_TIMEOUT = 2.seconds

private fun createUnleashConfig(isProduction: Boolean): UnleashConfig {
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

class HedvigUnleashClient internal constructor(
  private val client: Unleash,
  private val appVersionName: String,
  coroutineScope: CoroutineScope,
  private val memberIds: Flow<String?>,
) {
  constructor(
    androidContext: Context,
    isProduction: Boolean,
    appVersionName: String,
    coroutineScope: CoroutineScope,
    memberIdService: MemberIdService,
  ) : this(
    client = DefaultUnleash(
      androidContext = androidContext,
      unleashConfig = createUnleashConfig(isProduction),
    ),
    appVersionName = appVersionName,
    coroutineScope = coroutineScope,
    memberIds = memberIdService.getMemberId(),
  )

  private var contextInEffect: UnleashContext? = null

  /**
   * Whether the toggles in hand were fetched for [contextInEffect]. Unleash resolves strategies
   * server-side against the context the fetch carried, so a set fetched before the member was known
   * resolves a member-sticky strategy as if there were no member, which yields false even at a 100%
   * rollout. The SDK's own readiness only means some toggles arrived, and the anonymous set the app
   * fetches at startup satisfies it.
   */
  private val togglesMatchContext = MutableStateFlow(false)

  /**
   * Emits whenever the toggle state changes for any reason, which includes the local backup being
   * restored and not just a network fetch, so a flag read while offline still updates once the
   * last-known-good state loads.
   *
   * It also emits on readiness, because the SDK flips `isReady()` from a coroutine independent of
   * the one delivering [UnleashStateListener.onStateChanged]. A collector that observed the state
   * change first would otherwise keep the [neverFetchedDefaults] value until the next cache write,
   * which an unchanged toggle set never produces.
   */
  val featureUpdatedFlow: Flow<Unit> = callbackFlow {
    trySend(Unit)
    val listener = object : UnleashStateListener, UnleashReadyListener {
      override fun onStateChanged() {
        trySend(Unit)
      }

      override fun onReady() {
        trySend(Unit)
      }
    }

    client.addUnleashEventListener(listener = listener)
    awaitClose {
      client.removeUnleashEventListener(listener = listener)
    }
  }

  /**
   * The current value of [feature], falling back to [neverFetchedDefaults] until the client holds
   * real toggle state. An absent toggle reads as false, so the fallback is what gives a flag whose
   * natural polarity default is wrong the value it needs in that window.
   */
  fun valueOf(feature: Feature): Boolean {
    return if (client.isReady()) {
      client.isEnabled(feature.unleashKey)
    } else {
      neverFetchedDefaults[feature] ?: false
    }
  }

  /**
   * Suspends until flag values are available for the current member: Unleash reports isReady(), and
   * the toggles in hand were fetched for the context naming that member rather than an earlier
   * anonymous one (see [togglesMatchContext]). Never completes while the app has never fetched and
   * cannot reach Unleash, so callers must impose a timeout.
   */
  suspend fun awaitReady() {
    awaitClientReady()
    togglesMatchContext.first { it }
  }

  /**
   * Suspends until Unleash reports isReady(), which it sets on the first non-empty toggle set from
   * either the on-disk backup or a network fetch, returning immediately if it already has.
   *
   * Any toggle state seeded through the SDK's `bootstrap` parameter would also satisfy this, since
   * readiness is just "the toggle cache became non-empty". Seeding it would additionally stop the
   * on-disk backup from ever loading, which the SDK only attempts while not yet ready. Defaults for
   * the never-fetched window therefore live in [neverFetchedDefaults], outside the SDK's cache.
   */
  private suspend fun awaitClientReady() {
    if (client.isReady()) return
    suspendCancellableCoroutine { continuation ->
      val listener = object : UnleashReadyListener {
        override fun onReady() {
          if (continuation.isActive) continuation.resume(Unit)
        }
      }
      client.addUnleashEventListener(listener)
      // Guard the window between the isReady() check above and registering the listener, where onReady
      // could have fired with no one listening.
      if (client.isReady() && continuation.isActive) continuation.resume(Unit)
      continuation.invokeOnCancellation { client.removeUnleashEventListener(listener) }
    }
  }

  init {
    client.addUnleashEventListener(
      object : UnleashStateListener {
        override fun onStateChanged() {
          togglesMatchContext.value = true
        }
      },
    )
    coroutineScope.launch {
      // Reading the member id must not be able to keep Unleash from starting: without a start there
      // is no fetch, and every flag would spend the session on its never-fetched default.
      val memberIdsOrNone = memberIds.catch { throwable ->
        logcat(LogPriority.ERROR, throwable) { "Failed to read the member id for the Unleash context" }
        emit(null)
      }
      // Put the member in the context before the first fetch rather than switching to them after
      // it, so that fetch already resolves member-sticky strategies for them, and so the on-disk
      // backup, which the SDK keys by context, still matches on the next launch.
      applyContext(withTimeoutOrNull(INITIAL_MEMBER_ID_TIMEOUT) { memberIdsOrNone.first() })
      client.start()
      memberIdsOrNone.collect { memberId: String? -> applyContext(memberId) }
    }
  }

  /**
   * Points the client at [memberId]'s context, marking the toggles in hand as no longer matching it
   * until the fetch the change triggers lands. Called only from the single collecting coroutine.
   */
  private fun applyContext(memberId: String?) {
    val context = createContext(appVersion = appVersionName, memberId = memberId)
    if (context == contextInEffect) return
    contextInEffect = context
    togglesMatchContext.value = false
    client.setContextAsync(context)
  }

  private fun createContext(appVersion: String, memberId: String?): UnleashContext {
    val builder = UnleashContext.newBuilder()
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
    if (memberId != null) {
      // The property backs constraints and `memberId` stickiness; userId is the standard field the
      // `default` stickiness resolves, without which a partial rollout re-dices on every poll.
      builder.userId(memberId)
    }
    return builder.build()
  }
}
