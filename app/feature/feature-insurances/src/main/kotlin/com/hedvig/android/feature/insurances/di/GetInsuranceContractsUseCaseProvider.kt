package com.hedvig.android.feature.insurances.di

import com.hedvig.android.feature.demomode.DemoManager
import com.hedvig.android.feature.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase

internal class GetInsuranceContractsUseCaseProvider(
  demoManager: DemoManager,
  demoImpl: GetInsuranceContractsUseCase,
  prodImpl: GetInsuranceContractsUseCase,
) : ProdOrDemoProvider<GetInsuranceContractsUseCase>(
  demoManager,
  demoImpl,
  prodImpl,
)
