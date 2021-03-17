package com.hedvig.app.util

import com.hedvig.app.feature.settings.MarketManager
import org.koin.java.KoinJavaComponent.getKoin

fun market() = getKoin().get<MarketManager>().market

