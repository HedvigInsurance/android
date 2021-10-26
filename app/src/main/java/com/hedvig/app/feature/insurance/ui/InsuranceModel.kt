package com.hedvig.app.feature.insurance.ui

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.crossselling.ui.CrossSellData

sealed class InsuranceModel {
    object Header : InsuranceModel()

    data class Contract(
        val inner: InsuranceQuery.Contract
    ) : InsuranceModel()

    data class CrossSellHeader(
        val showNotificationBadge: Boolean = false
    ) : InsuranceModel()

    data class CrossSellCard(
        val inner: CrossSellData
    ) : InsuranceModel()

    object Error : InsuranceModel()

    object TerminatedContractsHeader : InsuranceModel()
    data class TerminatedContracts(val quantity: Int) : InsuranceModel()
}
