package com.hedvig.android.navigation.core.di

import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.HedvigDeepLinkContainerImpl
import org.koin.dsl.module

val deepLinkModule = module {
  single<HedvigDeepLinkContainer> {
    HedvigDeepLinkContainerImpl(
      isProduction = get<HedvigBuildConstants>().isProduction,
    )
  }
}
