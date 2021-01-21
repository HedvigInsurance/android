package com.hedvig.app.feature.insurance.screens

import android.view.View
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.terminatedcontracts.TerminatedContractsActivity
import org.hamcrest.Matcher

class InsuranceScreen : Screen<InsuranceScreen>() {
    val insuranceRecycler =
        KRecyclerView(
            { withId(R.id.insuranceRecycler) },
            {
                itemType(::ContractCard)
                itemType(::Error)
                itemType(::TerminatedContractsHeader)
                itemType(::TerminatedContracts)
            }
        )

    class ContractCard(parent: Matcher<View>) : KRecyclerItem<ContractCard>(parent) {
        val contractName = KTextView(parent) { withId(R.id.contractName) }
        val firstStatusPill = KTextView(parent) { withId(R.id.firstStatusPill) }
        val secondStatusPill = KTextView(parent) { withId(R.id.secondStatusPill) }
        val contractPills = KRecyclerView(
            parent, { withId(R.id.contractPills) },
            {
                itemType(::ContractPill)
            }
        )

        class ContractPill(parent: Matcher<View>) : KRecyclerItem<ContractPill>(parent) {
            val text = KTextView { withMatcher(parent) }
        }
    }

    class Error(parent: Matcher<View>) : KRecyclerItem<Error>(parent) {
        val retry = KButton(parent) { withId(R.id.retry) }
    }

    class TerminatedContractsHeader(parent: Matcher<View>) :
        KRecyclerItem<TerminatedContractsHeader>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class TerminatedContracts(parent: Matcher<View>) : KRecyclerItem<TerminatedContracts>(parent) {
        val caption = KTextView(parent) { withId(R.id.caption) }
    }

    val terminatedContractsScreen = KIntent { hasComponent(TerminatedContractsActivity::class.java.name) }
}
