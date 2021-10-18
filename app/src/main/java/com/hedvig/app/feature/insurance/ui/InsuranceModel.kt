package com.hedvig.app.feature.insurance.ui

import com.hedvig.android.owldroid.graphql.InsuranceQuery

sealed class InsuranceModel {
    object Header : InsuranceModel()

    data class Contract(
        val inner: InsuranceQuery.Contract
    ) : InsuranceModel()

    data class CrossSellHeader(
        val showNotificationBadge: Boolean = false
    ) : InsuranceModel()

    data class CrossSell(
        val title: String,
        val description: String,
        val callToAction: String,
        val action: Action,
        val backgroundUrl: String,
        val backgroundBlurHash: String,
        val typeOfContract: String
    ) : InsuranceModel() {
        sealed class Action {
            data class Embark(val embarkStoryId: String) : Action()
            object Chat : Action()
        }
    }

    object Error : InsuranceModel()

    object TerminatedContractsHeader : InsuranceModel()
    data class TerminatedContracts(val quantity: Int) : InsuranceModel()
}
