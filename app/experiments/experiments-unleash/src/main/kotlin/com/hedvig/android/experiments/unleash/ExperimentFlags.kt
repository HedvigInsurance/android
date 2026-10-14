package com.hedvig.android.experiments.unleash

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.featureflags.HedvigUnleashClient
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

/** The Unleash calls [UnleashExperimentProvider] needs, kept behind an interface so the provider is testable. */
internal interface ExperimentFlags {
  /** Emits whenever the flag state may have changed, including when the member id in the Unleash context does. */
  val updates: Flow<Unit>

  /** Suspends until flag state exists, either restored from disk or fetched. Never completes while it can't. */
  suspend fun awaitReady()

  fun variantOf(flagName: String): String?
}

@ContributesBinding(AppScope::class)
@Inject
internal class UnleashExperimentFlags(
  private val hedvigUnleashClient: HedvigUnleashClient,
) : ExperimentFlags {
  override val updates: Flow<Unit> get() = hedvigUnleashClient.featureUpdatedFlow

  override suspend fun awaitReady() = hedvigUnleashClient.awaitReady()

  override fun variantOf(flagName: String): String? = hedvigUnleashClient.variantOf(flagName)
}
