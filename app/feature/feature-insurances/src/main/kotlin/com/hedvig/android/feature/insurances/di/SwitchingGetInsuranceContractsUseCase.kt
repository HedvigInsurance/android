package com.hedvig.android.feature.insurances.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCaseDemo
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCaseImpl
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<GetInsuranceContractsUseCase>())
internal class SwitchingGetInsuranceContractsUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: GetInsuranceContractsUseCaseImpl,
  override val demoImpl: GetInsuranceContractsUseCaseDemo,
) : GetInsuranceContractsUseCase, DemoSwitcher<GetInsuranceContractsUseCase> {
  override fun invoke() = flow {
    emitAll(pick().invoke())
  }
}
