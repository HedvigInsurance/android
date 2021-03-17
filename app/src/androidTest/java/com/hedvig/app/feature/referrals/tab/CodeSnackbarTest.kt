package com.hedvig.app.feature.referrals.tab

import android.content.ClipboardManager
import androidx.core.content.getSystemService
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CodeSnackbarTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
            )
        },
        ReferralsQuery.QUERY_DOCUMENT to apolloResponse { success(REFERRALS_DATA_WITH_NO_DISCOUNTS) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

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
            initialTab = LoggedInTabs.REFERRALS
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
