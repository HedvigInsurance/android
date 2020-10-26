package com.hedvig.app.feature.insurance.screens

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class InsuranceScreen : Screen<InsuranceScreen>() {
    val insuranceRecycler =
        KRecyclerView({ withId(R.id.insuranceRecycler) }, {
            itemType(InsuranceScreen::ContractCard)
        })

    class ContractCard(parent: Matcher<View>) : KRecyclerItem<ContractCard>(parent) {
        val contractName = KTextView(parent) { withId(R.id.contractName) }
        val firstStatusPill = KTextView(parent) { withId(R.id.firstStatusPill) }
        val secondStatusPill = KTextView(parent) { withId(R.id.secondStatusPill) }
        val contractPills = KRecyclerView(parent, { withId(R.id.contractPills) }, {
            itemType(InsuranceScreen::ContractPill)
        })
    }

    class ContractPill(parent: Matcher<View>) : KRecyclerItem<ContractPill>(parent) {
        val text = KTextView { withMatcher(parent) }
    }
}
