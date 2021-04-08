package com.hedvig.app.feature.embark.textaction

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.clockModule
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.screens.TextActionScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_BIRTH_DATE_REVERSE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class TextActionBirthDateReverseMaskTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_TEXT_ACTION_BIRTH_DATE_REVERSE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @get:Rule
    val mockModuleRule = KoinMockModuleRule(
        listOf(clockModule),
        listOf(
            module {
                single {
                    Clock.fixed(
                        Instant.parse("2021-02-03T09:54:00.00Z"),
                        ZoneId.of("Europe/Stockholm")
                    )
                }
            }
        )
    )

    @Test
    fun shouldSaveCorrectDataWhenMaskIsBirthDateReverse() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name,
                storyTitle
            )
        )

        TextActionScreen {
            step("Enter reverse birth date") {
                input { edit { replaceText("13-10-1931") } }
                submitButton { click() }
            }
        }
        onScreen<EmbarkScreen> {
            step("Verify that data has been stored in reverse, and that values have been derived") {
                messages {
                    firstChild<EmbarkScreen.MessageRow> {
                        text { hasText("1931-10-13 was entered. 89 was derived.") }
                    }
                }
            }
        }
    }

    @Test
    fun shouldReapplyMaskWhenReloadingDataFromStore() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name,
                storyTitle
            )
        )

        TextActionScreen {
            step("Enter information and submit") {
                input { edit { replaceText("13-10-1931") } }
                submitButton { click() }
            }
            step("Ensure that new passage is showing") {
                onScreen<EmbarkScreen> {
                    messages {
                        firstChild<EmbarkScreen.MessageRow> {
                            text { hasText("1931-10-13 was entered. 89 was derived.") }
                        }
                    }
                }
            }
            step("Navigate back and verify that data has been correctly reloaded from store") {
                pressBack()
                input { edit { hasText("13-10-1931") } }
            }
            step("Verify that reloaded data passes validation") {
                submitButton { isEnabled() }
            }
        }
    }
}
