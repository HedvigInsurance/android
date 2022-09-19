package com.hedvig.app.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.hedvig.android.market.MarketManager
import com.hedvig.app.LanguageService
import org.koin.java.KoinJavaComponent.getKoin

fun context(): Context = ApplicationProvider.getApplicationContext()

fun market() = getKoin().get<MarketManager>().market

fun locale() = getKoin().get<LanguageService>().getLocale()
