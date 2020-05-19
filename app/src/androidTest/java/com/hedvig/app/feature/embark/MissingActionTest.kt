package com.hedvig.app.feature.embark

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.fragment.MessageFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.core.IsNot.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MissingActionTest {
    @get:Rule
    val activityRule = IntentsTestRule(EmbarkActivity::class.java, false, false)

    @Test
    fun shouldDisplayInformationAboutAppNeedingUpdateWhenActionCannotBeRendered() {
        MockWebServer().use { webServer ->
            webServer.start(8080)
            webServer.enqueue(MockResponse().setBody(DATA.toJson()))

            activityRule.launchActivity(INTENT_WITH_STORY_NAME)

            // Don't actually launch play store, that would be annoying.
            intending(not(isInternal())).respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK,
                    null
                )
            )

            onScreen<EmbarkScreen> {
                upgradeApp {
                    isVisible()
                    click()
                }
                playStoreIntent { intended() }
            }
        }
    }

    companion object {
        private val DATA = EmbarkStoryQuery.Data(
            embarkStory = EmbarkStoryQuery.EmbarkStory(
                startPassage = "1",
                passages = listOf(
                    EmbarkStoryQuery.Passage(
                        name = "TestPassage",
                        id = "1",
                        messages = listOf(
                            EmbarkStoryQuery.Message(
                                fragments = EmbarkStoryQuery.Message.Fragments(
                                    MessageFragment(
                                        text = "test message",
                                        expressions = emptyList()
                                    )
                                )
                            ),
                            EmbarkStoryQuery.Message(
                                fragments = EmbarkStoryQuery.Message.Fragments(
                                    MessageFragment(
                                        text = "123",
                                        expressions = emptyList()
                                    )
                                )
                            )
                        ),
                        response = EmbarkStoryQuery.Response(
                            fragments = EmbarkStoryQuery.Response.Fragments(
                                messageFragment = null
                            )
                        ),
                        action = EmbarkStoryQuery.Action(
                            asEmbarkSelectAction = null,
                            asEmbarkTextAction = null
                        ),
                        redirects = emptyList()
                    )
                )
            )
        )
        private val INTENT_WITH_STORY_NAME = Intent().apply {
            putExtra(EmbarkActivity.STORY_NAME, this@Companion::class.java.name)
        }
    }
}
