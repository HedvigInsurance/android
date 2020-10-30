package com.hedvig.app.feature.insurance.ui.detail.coverage

import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.android.owldroid.type.TypeOfContract

sealed class CoverageModel {
    sealed class Header : CoverageModel() {
        data class Perils(val typeOfContract: TypeOfContract) : Header()
        object InsurableLimits : Header()
    }

    data class Peril(val inner: PerilFragment) : CoverageModel()
    data class InsurableLimit(val inner: InsurableLimitsFragment) : CoverageModel()
}
