package com.hedvig.app.feature.referrals.editcode

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_TOO_SHORT
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class CodeTooShortErrorTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(ReferralsEditCodeActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        UpdateReferralCampaignCodeMutation.QUERY_DOCUMENT to apolloResponse {
            success(
                EDIT_CODE_DATA_TOO_SHORT
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowGenericErrorWhenCodeIsTooShort() = run {
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
