package com.hedvig.app.feature.marketpicker

import android.content.Intent
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.R
import com.hedvig.app.feature.marketpicker.screens.MarketPickerScreen
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.SettingsActivity
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarketPickerActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(MarketPickerActivity::class.java, false, false)

    @Test
    fun selectCorrectMarket() {
        MockWebServer().use { webServer ->
            webServer.start(8080)
            webServer.enqueue(MockResponse().setBody(DATA.toJson()))

            activityRule.launchActivity(Intent())

            onScreen<MarketPickerScreen> {
                marketRecyclerView {
                    childWith<MarketPickerScreen.MarketItem> {
                        withDescendant {
                            withText(R.string.sweden)
                        }
                    } perform {
                        isClickable()
                        radioButton {
                            isChecked()
                        }
                    }
                    childWith<MarketPickerScreen.MarketItem> {
                        withDescendant {
                            withText(R.string.norway)
                        }
                    } perform {
                        isClickable()
                        radioButton {
                            isNotChecked()
                        }
                    }
                }
            }
        }
    }

    @Test
    fun checkSaveButton() {
        activityRule.launchActivity(Intent())

        val pref =
            PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getInstrumentation().targetContext)

        onScreen<MarketPickerScreen> {
            save {
                isDisabled()
            }
            marketRecyclerView {
                childWith<MarketPickerScreen.MarketItem> {
                    withDescendant {
                        withText(R.string.sweden)
                    }
                } perform {
                    click()
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
            save {
                click()
            }
        }
        val market = pref.getString(Market.MARKET_SHARED_PREF, null)
        val language = pref.getString(SettingsActivity.SETTING_LANGUAGE, null)

        Assert.assertThat(market, CoreMatchers.containsString(MARKET))
        Assert.assertThat(language, CoreMatchers.containsString(Language.SETTING_SV_SE))
    }

    companion object {
        private const val MARKET = "SE"
        private val DATA = GeoQuery.Data(
            geo = GeoQuery.Geo(
                countryISOCode = MARKET
            )
        )
    }
}
