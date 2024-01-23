package com.hedvig.android.language.di

import com.hedvig.android.language.AndroidLanguageAndMarketLaunchCheckUseCase
import com.hedvig.android.language.LanguageAndMarketLaunchCheckUseCase
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import com.hedvig.android.market.set.SetMarketUseCase
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val languageMigrationModule = module {
  single<LanguageAndMarketLaunchCheckUseCase> {
    AndroidLanguageAndMarketLaunchCheckUseCase(get<MarketManager>(), get<LanguageService>(), get<SetMarketUseCase>())
  }
}
