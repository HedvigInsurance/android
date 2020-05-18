package com.hedvig.app.feature.marketpicker

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.R
import com.hedvig.app.feature.marketpicker.screens.MarketPickerScreen
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
            webServer.enqueue(MockResponse().setBody(DATA.toJson()))

            activityRule.launchActivity(Intent())

            onScreen<MarketPickerScreen> {
                marketRecyclerView {
                    firstChild<MarketPickerScreen.MarketItem> {
                        isClickable()
                        radioButton {
                            isNotChecked()
                        }
                    }
                    lastChild<MarketPickerScreen.MarketItem> {
                        isClickable()
                        radioButton {
                            isChecked()
                        }
                    }
                }
                languageRecyclerView {
                    firstChild<MarketPickerScreen.LanguageItem> {
                        languageText { R.string.swedish }
                    }
                    lastChild<MarketPickerScreen.LanguageItem> {
                        languageText {
                            hasText(R.string.english_swedish)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun checkSaveButton() {
        activityRule.launchActivity(Intent())

        onScreen<MarketPickerScreen> {
            save {
                isDisabled()
            }
            languageRecyclerView {
                firstChild<MarketPickerScreen.LanguageItem> {
                    click()
                }
            }
            save {
                click()
            }
        }
    }

    companion object {
        private val DATA = GeoQuery.Data(
            geo = GeoQuery.Geo(
                countryISOCode = "NO"
            )
        )
    }
}
