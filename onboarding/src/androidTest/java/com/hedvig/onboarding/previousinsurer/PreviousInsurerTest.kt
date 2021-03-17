package com.hedvig.onboarding.previousinsurer

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.PREVIOUS_INSURER_STORY
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class PreviousInsurerTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(PREVIOUS_INSURER_STORY) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldShowPreviousInsurerInBottomSheetWhenPressingButton() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name
            )
        )

        onScreen<EmbarkScreen> {
            step("Continue button should be disabled if no selection") {
                continueButton {
                    isDisabled()
                }
            }

            step("Open previous insurer bottom sheet") {
                previousInsurerButton {
                    click()
                }
            }

            step("Select insurer") {
                PreviousInsurerBottomSheetScreen {
                    recycler {
                        childAt<PreviousInsurerBottomSheetScreen.PreviousInsurer>(1) {
                            text { hasText("IF") }
                            click()
                        }
                    }
                }
            }

            step("Should show selected insurer in button") {
                previousInsurerButtonLabel {
                    hasText("IF")
                }
            }

            step("Continue to next view") {
                continueButton {
                    click()
                }
            }

            step("Check that selected insurer is visible on next view") {
                messages {
                    childAt<EmbarkScreen.MessageRow>(0) {
                        text { hasText("IF was entered") }
                    }
                }
            }
        }
    }
}
