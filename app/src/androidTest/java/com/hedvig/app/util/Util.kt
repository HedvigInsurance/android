package com.hedvig.app.util

import androidx.test.core.app.ApplicationProvider
import com.hedvig.android.market.Language
import com.hedvig.android.market.MarketManager
import org.koin.java.KoinJavaComponent.getKoin

fun context() = Language
  .fromSettings(ApplicationProvider.getApplicationContext(), market())
  .apply(ApplicationProvider.getApplicationContext())

fun market() = getKoin().get<MarketManager>().market
