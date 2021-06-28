package com.hedvig.app.feature.offer.ui

import com.hedvig.app.feature.table.Table
import javax.money.MonetaryAmount

sealed class OfferModel {
    data class Header(
        val title: String?,
        val netMonthlyCost: MonetaryAmount,
        val grossMonthlyCost: MonetaryAmount,
        val incentiveDisplayValue: String?,
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

    sealed class Subheading : OfferModel() {
        object Coverage : Subheading()
    }

    data class QuoteDetails(
        val name: String,
        val id: String,
    ) : OfferModel()

    object Paragraph : OfferModel()
}
