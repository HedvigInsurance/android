package com.hedvig.android.feature.insurances.insurancedetail.data

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

internal class GetContractDetailsUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: GetContractDetailsUseCaseDemo,
  override val prodImpl: GetContractDetailsUseCaseImpl,
) : ProdOrDemoProvider<GetContractDetailsUseCase>
