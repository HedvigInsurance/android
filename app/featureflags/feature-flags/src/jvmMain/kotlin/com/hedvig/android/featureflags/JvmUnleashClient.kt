package com.hedvig.android.featureflags

import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.flags.unleashKey
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val POLL_INTERVAL = 30.seconds

private val MEMBER_ID_RETRY_DELAY = 30.seconds

/**
 * Reads toggles from the Unleash frontend API, which lists only the toggles that are enabled for the context sent.
 *
 * Fetching runs only while something observes the flags, through [featureUpdatedFlow] or [awaitReady]: it fetches
 * when observation starts and whenever the member changes, then every [pollInterval]. The fetched state is kept in
 * memory only.
 */
class JvmUnleashClient internal constructor(
  private val httpClient: HttpClient,
  private val clientKey: String,
  private val appVersionName: String,
  memberIds: Flow<String?>,
  coroutineScope: CoroutineScope,
  private val pollInterval: Duration = POLL_INTERVAL,
) {
  private val toggleState = MutableStateFlow<ToggleState?>(null)

  /** The member the client currently fetches for, or null before the first member id is read. */
  private val memberInEffect = MutableStateFlow<MemberContext?>(null)

  private val fetchingWhileObserved: SharedFlow<Nothing> = memberIds
    .retryWhen { throwable, _ ->
      logcat(LogPriority.WARN, throwable) { "Failed to read the member id for the Unleash context" }
      emit(null)
      delay(MEMBER_ID_RETRY_DELAY)
      true
    }
    .distinctUntilChanged()
    .transformLatest<String?, Nothing> { memberId ->
      memberInEffect.value = MemberContext(memberId)
      while (true) {
        fetch(memberId)
        delay(pollInterval)
      }
    }
    .shareIn(coroutineScope, SharingStarted.WhileSubscribed())

  /** Emits whenever the toggle state changes. */
  val featureUpdatedFlow: Flow<Unit> = toggleState.map { }.whileFetching()

  /**
   * The current value of [feature], falling back to [neverFetchedDefaults] until the client holds fetched toggle
   * state. An absent toggle reads as false.
   */
  fun valueOf(feature: Feature): Boolean {
    val state = toggleState.value ?: return neverFetchedDefaults[feature] ?: false
    return feature.unleashKey in state.enabledToggleNames
  }

  /** Whether the toggles in hand were fetched for the member currently in effect, or for no member while logged out. */
  fun hasStateForCurrentMember(): Boolean {
    val state = toggleState.value ?: return false
    val member = memberInEffect.value ?: return false
    return state.memberId == member.memberId
  }

  /**
   * Suspends until the toggles in hand were fetched for the member currently in effect. Never completes while
   * Unleash cannot be reached, so callers must impose a timeout.
   */
  suspend fun awaitReady() {
    combine(toggleState, memberInEffect) { state, member ->
      state != null && member != null && state.memberId == member.memberId
    }.whileFetching().first { it }
  }

  private fun <T> Flow<T>.whileFetching(): Flow<T> = channelFlow {
    launch { fetchingWhileObserved.collect() }
    collect { send(it) }
  }

  private suspend fun fetch(memberId: String?) {
    try {
      val response = httpClient.get(UNLEASH_URL) {
        header(HttpHeaders.Authorization, clientKey)
        accept(ContentType.Application.Json)
        parameter("appName", APP_NAME)
        parameter("properties[appVersion]", appVersionName)
        parameter("properties[appName]", APP_NAME)
        parameter("properties[market]", "SE")
        if (memberId != null) {
          parameter("properties[$MEMBER_ID_PROPERTY]", memberId)
          // userId is the field the `default` stickiness resolves, without which a partial rollout using it is
          // re-rolled on every fetch.
          parameter("userId", memberId)
        }
      }
      if (!response.status.isSuccess()) {
        logcat(LogPriority.WARN) { "Unleash fetch failed with status ${response.status}" }
        return
      }
      val enabledToggleNames = json.decodeFromString<FrontendResponse>(response.bodyAsText())
        .toggles
        .filter { it.enabled }
        .map { it.name }
        .toSet()
      toggleState.value = ToggleState(enabledToggleNames = enabledToggleNames, memberId = memberId)
    } catch (exception: CancellationException) {
      throw exception
    } catch (exception: Exception) {
      logcat(LogPriority.WARN, exception) { "Unleash fetch failed" }
    }
  }
}

private class ToggleState(val enabledToggleNames: Set<String>, val memberId: String?)

private data class MemberContext(val memberId: String?)

@Serializable
private class FrontendResponse(val toggles: List<FrontendToggle>)

@Serializable
private class FrontendToggle(val name: String, val enabled: Boolean)

private val json = Json { ignoreUnknownKeys = true }
