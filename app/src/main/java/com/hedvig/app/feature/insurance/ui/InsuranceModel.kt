package com.hedvig.app.feature.insurance.ui

import androidx.annotation.StringRes
import com.hedvig.android.owldroid.graphql.InsuranceQuery

sealed class InsuranceModel {
    object Header : InsuranceModel()

    data class Contract(
        val inner: InsuranceQuery.Contract
    ) : InsuranceModel()

    data class Upsell(
            @get:StringRes val title: Int,
            @get:StringRes val description: Int,
            @get:StringRes val ctaText: Int
    ) : InsuranceModel()

    object Error : InsuranceModel()

    object TerminatedContractsHeader : InsuranceModel()
    data class TerminatedContracts(val quantity: Int) : InsuranceModel()
}
