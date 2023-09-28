package com.hedvig.android.feature.forever.di

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.forever.data.GetReferralsInformationUseCase

internal class GetReferralsInformationUseCaseProvider(
  demoManager: DemoManager,
  demoImpl: GetReferralsInformationUseCase,
  prodImpl: GetReferralsInformationUseCase,
) : ProdOrDemoProvider<GetReferralsInformationUseCase>(
  demoManager = demoManager,
  demoImpl = demoImpl,
  prodImpl = prodImpl,
)
