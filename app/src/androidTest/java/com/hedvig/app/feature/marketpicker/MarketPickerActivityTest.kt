package com.hedvig.app.feature.marketpicker

import android.content.Intent
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.R
import com.hedvig.app.feature.marketpicker.screens.MarketPickerScreen
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.SettingsActivity
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
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
            webServer.enqueue(MockResponse().setBody(DATA_SE.toJson()))

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

        assertThat(market).isEqualTo("SE")
        assertThat(language).isEqualTo(Language.SETTING_SV_SE)
    }

    @Test
    fun noPreselectedMarket() {
        val pref =
            PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getInstrumentation().targetContext)

        MockWebServer().use { webServer ->
            webServer.start(8080)
            webServer.enqueue(MockResponse().setBody(DATA_FI.toJson()))

            activityRule.launchActivity(Intent())

            onScreen<MarketPickerScreen> {
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
        }

        val market = pref.getString(Market.MARKET_SHARED_PREF, null)
        val language = pref.getString(SettingsActivity.SETTING_LANGUAGE, null)

        assertThat(market).isEqualTo("SE")
        assertThat(language).isEqualTo(Language.SETTING_SV_SE)
    }

    @Test
    fun networkProblem() {
        val pref =
            PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getInstrumentation().targetContext)

        MockWebServer().use { webServer ->
            webServer.start(8080)
            webServer.enqueue(MockResponse().setBody(DATA_NO.toJson()))

            webServer.shutdown()
            activityRule.launchActivity(Intent())

            onScreen<MarketPickerScreen> {
                marketRecyclerView {
                    childWith<MarketPickerScreen.MarketItem> {
                        withDescendant {
                            withText(R.string.norway)
                        }
                    } perform {
                        click()
                    }
                }
                languageRecyclerView {
                    childWith<MarketPickerScreen.LanguageItem> {
                        withDescendant {
                            withText(R.string.norwegian)
                        }
                    } perform {
                        click()
                    }
                }
                save {
                    click()
                }
            }
        }

        val market = pref.getString(Market.MARKET_SHARED_PREF, null)
        val language = pref.getString(SettingsActivity.SETTING_LANGUAGE, null)

        assertThat(market).isEqualTo("NO")
        assertThat(language).isEqualTo(Language.SETTING_NB_NO)
    }

    companion object {
        private val DATA_SE = GeoQuery.Data(
            geo = GeoQuery.Geo(
                countryISOCode = "SE"
            )
        )
        private val DATA_FI = GeoQuery.Data(
            geo = GeoQuery.Geo(
                countryISOCode = "FI"
            )
        )
        private val DATA_NO = GeoQuery.Data(
            geo = GeoQuery.Geo(
                countryISOCode = "NO"
            )
        )
    }
}
