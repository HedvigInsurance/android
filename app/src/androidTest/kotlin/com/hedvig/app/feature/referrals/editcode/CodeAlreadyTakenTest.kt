package com.hedvig.app.feature.referrals.editcode

import com.hedvig.android.apollo.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_ALREADY_TAKEN
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class CodeAlreadyTakenTest : TestCase() {
  @get:Rule
  val activityRule = LazyActivityScenarioRule(ReferralsEditCodeActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    UpdateReferralCampaignCodeMutation.OPERATION_DOCUMENT to apolloResponse {
      success(
        EDIT_CODE_DATA_ALREADY_TAKEN,
      )
    },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun shouldShowAlreadyTakenErrorWhenCodeIsAlreadyTaken() = run {
    activityRule.launch(
      ReferralsEditCodeActivity.newInstance(
        context(),
        "TEST123",
      ),
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
        hasError(hedvig.resources.R.string.referrals_change_code_sheet_error_claimed_code)
      }
    }
  }
}
