package com.hedvig.android.data.addons.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCase
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCaseImpl
import com.hedvig.android.featureflags.FeatureManager
import org.koin.dsl.module

val dataAddonsModule = module {
  single<GetTravelAddonBannerInfoUseCase>{
    GetTravelAddonBannerInfoUseCaseImpl(
      get<ApolloClient>(),
      get<FeatureManager>()
    )
  }
}
