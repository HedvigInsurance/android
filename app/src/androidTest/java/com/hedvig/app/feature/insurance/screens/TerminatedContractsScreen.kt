package com.hedvig.app.feature.insurance.screens

import com.hedvig.app.R
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen

class TerminatedContractsScreen : Screen<TerminatedContractsScreen>() {
    val recycler =
        KRecyclerView(
            { withId(R.id.recycler) },
            {
                itemType(InsuranceScreen::ContractCard)
                itemType(InsuranceScreen::Error)
            }
        )
}
