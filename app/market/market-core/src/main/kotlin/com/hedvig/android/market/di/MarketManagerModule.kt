package com.hedvig.android.market.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.market.MarketManager
import com.hedvig.android.market.MarketManagerImpl
import com.hedvig.android.market.MarketStorage
import com.hedvig.android.market.SetMarketUseCase
import com.hedvig.android.market.SetMarketUseCaseImpl
import org.koin.dsl.module

val marketManagerModule = module {
  single<MarketManager> { MarketManagerImpl(get<MarketStorage>(), get<ApplicationScope>()) }
  single<MarketStorage> { MarketStorage(get<DataStore<Preferences>>(), get<Context>()) }
  single<SetMarketUseCase> { SetMarketUseCaseImpl(get<MarketStorage>()) }
}
