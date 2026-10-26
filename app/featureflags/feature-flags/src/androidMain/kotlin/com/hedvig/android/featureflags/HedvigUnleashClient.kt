package com.hedvig.android.featureflags

import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.flags.unleashKey
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import io.getunleash.android.Unleash
import io.getunleash.android.UnleashConfig
import io.getunleash.android.data.UnleashContext
import io.getunleash.android.data.UnleashState
import io.getunleash.android.events.HeartbeatEvent
import io.getunleash.android.events.UnleashFetcherHeartbeatListener
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val PRODUCTION_CLIENT_KEY = "*:production.21d6af57ae16320fde3a3caf024162db19cc33bf600ab7439c865c20"
private const val DEVELOPMENT_CLIENT_KEY = "*:development.f2455340ac9d599b5816fa879d079f21dd0eb03e4315130deb5377b6"
private const val UNLEASH_URL = "https://eu.app.unleash-hosted.com/eubb1047/api/frontend"
private const val APP_NAME = "android"
private const val MEMBER_ID_PROPERTY = "memberId"

/**
 * How long the first member id is waited for before Unleash starts without one. The token store emits its
 * current contents as soon as it is read, a logged out member's being an immediate null, so no launch
 * spends this waiting. It only stops a read that never completes from keeping Unleash unstarted.
 */
private val INITIAL_MEMBER_ID_TIMEOUT = 2.seconds

private val MEMBER_ID_RETRY_DELAY = 30.seconds

/**
 * Values used until the client holds real toggle state, for the flags whose natural absent-toggle
 * default is wrong. These are kill switches that must read as on, keeping their feature hidden,
 * until a fetch or the stored state says otherwise. An app-gating flag must never get an entry
 * here: defaulting one into its blocking state locks out members who are offline on first launch.
 */
private val neverFetchedDefaults: Map<Feature, Boolean> = mapOf(
  Feature.DISABLE_ANALYTICS to true,
  Feature.DISABLE_PUPPY_GUIDE to true,
  Feature.DISABLE_TERMINATION_REDIRECTION to true,
  Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS to true,
)

class HedvigUnleashClient internal constructor(
  private val client: Unleash,
  private val toggleStateCache: ToggleStateCache,
  private val appVersionName: String,
  coroutineScope: CoroutineScope,
  memberIds: Flow<String?>,
) {
  /**
   * The context the client currently fetches for, or null before the first one is applied. Unleash resolves
   * strategies server-side against the context a fetch carried, so toggles fetched for another member, or
   * for no member, resolve every member-dependent strategy wrongly for this one.
   */
  private val contextInEffect = MutableStateFlow<UnleashContext?>(null)

  /**
   * Emits whenever the toggle state changes, whether from a network fetch or from restoring the state a
   * previous process stored, so a flag read while offline still updates once it loads.
   */
  val featureUpdatedFlow: Flow<Unit> = toggleStateCache.state.map { }

  /** Emits whenever the toggle state or the member it must belong to changes. */
  val memberStateUpdatedFlow: Flow<Unit> = combine(toggleStateCache.state, contextInEffect) { _, _ -> }

  /**
   * The current value of [feature], falling back to [neverFetchedDefaults] until the client holds
   * real toggle state. An absent toggle reads as false, so the fallback is what gives a flag whose
   * natural polarity default is wrong the value it needs in that window.
   *
   * This does not require the state to belong to the current member: right after a login, the previous
   * state is a better answer for most flags than every flag falling back to its default. A read that must
   * be right for the member first awaits [awaitReady].
   */
  fun valueOf(feature: Feature): Boolean {
    return if (toggleStateCache.state.value != null) {
      client.isEnabled(feature.unleashKey)
    } else {
      neverFetchedDefaults[feature] ?: false
    }
  }

  /**
   * The variant [flagName] assigns to the current member, or null when the flag is off for them, has no
   * variants, or the toggles in hand were not fetched for this member (see [hasStateForCurrentMember]).
   */
  fun variantOf(flagName: String): String? {
    if (!hasStateForCurrentMember()) return null
    val variant = client.getVariant(flagName)
    return variant.name.takeIf { variant.enabled }
  }

  /**
   * Whether the toggles in hand were fetched for the member currently in effect, or for no member while
   * logged out. A restored state counts when it names the same member, whichever app version stored it.
   */
  fun hasStateForCurrentMember(): Boolean {
    val state = toggleStateCache.state.value ?: return false
    val context = contextInEffect.value ?: return false
    return state.wasFetchedForMemberOf(context)
  }

  /**
   * Suspends until the toggles in hand were fetched for the member currently in effect (see
   * [hasStateForCurrentMember]), returning immediately if they already are. Never completes while the app
   * cannot reach Unleash and holds no state for this member, so callers must impose a timeout.
   *
   * Toggle state seeded through the SDK's `bootstrap` parameter would also satisfy this, since it is
   * written into the same cache as fetched state. Defaults for the never-fetched window therefore live in
   * [neverFetchedDefaults], outside the cache.
   */
  suspend fun awaitReady() {
    combine(toggleStateCache.state, contextInEffect) { state, context ->
      state != null && context != null && state.wasFetchedForMemberOf(context)
    }.first { it }
  }

  init {
    coroutineScope.launch {
      // Before start(), so nothing fetched in this process can be replaced by the older stored state.
      toggleStateCache.restore()
      // Reading the member id must not be able to keep Unleash from starting: without a start there is no
      // fetch, and every flag would spend the session on its never-fetched default. A failed read is retried, so
      // the member still reaches the context once the token store reads again.
      val memberIdsOrNone = memberIds.retryWhen { throwable, _ ->
        logcat(LogPriority.WARN, throwable) { "Failed to read the member id for the Unleash context" }
        emit(null)
        delay(MEMBER_ID_RETRY_DELAY)
        true
      }
      // The member goes into the context before the first fetch, so that fetch already resolves member-sticky
      // strategies for them.
      applyContext(withTimeoutOrNull(INITIAL_MEMBER_ID_TIMEOUT) { memberIdsOrNone.first() })
      client.addUnleashEventListener(fetchListener)
      client.start()
      memberIdsOrNone.collect(::applyContext)
    }
  }

  private val fetchListener = object : UnleashFetcherHeartbeatListener {
    override fun togglesChecked() = adoptStateForContextInEffect()

    override fun togglesUpdated() = Unit

    override fun onError(event: HeartbeatEvent) = Unit
  }

  /**
   * A fetch answered 304, so the server's toggles for the context in effect equal the last ones it sent, which
   * are the toggles in hand. The SDK keeps one ETag across context changes and writes nothing on a 304, so a
   * state fetched for another member is tagged with the context in effect here, and stored that way.
   */
  private fun adoptStateForContextInEffect() {
    val state = toggleStateCache.state.value ?: return
    val context = contextInEffect.value ?: return
    if (state.wasFetchedForMemberOf(context)) return
    toggleStateCache.write(state.copy(context = context))
  }

  private fun applyContext(memberId: String?) {
    val context = createContext(appVersion = appVersionName, memberId = memberId)
    if (context == contextInEffect.value) return
    contextInEffect.value = context
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
            put(MEMBER_ID_PROPERTY, memberId)
          }
        }.toMutableMap(),
      )
    if (memberId != null) {
      // The property backs constraints and `memberId` stickiness. userId is the field the `default`
      // stickiness resolves, without which a partial rollout using it is re-rolled on every fetch.
      builder.userId(memberId)
    }
    return builder.build()
  }
}

private val UnleashContext.memberId: String?
  get() = properties[MEMBER_ID_PROPERTY]

/** Whether this state was fetched for the same member as [context], or for no member when [context] has none. */
private fun UnleashState.wasFetchedForMemberOf(context: UnleashContext): Boolean {
  return this.context.memberId == context.memberId
}

internal fun createUnleashConfig(isProduction: Boolean): UnleashConfig {
  val clientKey = if (isProduction) {
    PRODUCTION_CLIENT_KEY
  } else {
    DEVELOPMENT_CLIENT_KEY
  }

  return UnleashConfig.newBuilder(APP_NAME)
    .proxyUrl(UNLEASH_URL)
    .clientKey(clientKey)
    // The SDK also fetches on every app foreground and context change, so this only bounds in-session staleness.
    .pollingStrategy.interval(30_000)
    .metricsStrategy.interval(120_000)
    // ToggleStateCache stores every fetched state. The SDK's backup restores only for an identical
    // context, and the context carries the app version.
    .localStorageConfig.enabled(false)
    .build()
}
