package com.hedvig.app.feature.referrals.editcode

import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.jsonObjectOf
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class GenericErrorTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(ReferralsEditCodeActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        UpdateReferralCampaignCodeMutation.QUERY_DOCUMENT to apolloResponse {
            graphQLError(jsonObjectOf("message" to "example message"))
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowErrorWhenNetworkErrorOccurs() = run {
        activityRule.launch(
            ReferralsEditCodeActivity.newInstance(
                context(),
                "TEST123"
            )
        )

        onScreen<ReferralsEditCodeScreen> {
            editLayout {
                edit {
                    replaceText("EDITEDCODE123")
                }
            }
            save { click() }
            editLayout {
                isErrorEnabled()
                hasError(R.string.referrals_change_code_sheet_general_error)
            }
        }
    }
}
