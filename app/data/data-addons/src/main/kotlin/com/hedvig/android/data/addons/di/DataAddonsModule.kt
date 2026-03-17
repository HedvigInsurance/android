package com.hedvig.android.data.addons.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.addons.data.DemoGetAddonBannerInfoUseCase
import com.hedvig.android.data.addons.data.GetAddonBannerInfoUseCase
import com.hedvig.android.data.addons.data.GetAddonBannerInfoUseCaseImpl
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCaseProvider
import com.hedvig.android.featureflags.FeatureManager
import org.koin.dsl.module

val dataAddonsModule = module {
  single<GetAddonBannerInfoUseCase> {
    GetAddonBannerInfoUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
    )
  }
  single<GetTravelAddonBannerInfoUseCaseProvider> {
    GetTravelAddonBannerInfoUseCaseProvider(
      demoManager = get<DemoManager>(),
      demoImpl = DemoGetAddonBannerInfoUseCase(),
      prodImpl = get<GetAddonBannerInfoUseCase>(),
    )
  }
}
