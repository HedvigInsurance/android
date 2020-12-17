package com.hedvig.app.feature.onboarding.screens

import android.content.Intent
import android.view.View
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.agoda.kakao.check.KCheckBox
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.onboarding.ui.ChoosePlanActivity
import com.kaspersky.kaspresso.screens.KScreen
import org.hamcrest.Matcher

object ChoosePlanScreen : KScreen<ChoosePlanScreen>() {

    override val layoutId = R.layout.activity_choose_plan
    override val viewClass = ChoosePlanActivity::class.java

    val recycler = KRecyclerView({ withId(R.id.recycler) },
        { itemType(ChoosePlanScreen::Card) })

    val continueButton = KButton { withId(R.id.continueButton) }

    val intent = KIntent {
        IntentMatchers.hasAction(Intent.ACTION_VIEW)
        hasExtras {
            hasEntry("WEB_PATH", "/no-en/new-member/travel")
        }
    }

    class Card(parent: Matcher<View>) : KRecyclerItem<Card>(parent) {
        val radioButton = KCheckBox(parent) { withId(R.id.radioButton) }
        val title = KTextView(parent) { withId(R.id.name) }
        val discount = KTextView(parent) { withId(R.id.discount) }
        val description = KTextView(parent) { withId(R.id.description) }
    }
}
