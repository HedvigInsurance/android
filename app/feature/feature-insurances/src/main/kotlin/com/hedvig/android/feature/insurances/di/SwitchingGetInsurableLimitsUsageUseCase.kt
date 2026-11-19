package com.hedvig.android.feature.insurances.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import com.hedvig.android.feature.insurances.data.GetInsurableLimitsUsageUseCase
import com.hedvig.android.feature.insurances.data.GetInsurableLimitsUsageUseCaseDemo
import com.hedvig.android.feature.insurances.data.GetInsurableLimitsUsageUseCaseImpl
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<GetInsurableLimitsUsageUseCase>())
internal class SwitchingGetInsurableLimitsUsageUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: GetInsurableLimitsUsageUseCaseImpl,
  override val demoImpl: GetInsurableLimitsUsageUseCaseDemo,
) : GetInsurableLimitsUsageUseCase, DemoSwitcher<GetInsurableLimitsUsageUseCase>() {
  override suspend fun invoke(contractId: String) = pick().invoke(contractId)
}
