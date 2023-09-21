package com.hedvig.app.util

import com.hedvig.android.auth.di.authTokenDemoServiceModule
import com.hedvig.android.auth.di.authTokenServiceModule
import com.hedvig.android.data.forever.di.foreverDataDemoModule
import com.hedvig.android.data.forever.di.foreverDataModule
import com.hedvig.android.feature.forever.di.referralsInformationDemoUseCase
import com.hedvig.android.feature.forever.di.referralsInformationUseCase
import com.hedvig.android.feature.home.di.homeDataDemoModule
import com.hedvig.android.feature.home.di.homeDataModule
import com.hedvig.android.feature.insurances.di.crossSellsDemoUseCaseModule
import com.hedvig.android.feature.insurances.di.crossSellsUseCaseModule
import com.hedvig.android.feature.insurances.di.insuranceContractsDemoUseCaseModule
import com.hedvig.android.feature.insurances.di.insuranceContractsUseCaseModule
import com.hedvig.android.feature.profile.di.profileDemoRepositoryModule
import com.hedvig.android.feature.profile.di.profileRepositoryModule
import com.hedvig.android.hanalytics.featureflags.di.featureManagerDemoModule
import com.hedvig.android.hanalytics.featureflags.di.featureManagerModule
import com.hedvig.android.payment.di.paymentDemoModule
import com.hedvig.android.payment.di.paymentModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

fun loadDemoModules() {
  unloadKoinModules(
    listOf(
      profileRepositoryModule,
      homeDataModule,
      insuranceContractsUseCaseModule,
      crossSellsUseCaseModule,
      featureManagerModule,
      foreverDataModule,
      paymentModule,
      authTokenServiceModule,
      referralsInformationUseCase,
    ),
  )
  loadKoinModules(
    listOf(
      profileDemoRepositoryModule,
      homeDataDemoModule,
      insuranceContractsDemoUseCaseModule,
      crossSellsDemoUseCaseModule,
      featureManagerDemoModule,
      foreverDataDemoModule,
      paymentDemoModule,
      authTokenDemoServiceModule,
      referralsInformationDemoUseCase,
    ),
  )
}

fun loadProductionModules() {
  unloadKoinModules(
    listOf(
      profileDemoRepositoryModule,
      homeDataDemoModule,
      insuranceContractsDemoUseCaseModule,
      crossSellsDemoUseCaseModule,
      featureManagerDemoModule,
      foreverDataDemoModule,
      paymentDemoModule,
      authTokenDemoServiceModule,
      referralsInformationDemoUseCase,
    ),
  )
  loadKoinModules(
    listOf(
      profileRepositoryModule,
      homeDataModule,
      insuranceContractsUseCaseModule,
      crossSellsUseCaseModule,
      featureManagerModule,
      foreverDataModule,
      paymentModule,
      authTokenServiceModule,
      referralsInformationUseCase,
    ),
  )
}
