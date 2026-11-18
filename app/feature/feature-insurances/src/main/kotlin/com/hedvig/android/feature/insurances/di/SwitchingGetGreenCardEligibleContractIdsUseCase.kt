package com.hedvig.android.feature.insurances.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import com.hedvig.android.feature.insurances.data.GetGreenCardEligibleContractIdsUseCase
import com.hedvig.android.feature.insurances.data.GetGreenCardEligibleContractIdsUseCaseDemo
import com.hedvig.android.feature.insurances.data.GetGreenCardEligibleContractIdsUseCaseImpl
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<GetGreenCardEligibleContractIdsUseCase>())
internal class SwitchingGetGreenCardEligibleContractIdsUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: GetGreenCardEligibleContractIdsUseCaseImpl,
  override val demoImpl: GetGreenCardEligibleContractIdsUseCaseDemo,
) : GetGreenCardEligibleContractIdsUseCase, DemoSwitcher<GetGreenCardEligibleContractIdsUseCase>() {
  override suspend fun invoke() = pick().invoke()
}
