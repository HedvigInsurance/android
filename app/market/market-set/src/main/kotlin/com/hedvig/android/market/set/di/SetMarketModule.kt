package com.hedvig.android.market.di

import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.InternalHedvigMarketApi
import com.hedvig.android.market.InternalSetMarketUseCase
import com.hedvig.android.market.set.SetMarketUseCase
import com.hedvig.android.market.set.SetMarketUseCaseImpl
import org.koin.dsl.module

@OptIn(InternalHedvigMarketApi::class)
val setMarketModule = module {
  single<SetMarketUseCase> { SetMarketUseCaseImpl(get<InternalSetMarketUseCase>(), get<LanguageService>()) }
}
