package com.hedvig.app.feature.embark

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.screens.TextActionScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.zignsec.ZignSecAuthenticationActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class EmbarkMenuTest : TestCase() {

    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_TEXT_ACTION) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    val authIntent = KIntent {
        hasComponent(ZignSecAuthenticationActivity::class.java.name)
    }

    val appInfoIntent = KIntent {
        hasComponent(MoreOptionsActivity::class.java.name)
    }

    val settingsIntent = KIntent {
        hasComponent(SettingsActivity::class.java.name)
    }

    @Test
    fun loginButtonShouldOpenLoginMethod() = run {
        val intent = EmbarkActivity.newInstance(context(), this.javaClass.name)
        activityRule.launch(intent)

        TextActionScreen {
            authIntent { stub() }

            openActionBarOverflowOrOptionsMenu(context())

            onView(withText("Log in"))
                .perform(click())

            authIntent { intended() }
        }
    }

    @Test
    fun restartButtonShouldReloadEmbark() = run {
        val intent = EmbarkActivity.newInstance(context(), this.javaClass.name)
        activityRule.launch(intent)

        TextActionScreen {

            step("Enter text for first passage and continue") {
                input { edit { typeText("Test entry") } }
                submit { click() }
            }

            step("Open overflow menu, press restart and accept the dialog") {
                openActionBarOverflowOrOptionsMenu(context())
                onView(withText("Restart questions"))
                    .perform(click())
                onView(withText("OK"))
                    .perform(click())
            }

            step("Check that the first message (from first passage) is displayed") {
                Screen.onScreen<EmbarkScreen> {
                    messages { firstChild<EmbarkScreen.MessageRow> { text { hasText("test message") } } }
                }
            }
        }
    }

    @Test
    fun appInfoButtonShouldStartMoreOptionsActivity() = run {
        val intent = EmbarkActivity.newInstance(context(), this.javaClass.name)
        activityRule.launch(intent)

        TextActionScreen {
            appInfoIntent { stub() }

            openActionBarOverflowOrOptionsMenu(context())
            onView(withText("App information"))
                .perform(click())

            appInfoIntent { intended() }
        }
    }

    @Test
    fun settingsButtonShouldStartMoreOptionsActivity() = run {
        val intent = EmbarkActivity.newInstance(context(), this.javaClass.name)
        activityRule.launch(intent)

        TextActionScreen {
            settingsIntent { stub() }

            openActionBarOverflowOrOptionsMenu(context())
            onView(withText("App settings"))
                .perform(click())

            settingsIntent { intended() }
        }
    }
}
