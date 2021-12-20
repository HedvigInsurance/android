package com.hedvig.app.util.compose.preview

import com.hedvig.app.feature.claimdetail.model.ClaimDetailData
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.feature.home.ui.claimstatus.data.PillData
import com.hedvig.app.feature.offer.ui.OfferModel
import org.javamoney.moneta.Money
import java.util.UUID
import javax.money.CurrencyContext
import javax.money.CurrencyUnit

fun PillData.Companion.previewData(): List<PillData> {
    return PillData.PillType.values().dropLast(1).map { pillType ->
        PillData(pillType.name, pillType)
    }
}

fun ClaimProgressData.Companion.previewData(): List<ClaimProgressData> {
    return ClaimProgressData.ClaimProgressType.values().dropLast(1).map { progressType ->
        ClaimProgressData(progressType.name, progressType)
    }
}

fun ClaimDetailData.Companion.previewData(): List<ClaimDetailData> {
    return listOf(
        ClaimDetailData(
            claimType = "All-risk",
            submittedText = "30 min ago",
            closedText = "—",
            progress = ClaimProgressData.previewData(),
            statusParagraph = """
                |Your claim in being reviewed by one of our insurance specialists.
                | We'll get back to you soon with an update.
                """.trimMargin(),
        )
    )
}

fun OfferModel.InsurelyCard.Retrieved.Companion.previewData(): OfferModel.InsurelyCard.Retrieved {
    val fakeSekCurrency = object : CurrencyUnit {
        override fun compareTo(other: CurrencyUnit?): Int = 0
        override fun getCurrencyCode(): String = "SEK"
        override fun getNumericCode(): Int = 0
        override fun getDefaultFractionDigits(): Int = 0
        override fun getContext(): CurrencyContext? = null
    }
    return OfferModel.InsurelyCard.Retrieved(
        id = UUID.randomUUID().toString(),
        insuranceProviderDisplayName = "Some Insurance",
        currentInsurances = List(2) {
            OfferModel.InsurelyCard.Retrieved.CurrentInsurance(
                "SmthInsrnce",
                Money.of(
                    (it + 1) * 12,
                    fakeSekCurrency
                )
            )
        },
        savedWithHedvig = Money.of(19, fakeSekCurrency),
    )
}
