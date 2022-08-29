package com.hedvig.app.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.hedvig.android.market.MarketManager
import org.koin.java.KoinJavaComponent.getKoin

fun context(): Context = ApplicationProvider.getApplicationContext()

fun market() = getKoin().get<MarketManager>().market
