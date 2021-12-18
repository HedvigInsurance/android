package com.hedvig.app.util.compose.preview

import com.hedvig.app.feature.claimdetail.ClaimDetailParameter
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.feature.home.ui.claimstatus.data.PillData
import com.hedvig.app.feature.offer.ui.OfferModel
import org.javamoney.moneta.Money
import java.util.UUID
import javax.money.CurrencyContext
import javax.money.CurrencyUnit
import java.time.Duration
import java.time.Instant

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

fun ClaimDetailParameter.Companion.previewData() = ClaimDetailParameter(
    claimType = "Insurance case",
    submittedAt = Instant.now().minus(Duration.ofMinutes(1)),
    closedAt = null,
    progressSegments = ClaimProgressData.previewData(),
    statusParagraph = "We have received your claim and will start reviewing it soon.",
)

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
