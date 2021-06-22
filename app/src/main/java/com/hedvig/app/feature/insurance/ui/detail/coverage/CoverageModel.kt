package com.hedvig.app.feature.insurance.ui.detail.coverage

import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem

data class CoverageViewState(
    val perilItems: List<CoverageModel>,
    val insurableLimitItems: List<InsurableLimitItem>,
)

sealed class CoverageModel {
    data class Header(val typeOfContract: TypeOfContract) : CoverageModel()

    data class Peril(val inner: PerilFragment) : CoverageModel()
}
