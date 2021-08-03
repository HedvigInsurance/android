package com.hedvig.app.feature.insurance.ui.detail.coverage

import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.perils.PerilItem

data class CoverageViewState(
    val perilItems: List<PerilItem>,
    val insurableLimitItems: List<InsurableLimitItem>,
)
