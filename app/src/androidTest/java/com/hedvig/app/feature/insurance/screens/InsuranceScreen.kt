package com.hedvig.app.feature.insurance.screens

import android.content.Intent
import android.view.View
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class InsuranceScreen : Screen<InsuranceScreen>() {
    val root =
        KRecyclerView({ withId(R.id.insuranceRecycler) }, {
            itemType(InsuranceScreen::InfoCardItem)
        })

    class InfoCardItem(parent: Matcher<View>) : KRecyclerItem<InfoCardItem>(parent) {
        val title = KTextView(parent) { withId(R.id.title) }
        val action = KButton(parent) { withId(R.id.action) }

        val renewalLink = KIntent {
            hasAction(Intent.ACTION_VIEW)
            hasData("https://www.example.com")
        }
    }
}
