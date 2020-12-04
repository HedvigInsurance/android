package com.hedvig.app.feature.referrals.editcode

import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class GenericErrorTest : TestCase() {

    @get:Rule
    val activityRule = ActivityTestRule(ReferralsEditCodeActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        UpdateReferralCampaignCodeMutation.QUERY_DOCUMENT to apolloResponse {
            graphQLError("example message")
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowErrorWhenNetworkErrorOccurs() = run {
        activityRule.launchActivity(
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
