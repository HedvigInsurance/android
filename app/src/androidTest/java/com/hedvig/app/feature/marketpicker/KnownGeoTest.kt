package com.hedvig.app.feature.marketpicker

import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.R
import com.hedvig.app.feature.marketing.ui.MarketingActivity
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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class KnownGeoTest {
    @get:Rule
    val activityRule = ActivityTestRule(MarketingActivity::class.java, false, false)

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
    var originalShouldOpenMarketSelected = false

    @Before
    fun setup() {
        val pref = PreferenceManager.getDefaultSharedPreferences(context())

        originalMarket = pref.getString(Market.MARKET_SHARED_PREF, null)
        originalLanguage = pref.getString(SettingsActivity.SETTING_LANGUAGE, null)
        originalShouldOpenMarketSelected = pref.getBoolean(MarketingActivity.SHOULD_OPEN_MARKET_SELECTED, false)

        pref
            .edit()
            .remove(Market.MARKET_SHARED_PREF)
            .remove(SettingsActivity.SETTING_LANGUAGE)
            .remove(MarketingActivity.SHOULD_OPEN_MARKET_SELECTED)
            .commit()
    }

    @Test
    fun shouldPreselectMarketWhenUserIsInSupportedGeoArea() {
        activityRule.launchActivity(MarketingActivity.newInstance(context()))

        onScreen<MarketPickerScreen> {
            picker {
                childAt<MarketPickerScreen.Picker>(2) {
                    selectedMarket.hasText(R.string.sweden)
                }
                childAt<MarketPickerScreen.ContinueButton>(0) {
                    click()
                }
            }
        }


        verify(exactly = 0) { tracker.selectMarket(any()) }
        verify(exactly = 0) { tracker.selectLocale(any()) }
        verify(exactly = 1) { tracker.submit() }

        val pref = PreferenceManager.getDefaultSharedPreferences(context())

        val market = pref.getString(Market.MARKET_SHARED_PREF, null)
        val language = pref.getString(SettingsActivity.SETTING_LANGUAGE, null)

        assertThat(market).isEqualTo("SE")
        assertThat(language).isEqualTo(Language.SETTING_EN_SE)
    }

    @After
    fun teardown() {
        PreferenceManager.getDefaultSharedPreferences(context())
            .edit()
            .putString(Market.MARKET_SHARED_PREF, originalMarket)
            .putString(SettingsActivity.SETTING_LANGUAGE, originalLanguage)
            .putBoolean(MarketingActivity.SHOULD_OPEN_MARKET_SELECTED, originalShouldOpenMarketSelected)
            .commit()
    }
}
