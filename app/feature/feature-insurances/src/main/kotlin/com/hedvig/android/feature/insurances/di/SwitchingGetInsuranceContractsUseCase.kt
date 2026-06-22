package com.hedvig.android.feature.insurances.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCaseDemo
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCaseImpl
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class SwitchingGetInsuranceContractsUseCase(
  private val demoManager: DemoManager,
  private val prodImpl: GetInsuranceContractsUseCaseImpl,
  private val demoImpl: GetInsuranceContractsUseCaseDemo,
) : GetInsuranceContractsUseCase {
  override fun invoke() = flow {
    emitAll(pick().invoke())
  }

  private suspend fun pick(): GetInsuranceContractsUseCase =
    if (demoManager.isDemoMode().first()) demoImpl else prodImpl
}
