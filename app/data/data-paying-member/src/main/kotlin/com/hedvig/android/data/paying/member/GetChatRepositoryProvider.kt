package com.hedvig.android.data.paying.member

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

class GetOnlyHasNonPayingContractsUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: GetOnlyHasNonPayingContractsUseCase,
  override val prodImpl: GetOnlyHasNonPayingContractsUseCase,
) : ProdOrDemoProvider<GetOnlyHasNonPayingContractsUseCase>
