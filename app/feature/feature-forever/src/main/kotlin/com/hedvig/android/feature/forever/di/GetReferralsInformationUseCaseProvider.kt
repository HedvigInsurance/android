package com.hedvig.android.feature.forever.di

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.forever.data.GetReferralsInformationUseCase

internal class GetReferralsInformationUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: GetReferralsInformationUseCase,
  override val prodImpl: GetReferralsInformationUseCase,
) : ProdOrDemoProvider<GetReferralsInformationUseCase>
