package com.hedvig.android.hanalytics

/**
 * Clears existing stored experiments
 */
interface HAnalyticsExperimentClearUseCase {
  suspend fun invoke()
}

internal class HAnalyticsExperimentClearUseCaseImpl(
  private val hAnalyticsExperimentStorage: HAnalyticsExperimentStorage,
) : HAnalyticsExperimentClearUseCase {
  override suspend fun invoke() {
    hAnalyticsExperimentStorage.invalidateExperiments()
  }
}
