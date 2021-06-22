package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class MultipleQuotesTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(OfferActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse { success(OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun givenBundleWithMultipleQuotesShouldShowQuoteDetailScreens() = run {
        activityRule.launch()

        OfferScreen {

        }
    }
}
