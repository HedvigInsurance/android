package com.hedvig.app.feature.embark.screens

import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionFragment
import com.kaspersky.kaspresso.screens.KScreen

object MultiActionScreen : KScreen<MultiActionScreen>() {
    override val layoutId = R.layout.number_action_fragment
    override val viewClass = MultiActionFragment::class.java

}
