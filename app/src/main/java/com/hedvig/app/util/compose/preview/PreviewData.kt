package com.hedvig.app.util.compose.preview

import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.claimdetail.model.ClaimDetailCardUiState
import com.hedvig.app.feature.claimdetail.model.ClaimDetailResult
import com.hedvig.app.feature.claimdetail.model.ClaimDetailUiState
import com.hedvig.app.feature.home.ui.claimstatus.data.PillUiState
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.ui.compose.composables.claimprogress.ClaimProgressUiState
import org.javamoney.moneta.CurrencyUnitBuilder
import org.javamoney.moneta.Money
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.money.CurrencyContext
import javax.money.CurrencyUnit
import javax.money.MonetaryAmount

fun PillUiState.Companion.previewList(): List<PillUiState> {
    return PillUiState.PillType.values().dropLast(1).map { pillType ->
        PillUiState(pillType.name, pillType)
    }
}

fun ClaimProgressUiState.Companion.previewList(): List<ClaimProgressUiState> {
    return ClaimProgressUiState.ClaimProgressType.values().dropLast(1).map { progressType ->
        ClaimProgressUiState(progressType.name, progressType)
    }
}

fun ClaimDetailUiState.Companion.previewData(): ClaimDetailUiState {
    return ClaimDetailUiState(
        claimType = "All-risk",
        insuranceType = "Contents Insurance",
        claimDetailResult = ClaimDetailResult.Closed.Paid(PreviewData.monetaryAmount(2_500)),
        submittedAt = Instant.now().minus(Duration.ofMinutes(30)),
        closedAt = null,
        claimDetailCard = ClaimDetailCardUiState(
            ClaimProgressUiState.previewList(),
            statusParagraph = """
                |Your claim in being reviewed by one of our insurance specialists.
                | We'll get back to you soon with an update.
                """.trimMargin(),
        ),
        signedAudioURL = null,
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
fun DanishAddress.Companion.previewData(): DanishAddress {
    return DanishAddress(
        address = "Asagården 20",
        id = "0a3f50bd-ea90-32b8-ef44-0003ba298018",
        postalCode = null,
        city = null,
        streetName = "Asagården",
        streetNumber = "20",
        floor = null,
        apartment = null,
    )
}

fun DanishAddress.Companion.previewList(): List<DanishAddress> {
    return listOf(
        DanishAddress(
            address = "Asagården 20, 2. tv, 7500 Holstebro",
            id = "0a3f50bd-ea90-32b8-e044-0003ba298018",
            postalCode = "7500",
            city = "Holstebro",
            streetName = "Asagården",
            streetNumber = "20",
            floor = "2",
            apartment = "tv",
        ),
        DanishAddress(
            address = "Asagården 20, 2. th, 7500 Holstebro",
            id = "0a3f50bd-ea8f-32b8-e044-0003ba298018",
            postalCode = "7500",
            city = "Holstebro",
            streetName = "Asagården",
            streetNumber = "20",
            floor = "2",
            apartment = "th",
        ),
        DanishAddress(
            address = "Asagården 21, 2. tv, 7500 Holstebro",
            id = "0a3f50bd-ea96-32b8-e044-0003ba298018",
            postalCode = "7500",
            city = "Holstebro",
            streetName = "Asagården",
            streetNumber = "21",
            floor = "2",
            apartment = "tv",
        ),
        DanishAddress(
            address = "Asagården 21, 2. th, 7500 Holstebro",
            id = "0a3f50bd-ea95-32b8-e044-0003ba298018",
            postalCode = "7500",
            city = "Holstebro",
            streetName = "Asagården",
            streetNumber = "21",
            floor = "2",
            apartment = "th",
        ),
        DanishAddress(
            address = "Asagården 22, 2. tv, 7500 Holstebro",
            id = "0a3f50bd-ea9c-32b8-e044-0003ba298018",
            postalCode = "7500",
            city = "Holstebro",
            streetName = "Asagården",
            streetNumber = "22",
            floor = "2",
            apartment = "tv",
        ),
        DanishAddress(
            address = "Asagården 22, 2. th, 7500 Holstebro",
            id = "0a3f50bd-ea9b-32b8-e044-0003ba298018",
            postalCode = "7500",
            city = "Holstebro",
            streetName = "Asagården",
            streetNumber = "22",
            floor = "2",
            apartment = "th",
        ),
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
