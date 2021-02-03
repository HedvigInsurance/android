package com.hedvig.app

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.KoinMockModuleRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.koin.dsl.module

abstract class MarketTest(val market: Market) : TestCase() {

    val marketManager = mockk<MarketManager>(relaxed = true)

    @get:Rule
    val mockModuleRule = KoinMockModuleRule(
        listOf(marketManagerModule),
        listOf(module { single { marketManager } })
    )

    @Before
    fun setup() {
        every { marketManager.market } returns market
    }
}
