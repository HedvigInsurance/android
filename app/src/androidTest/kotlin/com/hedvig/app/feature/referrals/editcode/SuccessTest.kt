package com.hedvig.app.feature.referrals.editcode

import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.tab.ReferralTabScreen
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_SUCCESS
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class SuccessTest : TestCase() {

  @get:Rule
  val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    LoggedInQuery.OPERATION_DOCUMENT to apolloResponse {
      success(LOGGED_IN_DATA)
    },
    ReferralsQuery.OPERATION_DOCUMENT to apolloResponse { success(REFERRALS_DATA_WITH_NO_DISCOUNTS) },
    UpdateReferralCampaignCodeMutation.OPERATION_DOCUMENT to apolloResponse {
      success(
        EDIT_CODE_DATA_SUCCESS,
      )
    },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Ignore("Succeeds locally but always fails on CI. Need to look into why")
  @Test
  fun shouldUpdateCodeWhenCodeIsAccepted() = run {
    activityRule.launch(
      LoggedInActivity.newInstance(
        context(),
        initialTab = LoggedInTabs.REFERRALS,
      ),
    )

    onScreen<ReferralTabScreen> {
      recycler {
        childAt<ReferralTabScreen.CodeItem>(2) {
          edit { click() }
        }
      }
    }

    onScreen<ReferralsEditCodeScreen> {
      editLayout {
        edit {
          hasText("TEST123")
          replaceText("EDITEDCODE123")
        }
      }
      save { click() }
    }

    onScreen<ReferralTabScreen> {
      recycler {
        childAt<ReferralTabScreen.CodeItem>(2) {
          code { hasText("EDITEDCODE123") }
        }
      }
    }
  }
}
