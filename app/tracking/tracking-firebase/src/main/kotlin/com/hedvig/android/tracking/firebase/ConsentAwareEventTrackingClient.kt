package com.hedvig.android.tracking.firebase

import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.IoDispatcher
import com.hedvig.android.core.tracking.EventTrackingClient
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Gates product analytics behind the member's [AnalyticsConsent]:
 * - [AnalyticsConsent.NOT_DECIDED]: events/screens are buffered in-memory (bounded), not forwarded.
 * - [AnalyticsConsent.GRANTED]: the buffer is flushed (stamped with `buffered_at_epoch_ms`) and
 *   subsequent events forward live.
 * - [AnalyticsConsent.DENIED]: the buffer is dropped and nothing is forwarded. Additionally, the
 *   Firebase SDK's own collection is disabled (automatic events included) by calling
 *   [EventTrackingClient.setCollectionEnabled] with `false`. Collection is re-enabled when consent
 *   returns to GRANTED, subject to the demo-mode gate. This is a product decision: DENIED must
 *   suppress automatic Firebase events, not only our custom-tracked ones.
 * Identity calls ([setUserId], [setUserProperty]) always pass through. [setCollectionEnabled] is
 * the demo-mode gate; the effective value forwarded to the delegate is an AND of the demo gate and
 * whether consent is not DENIED. Datadog is untouched by design: it carries performance and bug
 * analytics, not product analytics.
 *
 * Deliberately NOT annotated with Metro annotations: annotating it `@ContributesBinding` for
 * [EventTrackingClient] while also injecting an [EventTrackingClient] delegate would self-loop.
 * The binding is supplied by [ConsentAwareEventTrackingClientProviders] below, which hands in the
 * concrete [FirebaseEventTrackingClient] as the delegate.
 */
internal class ConsentAwareEventTrackingClient(
  private val delegate: EventTrackingClient,
  private val settingsDataStore: SettingsDataStore,
  applicationScope: CoroutineScope,
  @IoDispatcher coroutineContext: CoroutineContext,
  private val clock: () -> Long = { System.currentTimeMillis() },
) : EventTrackingClient {
  private val lock = Any()
  private var consent: AnalyticsConsent = AnalyticsConsent.NOT_DECIDED
  private var collectionRequestedEnabled: Boolean = true
  private val buffer = ArrayDeque<BufferedCall>()

  init {
    applicationScope.launch(coroutineContext) {
      settingsDataStore.observeAnalyticsConsent().collect { newConsent ->
        val toFlush: List<BufferedCall> = synchronized(lock) {
          consent = newConsent
          when (newConsent) {
            AnalyticsConsent.GRANTED -> {
              buffer.toList().also { buffer.clear() }
            }

            AnalyticsConsent.DENIED -> {
              buffer.clear()
              emptyList()
            }

            AnalyticsConsent.NOT_DECIDED -> {
              emptyList()
            }
          }
        }
        applyCollectionEnabled()
        if (toFlush.isNotEmpty()) {
          logcat { "Analytics consent granted, flushing ${toFlush.size} buffered events" }
          for (call in toFlush) {
            call.forwardTo(delegate)
          }
        }
      }
    }
  }

  override fun setCollectionEnabled(enabled: Boolean) {
    synchronized(lock) { collectionRequestedEnabled = enabled }
    applyCollectionEnabled()
  }

  private fun applyCollectionEnabled() {
    val effective = synchronized(lock) { collectionRequestedEnabled && consent != AnalyticsConsent.DENIED }
    delegate.setCollectionEnabled(effective)
  }

  override fun trackEvent(name: String, parameters: Map<String, Any?>) {
    handle(BufferedCall.Event(name, parameters, clock()))
  }

  override fun trackScreen(name: String, screenClass: String?, parameters: Map<String, Any?>) {
    handle(BufferedCall.Screen(name, screenClass, parameters, clock()))
  }

  override fun setUserId(userId: String?) {
    delegate.setUserId(userId)
  }

  override fun setUserProperty(name: String, value: String?) {
    delegate.setUserProperty(name, value)
  }

  private fun handle(call: BufferedCall) {
    val action: Action = synchronized(lock) {
      when (consent) {
        AnalyticsConsent.GRANTED -> {
          Action.Forward
        }

        AnalyticsConsent.DENIED -> {
          Action.Drop
        }

        AnalyticsConsent.NOT_DECIDED -> {
          buffer.addLast(call)
          while (buffer.size > MAX_BUFFERED_CALLS) {
            buffer.removeFirst()
          }
          Action.Drop
        }
      }
    }
    if (action == Action.Forward) {
      call.forwardTo(delegate, includeBufferedAt = false)
    }
  }

  private enum class Action { Forward, Drop }

  private sealed interface BufferedCall {
    val bufferedAtEpochMs: Long

    fun forwardTo(client: EventTrackingClient, includeBufferedAt: Boolean = true)

    data class Event(
      val name: String,
      val parameters: Map<String, Any?>,
      override val bufferedAtEpochMs: Long,
    ) : BufferedCall {
      override fun forwardTo(client: EventTrackingClient, includeBufferedAt: Boolean) {
        val params = if (includeBufferedAt) parameters + ("buffered_at_epoch_ms" to bufferedAtEpochMs) else parameters
        client.trackEvent(name, params)
      }
    }

    data class Screen(
      val name: String,
      val screenClass: String?,
      val parameters: Map<String, Any?>,
      override val bufferedAtEpochMs: Long,
    ) : BufferedCall {
      override fun forwardTo(client: EventTrackingClient, includeBufferedAt: Boolean) {
        val params = if (includeBufferedAt) parameters + ("buffered_at_epoch_ms" to bufferedAtEpochMs) else parameters
        client.trackScreen(name, screenClass, params)
      }
    }
  }

  companion object {
    private const val MAX_BUFFERED_CALLS = 200
  }
}

// Must stay public: Metro only discovers cross-module contributions with public hints (see CLAUDE.md).
@ContributesTo(AppScope::class)
@Suppress("EXPOSED_PARAMETER_TYPE")
interface ConsentAwareEventTrackingClientProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideEventTrackingClient(
    firebaseEventTrackingClient: FirebaseEventTrackingClient,
    settingsDataStore: SettingsDataStore,
    applicationScope: ApplicationScope,
    @IoDispatcher coroutineContext: CoroutineContext,
  ): EventTrackingClient = ConsentAwareEventTrackingClient(
    delegate = firebaseEventTrackingClient,
    settingsDataStore = settingsDataStore,
    applicationScope = applicationScope,
    coroutineContext = coroutineContext,
  )
}
