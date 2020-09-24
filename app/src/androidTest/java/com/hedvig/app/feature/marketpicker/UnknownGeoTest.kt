package com.hedvig.app.feature.marketpicker

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.R
import com.hedvig.app.feature.marketpicker.screens.MarketPickerScreen
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.marketPickerTrackerModule
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_FI
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import io.mockk.mockk
import io.mockk.verify
import org.awaitility.Duration.TWO_SECONDS
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class UnknownGeoTest {
    @get:Rule
    val activityRule = ActivityTestRule(MarketPickerActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        GeoQuery.QUERY_DOCUMENT to apolloResponse { success(GEO_DATA_FI) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    private val tracker = mockk<MarketPickerTracker>(relaxed = true)

    private val module = module {
        single { tracker }
    }

    @Before
    fun setup() {
        unloadKoinModules(marketPickerTrackerModule)
        loadKoinModules(module)
    }

    @Test
    fun shouldNotPreselectMarketWhenUserIsInUnknownGeo() {
        activityRule.launchActivity(MarketPickerActivity.newInstance(context()))

        onScreen<MarketPickerScreen> {
            marketRecyclerView {
                childWith<MarketPickerScreen.MarketItem> {
                    withDescendant {
                        withText(R.string.sweden)
                    }
                } perform {
                    radioButton {
                        isNotChecked()
                        click()
                    }
                }
            }
            languageRecyclerView {
                childWith<MarketPickerScreen.LanguageItem> {
                    withDescendant {
                        withText(R.string.swedish)
                    }
                } perform {
                    click()
                }
            }
            scroll {
                scrollToEnd()
            }
            await atMost TWO_SECONDS untilAsserted {
                save {
                    click()
                }
            }
        }

        verify { tracker.selectMarket(Market.SE) }
        verify { tracker.selectLocale(Language.SV_SE) }
    }

    @After
    fun teardown() {
        unloadKoinModules(module)
        loadKoinModules(marketPickerTrackerModule)
    }
}
