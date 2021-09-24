package com.hedvig.app.feature.changeaddress

import com.hedvig.android.owldroid.graphql.ActiveContractBundlesQuery
import com.hedvig.android.owldroid.graphql.UpcomingAgreementQuery
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.R
import com.hedvig.app.apolloResponse
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.testdata.feature.changeaddress.SELF_CHANGE_ELIGIBILITY
import com.hedvig.app.testdata.feature.changeaddress.UPCOMING_AGREEMENT_NONE
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ChangeAddressTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(ChangeAddressActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        UpcomingAgreementQuery.QUERY_DOCUMENT to apolloResponse { success(UPCOMING_AGREEMENT_NONE) },
        ActiveContractBundlesQuery.QUERY_DOCUMENT to apolloResponse { success(SELF_CHANGE_ELIGIBILITY) }
    )

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun shouldShowChangeAddressWhenNoUpcomingAgreementAndSelfChangePossible() = run {
        activityRule.launch(ChangeAddressActivity.newInstance(context()))

        ChangeAddressScreen {
            title {
                hasText(R.string.moving_intro_title)
            }

            subtitle {
                hasText(R.string.moving_intro_description)
            }

            continueButton {
                hasText(R.string.moving_intro_open_flow_button_text)
            }
        }
    }
}
