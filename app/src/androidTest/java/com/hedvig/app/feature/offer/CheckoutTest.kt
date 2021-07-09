package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.feature.offer.ui.checkout.CheckoutActivity
import com.hedvig.app.feature.offer.ui.checkout.CheckoutParameter
import com.hedvig.app.testdata.feature.offer.BUNDLE_GROSS_COST
import com.hedvig.app.testdata.feature.offer.BUNDLE_NAME
import com.hedvig.app.testdata.feature.offer.BUNDLE_NET_COST
import com.hedvig.app.testdata.feature.offer.BUNDLE_WITH_SIMPLE_SIGN
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.market
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.javamoney.moneta.Money
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
            title.hasText(BUNDLE_NAME)
            val netAmount = Money.of(BUNDLE_NET_COST.toBigDecimal(), "SEK").format(context(), market())
            val netString = context().getString(R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, netAmount)
            cost.hasText(netString)

            val grossAmount = Money.of(BUNDLE_GROSS_COST.toBigDecimal(), "SEK").format(context(), market())
            originalCost.hasText(grossAmount)
        }
    }
}
