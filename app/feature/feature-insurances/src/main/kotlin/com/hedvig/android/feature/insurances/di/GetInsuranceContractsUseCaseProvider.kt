package com.hedvig.android.feature.insurances.di

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase

internal class GetInsuranceContractsUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: GetInsuranceContractsUseCase,
  override val prodImpl: GetInsuranceContractsUseCase,
) : ProdOrDemoProvider<GetInsuranceContractsUseCase>
