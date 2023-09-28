package com.hedvig.android.feature.home.di

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase

internal class GetHomeDataUseCaseProvider(
  demoManager: DemoManager,
  demoImpl: GetHomeDataUseCase,
  prodImpl: GetHomeDataUseCase,
) : ProdOrDemoProvider<GetHomeDataUseCase>(
  demoManager,
  demoImpl,
  prodImpl,
)
