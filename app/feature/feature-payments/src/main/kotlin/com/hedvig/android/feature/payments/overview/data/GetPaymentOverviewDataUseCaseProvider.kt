package com.hedvig.android.feature.payments.overview.data

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

internal class GetPaymentOverviewDataUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: GetPaymentOverviewDataUseCase,
  override val prodImpl: GetPaymentOverviewDataUseCase,
) : ProdOrDemoProvider<GetPaymentOverviewDataUseCase>
