package com.hedvig.app.feature.embark.multiaction

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
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
            AddComponentBottomSheetScreen {
                dropDownMenu {
                    click()
                }

                list {
                    inRoot {
                        isPlatformPopup()
                    }

                    isVisible()
                    hasSize(3)

                    childWith<AddComponentBottomSheetScreen.Item> {
                        isInstanceOf(String::class.java)
                        equals("Attefall")
                    } perform {
                        text {
                            isDisplayed()
                            click()
                        }
                    }
                }

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
            AddComponentBottomSheetScreen {
                dropDownMenu {
                    click()
                }

                list {
                    inRoot {
                        isPlatformPopup()
                    }

                    isVisible()
                    hasSize(3)

                    childWith<AddComponentBottomSheetScreen.Item> {
                        isInstanceOf(String::class.java)
                        equals("Attefall")
                    } perform {
                        text {
                            isDisplayed()
                            click()
                        }
                    }
                }

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
            AddComponentBottomSheetScreen {
                dropDownMenu {
                    click()
                }

                list {
                    inRoot {
                        isPlatformPopup()
                    }

                    isVisible()
                    hasSize(3)

                    childWith<AddComponentBottomSheetScreen.Item> {
                        isInstanceOf(String::class.java)
                        equals("Friggebod")
                    } perform {
                        text {
                            isDisplayed()
                            click()
                        }
                    }
                }

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
