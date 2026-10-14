package com.hedvig.android.experiments.unleash

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.featureflags.HedvigUnleashClient
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

/** The Unleash calls [UnleashExperimentProvider] needs, kept behind an interface so the provider is testable. */
internal interface ExperimentFlags {
  /** Emits whenever the toggles, or the member they must belong to, may have changed. */
  val updates: Flow<Unit>

  /** Whether the toggles in hand were fetched for the current member, or for nobody while logged out. */
  fun hasStateForCurrentMember(): Boolean

  /** Suspends until [hasStateForCurrentMember]. Never completes while that can't happen, e.g. offline. */
  suspend fun awaitStateForCurrentMember()

  /** The current member's variant of [flagName], or null when they have none or it isn't known yet. */
  fun variantOf(flagName: String): String?
}

@ContributesBinding(AppScope::class)
@Inject
internal class UnleashExperimentFlags(
  private val hedvigUnleashClient: HedvigUnleashClient,
) : ExperimentFlags {
  override val updates: Flow<Unit> get() = hedvigUnleashClient.memberStateUpdatedFlow

  override fun hasStateForCurrentMember(): Boolean = hedvigUnleashClient.hasStateForCurrentMember()

  override suspend fun awaitStateForCurrentMember() = hedvigUnleashClient.awaitReady()

  override fun variantOf(flagName: String): String? = hedvigUnleashClient.variantOf(flagName)
}
