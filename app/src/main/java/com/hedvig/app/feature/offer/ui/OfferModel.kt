package com.hedvig.app.feature.offer.ui

import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.table.Table
import javax.money.MonetaryAmount

sealed class OfferModel {

    data class Header(
        val title: String?,
        val netMonthlyCost: MonetaryAmount,
        val grossMonthlyCost: MonetaryAmount,
        val incentiveDisplayValue: String?,
        val changeDateBottomSheetData: ChangeDateBottomSheetData
    ) : OfferModel()

    data class Facts(
        val table: Table,
    ) : OfferModel()

    data class Switcher(
        val displayName: String?,
    ) : OfferModel()

    data class Footer(
        val url: String,
    ) : OfferModel()

    object Loading : OfferModel()
}
