package com.hedvig.android.feature.home.yearinreview.ui

import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.home.yearinreview.data.GetYearInReviewUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class YearInReviewViewModel(
  getYearInReviewUseCase: GetYearInReviewUseCase,
) : MoleculeViewModel<YearInReviewEvent, YearInReviewUiState>(
    YearInReviewUiState.Loading,
    YearInReviewPresenter(getYearInReviewUseCase),
  )
