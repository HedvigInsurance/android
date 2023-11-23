package com.hedvig.android.feature.editcoinsured.di

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase

internal class GetCoInsuredUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: GetCoInsuredUseCase,
  override val prodImpl: GetCoInsuredUseCase,
) : ProdOrDemoProvider<GetCoInsuredUseCase>
