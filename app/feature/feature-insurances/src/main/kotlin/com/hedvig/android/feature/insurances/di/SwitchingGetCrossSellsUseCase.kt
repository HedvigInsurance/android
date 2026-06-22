package com.hedvig.android.feature.insurances.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCaseDemo
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCaseImpl
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<GetCrossSellsUseCase>())
internal class SwitchingGetCrossSellsUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: GetCrossSellsUseCaseImpl,
  override val demoImpl: GetCrossSellsUseCaseDemo,
) : GetCrossSellsUseCase, DemoSwitcher<GetCrossSellsUseCase>() {
  override suspend fun invoke() = pick().invoke()
}
