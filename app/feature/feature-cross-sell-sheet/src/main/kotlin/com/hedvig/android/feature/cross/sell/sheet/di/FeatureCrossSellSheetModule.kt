package com.hedvig.android.feature.cross.sell.sheet.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCase
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.feature.cross.sell.sheet.CrossSellSheetViewModel
import com.hedvig.android.feature.cross.sell.sheet.DemoGetCrossSellSheetDataUseCase
import com.hedvig.android.feature.cross.sell.sheet.GetCrossSellSheetDataUseCase
import com.hedvig.android.feature.cross.sell.sheet.GetCrossSellSheetDataUseCaseImpl
import com.hedvig.android.feature.cross.sell.sheet.GetCrossSellSheetDataUseCaseProvider
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureCrossSellSheetModule = module {
  viewModel<CrossSellSheetViewModel> {
    CrossSellSheetViewModel(get<GetCrossSellSheetDataUseCaseProvider>(), get<CrossSellAfterFlowRepository>())
  }
  single<GetCrossSellSheetDataUseCase> {
    GetCrossSellSheetDataUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      getTravelAddonBannerInfoUseCase = get<GetTravelAddonBannerInfoUseCase>(),
    )
  }
  single<GetCrossSellSheetDataUseCaseProvider> {
    GetCrossSellSheetDataUseCaseProvider(
      demoManager = get<DemoManager>(),
      demoImpl = DemoGetCrossSellSheetDataUseCase(),
      prodImpl = get<GetCrossSellSheetDataUseCase>(),
    )
  }
}
