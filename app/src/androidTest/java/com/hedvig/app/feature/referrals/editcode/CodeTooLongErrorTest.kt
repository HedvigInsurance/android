package com.hedvig.app.feature.referrals.editcode

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_TOO_LONG
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class CodeTooLongErrorTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(ReferralsEditCodeActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        UpdateReferralCampaignCodeMutation.QUERY_DOCUMENT to apolloResponse {
            success(
                EDIT_CODE_DATA_TOO_LONG
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldShowErrorWhenCodeIsTooLong() = run {
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
                hasError(R.string.referrals_change_code_sheet_error_max_length)
            }
        }
    }
}
