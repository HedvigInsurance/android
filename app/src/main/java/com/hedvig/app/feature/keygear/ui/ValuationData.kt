package com.hedvig.app.feature.keygear.ui

import android.os.Parcelable
import com.hedvig.app.feature.keygear.ValuationType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ValuationData(
    val purchasePrice: String,
    val valuationType: ValuationType,
    val ratio: Int,
    val valuationAmount: String?
) : Parcelable {
    companion object {
        fun from(
            purchasePrice: String,
            valuationType: ValuationType,
            ratio: Int,
            valuationAmount: String? = null
        ) = ValuationData(purchasePrice, valuationType, ratio, valuationAmount)
    }
}
