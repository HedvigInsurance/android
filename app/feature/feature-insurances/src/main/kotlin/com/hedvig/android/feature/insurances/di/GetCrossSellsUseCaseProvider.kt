package com.hedvig.android.feature.insurances.di

import com.hedvig.android.feature.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase

internal class GetCrossSellsUseCaseProvider(
  demoManager: DemoManager,
  demoImpl: GetCrossSellsUseCase,
  prodImpl: GetCrossSellsUseCase,
) : ProdOrDemoProvider<GetCrossSellsUseCase>(
  demoManager,
  demoImpl,
  prodImpl,
)
