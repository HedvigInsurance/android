package com.hedvig.app.feature.embark.previousinsurer

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.PREVIOUS_INSURER_STORY
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
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
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowPreviousInsurerInBottomSheetWhenPressingButton() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                    context(),
                    this.javaClass.name,
                    storyTitle
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
