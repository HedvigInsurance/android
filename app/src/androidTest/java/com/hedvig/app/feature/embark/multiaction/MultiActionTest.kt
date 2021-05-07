package com.hedvig.app.feature.embark.multiaction

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_MULTI_ACTION
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class MultiActionTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_MULTI_ACTION) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun errorInput() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this.javaClass.name, ""))

        step("Press add building to display bottom sheet") {
            MultiActionScreen {
                multiActionList {
                    childAt<MultiActionScreen.AddBuildingButton>(0) {
                        click()
                    }
                }
            }
        }

        step("Input more than max value") {
            AddBuildingBottomSheetScreen {
                dropDownMenu {
                    click()
                }

                onView(withText("Attefall"))
                    .inRoot(RootMatchers.isPlatformPopup())
                    .perform(click())

                numberInput {
                    typeText("15000")
                }

                numberLayout {
                    hasError("Max input")
                }
            }
        }

        step("Input less than min value") {
            AddBuildingBottomSheetScreen {
                numberInput {
                    clearText()
                    typeText("5")
                }

                numberLayout {
                    hasError("Min input")
                }

                numberInput {
                    pressImeAction()
                }
            }
        }

        step("Continue button is disabled") {
            MultiActionScreen {
                continueButton {
                    isDisabled()
                }
            }
        }
    }

    @Test
    fun addComponent() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this.javaClass.name, ""))

        step("Press add building to display bottom sheet") {
            MultiActionScreen {
                multiActionList {
                    childAt<MultiActionScreen.AddBuildingButton>(0) {
                        click()
                    }
                }
            }
        }

        step("Add building") {
            AddBuildingBottomSheetScreen {
                dropDownMenu {
                    click()
                }

                onView(withText("Attefall"))
                    .inRoot(RootMatchers.isPlatformPopup())
                    .perform(click())

                numberInput {
                    typeText("23")
                    pressImeAction()
                }
            }

            MultiActionScreen {
                continueButton {
                    isEnabled()
                    click()
                }
            }
        }

        step("Check that added building is visible") {
            MultiActionScreen {
                multiActionList {
                    childAt<MultiActionScreen.Component>(1) {
                        title.hasText("Attefall")
                    }
                }
            }
        }
    }

    @Test
    fun addMaxComponents() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this.javaClass.name, ""))

        step("Press add building to display bottom sheet") {
            MultiActionScreen {
                multiActionList {
                    childAt<MultiActionScreen.AddBuildingButton>(0) {
                        click()
                    }
                }
            }
        }

        step("Add building") {
            AddBuildingBottomSheetScreen {
                dropDownMenu {
                    click()
                }

                onView(withText("Attefall"))
                    .inRoot(RootMatchers.isPlatformPopup())
                    .perform(click())

                numberInput {
                    typeText("23")
                    pressImeAction()
                }
            }

            MultiActionScreen {
                continueButton {
                    isEnabled()
                    click()
                }
            }
        }

        step("Press add building to display bottom sheet") {
            MultiActionScreen {
                multiActionList {
                    childAt<MultiActionScreen.AddBuildingButton>(0) {
                        click()
                    }
                }
            }
        }

        step("Add another building") {
            AddBuildingBottomSheetScreen {
                dropDownMenu {
                    click()
                }

                onView(withText("Friggebod"))
                    .inRoot(RootMatchers.isPlatformPopup())
                    .perform(click())

                numberInput {
                    typeText("124")
                    pressImeAction()
                }
            }

            MultiActionScreen {
                continueButton {
                    isEnabled()
                    click()
                }
            }
        }

        step("Check that add building button is not visible") {
            MultiActionScreen {
                multiActionList {
                    childAt<MultiActionScreen.Component>(0) {
                        title.hasText("Attefall")
                    }
                    childAt<MultiActionScreen.Component>(1) {
                        title.hasText("Friggebod")
                    }
                    hasSize(2)
                }
            }
        }
    }
}
