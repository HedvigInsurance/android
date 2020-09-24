package com.hedvig.app.feature.marketpicker

import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.R
import com.hedvig.app.feature.marketpicker.screens.MarketPickerScreen
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.marketPickerTrackerModule
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_SE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
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
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class KnownGeoTest {
    @get:Rule
    val activityRule = ActivityTestRule(MarketPickerActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        GeoQuery.QUERY_DOCUMENT to apolloResponse { success(GEO_DATA_SE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    private val tracker = mockk<MarketPickerTracker>(relaxed = true)

    @get:Rule
    val koinMockModuleRule = KoinMockModuleRule(
        listOf(marketPickerTrackerModule),
        listOf(module {
            single { tracker }
        })
    )

    var originalMarket: String? = null
    var originalLanguage: String? = null

    @Before
    fun setup() {
        val pref = PreferenceManager.getDefaultSharedPreferences(context())

        originalMarket = pref.getString(Market.MARKET_SHARED_PREF, null)
        originalLanguage = pref.getString(SettingsActivity.SETTING_LANGUAGE, null)

        pref
            .edit()
            .remove(Market.MARKET_SHARED_PREF)
            .remove(SettingsActivity.SETTING_LANGUAGE)
            .commit()
    }

    @Test
    fun shouldPreselectMarketWhenUserIsInSupportedGeoArea() {
        activityRule.launchActivity(MarketPickerActivity.newInstance(context()))

        onScreen<MarketPickerScreen> {
            marketRecyclerView {
                childWith<MarketPickerScreen.MarketItem> {
                    withDescendant {
                        withText(R.string.sweden)
                    }
                } perform {
                    radioButton { isChecked() }
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

        verify(exactly = 0) { tracker.selectMarket(any()) }
        verify(exactly = 1) { tracker.selectLocale(Language.SV_SE) }
        verify(exactly = 1) { tracker.submit() }

        val pref = PreferenceManager.getDefaultSharedPreferences(context())

        val market = pref.getString(Market.MARKET_SHARED_PREF, null)
        val language = pref.getString(SettingsActivity.SETTING_LANGUAGE, null)

        assertThat(market).isEqualTo("SE")
        assertThat(language).isEqualTo(Language.SETTING_SV_SE)
    }

    @After
    fun teardown() {
        PreferenceManager.getDefaultSharedPreferences(context())
            .edit()
            .putString(Market.MARKET_SHARED_PREF, originalMarket)
            .putString(SettingsActivity.SETTING_LANGUAGE, originalLanguage)
            .commit()
    }
}
