package com.hedvig.android.feature.payments.overview.data

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

internal class GetUpcomingPaymentUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: GetUpcomingPaymentUseCase,
  override val prodImpl: GetUpcomingPaymentUseCase,
) : ProdOrDemoProvider<GetUpcomingPaymentUseCase>
