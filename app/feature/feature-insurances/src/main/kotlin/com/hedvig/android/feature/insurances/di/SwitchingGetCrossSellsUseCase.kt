package com.hedvig.android.feature.insurances.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCaseDemo
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCaseImpl
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class SwitchingGetCrossSellsUseCase(
  private val demoManager: DemoManager,
  private val prodImpl: GetCrossSellsUseCaseImpl,
  private val demoImpl: GetCrossSellsUseCaseDemo,
) : GetCrossSellsUseCase {
  override suspend fun invoke() = pick().invoke()

  private suspend fun pick(): GetCrossSellsUseCase = if (demoManager.isDemoMode().first()) demoImpl else prodImpl
}
