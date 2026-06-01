package com.hedvig.android.feature.insurances.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCaseDemo
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCaseDemo
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface InsurancesMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideGetInsuranceContractsUseCaseProvider(
    demoManager: DemoManager,
    prodImpl: GetInsuranceContractsUseCase,
    demoImpl: GetInsuranceContractsUseCaseDemo,
  ): Provider<GetInsuranceContractsUseCase> = GetInsuranceContractsUseCaseProvider(
    demoManager = demoManager,
    demoImpl = demoImpl,
    prodImpl = prodImpl,
  )

  @Provides
  @SingleIn(AppScope::class)
  fun provideGetCrossSellsUseCaseProvider(
    demoManager: DemoManager,
    prodImpl: GetCrossSellsUseCase,
    demoImpl: GetCrossSellsUseCaseDemo,
  ): Provider<GetCrossSellsUseCase> = GetCrossSellsUseCaseProvider(
    demoManager = demoManager,
    demoImpl = demoImpl,
    prodImpl = prodImpl,
  )
}
