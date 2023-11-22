package com.hedvig.android.feature.editcoinsured.di

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase

internal class FetchCoInsuredPersonalInformationUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: FetchCoInsuredPersonalInformationUseCase,
  override val prodImpl: FetchCoInsuredPersonalInformationUseCase
) : ProdOrDemoProvider<FetchCoInsuredPersonalInformationUseCase>
