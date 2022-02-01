package com.hedvig.app.feature.referrals.tab

import android.content.ClipboardManager
import androidx.core.content.getSystemService
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.campaignModule
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.ui.tab.CampaignUseCase
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class CodeSnackbarTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

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
    val apolloCacheClearRule = ApolloCacheClearRule()

    private val mockCampaignUseCase = mockk<CampaignUseCase>()

    @get:Rule
    val mockCampaignRule = KoinMockModuleRule(
        listOf(campaignModule),
        listOf(module { single { mockCampaignUseCase } })
    )

    @Before
    fun setup() {
        coEvery { mockCampaignUseCase.shouldShowCampaign() }.returns(false)

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
