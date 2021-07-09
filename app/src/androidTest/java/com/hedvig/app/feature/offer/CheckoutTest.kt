package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.feature.offer.ui.checkout.CheckoutActivity
import com.hedvig.app.feature.offer.ui.checkout.CheckoutParameter
import com.hedvig.app.testdata.feature.offer.BUNDLE_NAME
import com.hedvig.app.testdata.feature.offer.BUNDLE_WITH_SIMPLE_SIGN
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class CheckoutTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(OfferActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse { success(BUNDLE_WITH_SIMPLE_SIGN) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldDisplayCostAndBundleName() {
        activityRule.launch(CheckoutActivity.newInstance(context(), CheckoutParameter(listOf("123"))))

        CheckoutScreen {
            title { hasText(BUNDLE_NAME) }
            originalCost.hasText("449 kr")
            cost.hasText("349 kr/mo.")
        }
    }
}
