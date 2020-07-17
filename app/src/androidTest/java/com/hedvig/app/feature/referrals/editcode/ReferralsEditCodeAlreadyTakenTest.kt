package com.hedvig.app.feature.referrals.editcode

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_ALREADY_TAKEN
import com.hedvig.app.util.apolloMockServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.KoinComponent
import org.koin.core.inject

@RunWith(AndroidJUnit4::class)
class ReferralsEditCodeAlreadyTakenTest : KoinComponent {
    private val apolloClientWrapper: ApolloClientWrapper by inject()

    @get:Rule
    val activityRule = ActivityTestRule(ReferralsEditCodeActivity::class.java, false, false)

    @Before
    fun setup() {
        apolloClientWrapper
            .apolloClient
            .clearNormalizedCache()
    }

    @Test
    fun shouldShowAlreadyTakenErrorWhenCodeIsAlreadyTaken() {
        apolloMockServer(
            UpdateReferralCampaignCodeMutation.OPERATION_NAME to EDIT_CODE_DATA_ALREADY_TAKEN
        ).use { webServer ->

            webServer.start(8080)

            activityRule.launchActivity(
                ReferralsEditCodeActivity.newInstance(
                    ApplicationProvider.getApplicationContext(),
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
                    hasError(
                        ApplicationProvider.getApplicationContext<Context>()
                            .getString(R.string.referrals_change_code_sheet_error_claimed_code)
                    )
                }
            }
        }
    }
}
