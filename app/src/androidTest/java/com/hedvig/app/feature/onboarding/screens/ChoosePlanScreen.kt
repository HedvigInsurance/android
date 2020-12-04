package com.hedvig.app.feature.onboarding.screens

import android.view.View
import com.agoda.kakao.check.KCheckBox
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class ChoosePlanScreen : Screen<ChoosePlanScreen>() {

    val recycler = KRecyclerView({ withId(R.id.recycler) },
        { itemType(ChoosePlanScreen::Card) })

    val continueButton = KButton { withId(R.id.continueButton) }

    class Card(parent: Matcher<View>) : KRecyclerItem<Card>(parent) {
        val radioButton = KCheckBox(parent) { withId(R.id.radioButton) }
        val title = KTextView(parent) { withId(R.id.name) }
        val discount = KTextView(parent) { withId(R.id.discount) }
        val description = KTextView(parent) { withId(R.id.description) }
    }
}
