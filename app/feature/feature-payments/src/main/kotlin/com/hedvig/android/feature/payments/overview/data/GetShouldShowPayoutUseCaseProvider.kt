package com.hedvig.android.feature.payments.overview.data

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

internal class GetShouldShowPayoutUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: GetShouldShowPayoutUseCase,
  override val prodImpl: GetShouldShowPayoutUseCase,
) : ProdOrDemoProvider<GetShouldShowPayoutUseCase>
