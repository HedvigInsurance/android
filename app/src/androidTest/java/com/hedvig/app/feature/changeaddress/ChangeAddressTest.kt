package com.hedvig.app.feature.changeaddress

import com.hedvig.android.owldroid.graphql.ActiveContractBundlesQuery
import com.hedvig.android.owldroid.graphql.CreateOnboardingQuoteCartMutation
import com.hedvig.android.owldroid.graphql.UpcomingAgreementQuery
import com.hedvig.app.R
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.testdata.feature.changeaddress.SELF_CHANGE_ELIGIBILITY
import com.hedvig.app.testdata.feature.changeaddress.UPCOMING_AGREEMENT_NONE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ChangeAddressTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(ChangeAddressActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        UpcomingAgreementQuery.OPERATION_DOCUMENT to apolloResponse { success(UPCOMING_AGREEMENT_NONE) },
        ActiveContractBundlesQuery.OPERATION_DOCUMENT to apolloResponse { success(SELF_CHANGE_ELIGIBILITY) },
        CreateOnboardingQuoteCartMutation.OPERATION_DOCUMENT to apolloResponse {
            success(
                CreateOnboardingQuoteCartMutation.Data(
                    onboardingQuoteCart_create = CreateOnboardingQuoteCartMutation.OnboardingQuoteCart_create(
                        id = "123",
                    ),
                ),
            )
        },
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

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
