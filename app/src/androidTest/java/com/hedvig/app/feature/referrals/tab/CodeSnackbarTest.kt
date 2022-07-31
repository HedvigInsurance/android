package com.hedvig.app.feature.referrals.tab

import android.content.ClipboardManager
import androidx.core.content.getSystemService
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.FeatureFlagRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CodeSnackbarTest : TestCase() {

  @get:Rule
  val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    LoggedInQuery.OPERATION_DOCUMENT to apolloResponse {
      success(LOGGED_IN_DATA)
    },
    ReferralsQuery.OPERATION_DOCUMENT to apolloResponse { success(REFERRALS_DATA_WITH_NO_DISCOUNTS) },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @get:Rule
  val featureFlagRule = FeatureFlagRule(
    Feature.REFERRAL_CAMPAIGN to false,
    Feature.REFERRALS to true,
    Feature.KEY_GEAR to false,
  )

  @Before
  fun setup() {
    runCatching {
      context()
        .getSystemService<ClipboardManager>()
        ?.clearPrimaryClip()
    }
  }

  @Test
  fun shouldShowSnackbarWhenClickingCode() = run {
    val intent = LoggedInActivity.newInstance(
      context(),
      initialTab = LoggedInTabs.REFERRALS,
    )

    activityRule.launch(intent)

    Screen.onScreen<ReferralTabScreen> {
      share { isVisible() }
      recycler {
        hasSize(3)
        childAt<ReferralTabScreen.CodeItem>(2) {
          placeholder { isGone() }
          code {
            isVisible()
            hasText("TEST123")
            longClick()
          }
        }
      }
      codeCopied {
        isDisplayed()
      }
    }

    activityRule.scenario.onActivity {
      val clipboardContent = context()
        .getSystemService<ClipboardManager>()?.primaryClip?.getItemAt(0)?.text
      assertThat(clipboardContent).isEqualTo("TEST123")
    }
  }
}
