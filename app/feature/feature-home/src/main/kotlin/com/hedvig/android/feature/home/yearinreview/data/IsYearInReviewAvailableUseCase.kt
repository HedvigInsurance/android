package com.hedvig.android.feature.home.yearinreview.data

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

/** Whether the member has a year in review to open from the home screen. */
internal interface IsYearInReviewAvailableUseCase {
  fun invoke(): Flow<Boolean>
}

// TODO: Also require the member to have a year in review from the backend once the API exists.
@ContributesBinding(AppScope::class)
@Inject
internal class IsYearInReviewAvailableUseCaseImpl(
  private val featureManager: FeatureManager,
) : IsYearInReviewAvailableUseCase {
  override fun invoke(): Flow<Boolean> = featureManager.isFeatureEnabled(Feature.YEAR_IN_REVIEW)
}
