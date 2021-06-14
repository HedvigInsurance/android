package com.hedvig.app.util

import androidx.test.core.app.ApplicationProvider
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import org.koin.java.KoinJavaComponent.getKoin

fun context() = Language
    .fromSettings(ApplicationProvider.getApplicationContext(), market())
    .apply(ApplicationProvider.getApplicationContext())

fun market() = Market.SE
