package com.hedvig.app.feature.marketpicker

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.graphql.GeoQuery
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
        val server = MockWebServer()
        server.use { webServer ->
            webServer.start(8080)
            webServer.enqueue(MockResponse().setBody(DATA.toJson()))
        }
        activityRule.launchActivity(Intent())
    }

    companion object {
        private val DATA = GeoQuery.Data(
            geo = GeoQuery.Geo(
                countryISOCode = "SE"
            )
        )
    }
}
