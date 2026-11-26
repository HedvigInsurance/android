package com.hedvig.android.feature.home.yearinreview.data

import com.apollographql.cache.normalized.FetchPolicy
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/** Whether the member has a year in review to open from the home screen. */
internal interface IsYearInReviewAvailableUseCase {
  fun invoke(): Flow<Boolean>
}

@ContributesBinding(AppScope::class)
@Inject
internal class IsYearInReviewAvailableUseCaseImpl(
  private val featureManager: FeatureManager,
  private val getYearInReviewUseCase: GetYearInReviewUseCase,
) : IsYearInReviewAvailableUseCase {
  @OptIn(ExperimentalCoroutinesApi::class)
  override fun invoke(): Flow<Boolean> = featureManager.isFeatureEnabled(Feature.YEAR_IN_REVIEW)
    .flatMapLatest { isEnabled ->
      if (!isEnabled) {
        flowOf(false)
      } else {
        flow { emit(getYearInReviewUseCase.invoke(FetchPolicy.CacheFirst).isRight()) }
      }
    }
}
