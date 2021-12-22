package com.hedvig.app.util.compose.preview

import com.hedvig.app.feature.claimdetail.model.ClaimDetailsData
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.feature.home.ui.claimstatus.data.PillData
import com.hedvig.app.feature.offer.ui.OfferModel
import org.javamoney.moneta.CurrencyUnitBuilder
import org.javamoney.moneta.Money
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.money.CurrencyContext
import javax.money.CurrencyUnit
import javax.money.MonetaryAmount

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

fun ClaimDetailsData.Companion.previewData(): ClaimDetailsData {
    return ClaimDetailsData(
        claimType = "All-risk",
        insuranceType = "Contents Insurance",
        claimResult = ClaimDetailsData.ClaimResult.Closed.Paid(PreviewData.monetaryAmount(2_500)),
        submittedAt = Instant.now().minus(Duration.ofMinutes(30)),
        closedAt = null,
        cardData = ClaimDetailsData.CardData(
            ClaimProgressData.previewData(),
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

/**
 * For classes we do not own, we can not add a Companion object on them to fake a static function of the class itself.
 * This object should serve as a workaround until(if) this is resolved https://youtrack.jetbrains.com/issue/KT-11968.
 */
object PreviewData {
    fun monetaryAmount(
        money: Number = 0,
        currencyText: String = "SEK",
    ): MonetaryAmount {
        return Money.of(
            money,
            CurrencyUnitBuilder.of(
                currencyText,
                "default"
            ).build()
        )
    }
}
