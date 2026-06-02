package com.hedvig.android.feature.insurances.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCaseDemo
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding<Provider<GetInsuranceContractsUseCase>>())
internal class GetInsuranceContractsUseCaseProvider(
  override val demoManager: DemoManager,
  override val prodImpl: GetInsuranceContractsUseCase,
  override val demoImpl: GetInsuranceContractsUseCaseDemo,
) : ProdOrDemoProvider<GetInsuranceContractsUseCase>
