package com.hedvig.app.feature.insurance.screens

import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.hedvig.app.R

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
