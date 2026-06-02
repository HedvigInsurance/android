package com.hedvig.android.feature.cross.sell.sheet.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.cross.sell.sheet.DemoGetCrossSellSheetDataUseCase
import com.hedvig.android.feature.cross.sell.sheet.GetCrossSellSheetDataUseCase
import com.hedvig.android.feature.cross.sell.sheet.GetCrossSellSheetDataUseCaseImpl
import com.hedvig.android.feature.cross.sell.sheet.GetCrossSellSheetDataUseCaseProvider
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface CrossSellSheetMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideGetCrossSellSheetDataUseCaseProvider(
    demoManager: DemoManager,
    apolloClient: ApolloClient,
  ): Provider<GetCrossSellSheetDataUseCase> = GetCrossSellSheetDataUseCaseProvider(
    demoManager = demoManager,
    prodImpl = GetCrossSellSheetDataUseCaseImpl(apolloClient),
    demoImpl = DemoGetCrossSellSheetDataUseCase(),
  )
}
