package com.hedvig.android.feature.home.di

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase

internal class GetHomeDataUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: GetHomeDataUseCase,
  override val prodImpl: GetHomeDataUseCase,
) : ProdOrDemoProvider<GetHomeDataUseCase>
