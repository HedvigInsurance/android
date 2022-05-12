package com.hedvig.app.feature.onboarding.screens

import android.content.Intent
import android.view.View
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.hedvig.app.R
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.onboarding.ui.ChoosePlanActivity
import com.hedvig.app.testdata.feature.onboarding.builders.EmbarkStoryBuilder.Companion.ENGLISH_CONTENTS
import com.hedvig.app.testdata.feature.onboarding.builders.EmbarkStoryBuilder.Companion.ENGLISH_TRAVEL
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.check.KCheckBox
import io.github.kakaocup.kakao.intent.KIntent
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object ChoosePlanScreen : KScreen<ChoosePlanScreen>() {

    override val layoutId = R.layout.activity_choose_plan
    override val viewClass = ChoosePlanActivity::class.java

    val recycler = KRecyclerView(
        { withId(R.id.recycler) },
        { itemType(ChoosePlanScreen::Card) }
    )

    val continueButton = KButton { withId(R.id.continueButton) }

    val contents = KIntent {
        IntentMatchers.hasAction(Intent.ACTION_VIEW)
        hasComponent(EmbarkActivity::class.java.name)
        hasExtra(EmbarkActivity.STORY_NAME, ENGLISH_CONTENTS)
    }

    val travel = KIntent {
        hasComponent(EmbarkActivity::class.java.name)
        hasExtra(EmbarkActivity.STORY_NAME, ENGLISH_TRAVEL)
    }

    class Card(parent: Matcher<View>) : KRecyclerItem<Card>(parent) {
        val radioButton = KCheckBox(parent) { withId(R.id.radioButton) }
        val title = KTextView(parent) { withId(R.id.name) }
    }
}
