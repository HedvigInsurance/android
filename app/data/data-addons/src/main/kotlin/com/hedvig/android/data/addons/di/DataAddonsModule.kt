package com.hedvig.android.data.addons.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.addons.data.DemoGetTravelAddonBannerInfoUseCase
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCase
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCaseImpl
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCaseProvider
import com.hedvig.android.featureflags.FeatureManager
import org.koin.dsl.module

val dataAddonsModule = module {
  single<GetTravelAddonBannerInfoUseCase> {
    GetTravelAddonBannerInfoUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
    )
  }
  single<GetTravelAddonBannerInfoUseCaseProvider> {
    GetTravelAddonBannerInfoUseCaseProvider(
      demoManager = get<DemoManager>(),
      demoImpl = DemoGetTravelAddonBannerInfoUseCase(),
      prodImpl = get<GetTravelAddonBannerInfoUseCase>(),
    )
  }
}
