package com.hedvig.android.market.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.market.InternalHedvigMarketApi
import com.hedvig.android.market.InternalSetMarketUseCase
import com.hedvig.android.market.InternalSetMarketUseCaseImpl
import com.hedvig.android.market.MarketManager
import com.hedvig.android.market.MarketManagerImpl
import com.hedvig.android.market.MarketStorage
import org.koin.dsl.module

@OptIn(InternalHedvigMarketApi::class)
val marketManagerModule = module {
  single<MarketManager> { MarketManagerImpl(get<MarketStorage>(), get<ApplicationScope>()) }
  single<MarketStorage> { MarketStorage(get<DataStore<Preferences>>()) }
  single<InternalSetMarketUseCase> { InternalSetMarketUseCaseImpl(get<MarketStorage>()) }
}
