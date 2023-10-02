package com.hedvig.android.feature.insurances.di

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase

internal class GetCrossSellsUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: GetCrossSellsUseCase,
  override val prodImpl: GetCrossSellsUseCase,
) : ProdOrDemoProvider<GetCrossSellsUseCase>
