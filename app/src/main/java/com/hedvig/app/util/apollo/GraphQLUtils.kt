package com.hedvig.app.util.apollo

import android.content.Context
import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.getLocale
import org.javamoney.moneta.Money
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import javax.money.MonetaryAmount

fun Locale.toLocaleString() = when (this) {
    Locale.EN_SE -> "en_SE"
    Locale.SV_SE -> "sv_SE"
    Locale.EN_NO -> "en_NO"
    Locale.NB_NO -> "nb_NO"
    Locale.DA_DK -> "da_DK"
    Locale.EN_DK -> "en_DK"
    Locale.UNKNOWN__ -> ""
}

fun Locale.toWebLocaleTag() = when (this) {
    Locale.EN_SE -> "se-en"
    Locale.SV_SE -> "se"
    Locale.NB_NO -> "no"
    Locale.EN_NO -> "no-en"
    Locale.DA_DK -> "dk"
    Locale.EN_DK -> "dk-en"
    Locale.UNKNOWN__ -> "se-en"
}

fun MonetaryAmountFragment.toMonetaryAmount(): MonetaryAmount =
    Money.of(amount.toBigDecimal(), currency)

fun MonetaryAmount.format(context: Context, market: Market?, minimumDecimals: Int = 0): String =
    NumberFormat.getCurrencyInstance(getLocale(context, market)).also {
        it.currency = Currency.getInstance(currency.currencyCode)
        it.minimumFractionDigits = minimumDecimals
    }.format(this.number.numberValueExact(BigDecimal::class.java))

fun TypeOfContract.canHaveAddressChanged(): Boolean = when (this) {
    TypeOfContract.DK_ACCIDENT,
    TypeOfContract.DK_ACCIDENT_STUDENT,
    TypeOfContract.DK_HOME_CONTENT_OWN,
    TypeOfContract.DK_HOME_CONTENT_RENT,
    TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN,
    TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT,
    TypeOfContract.DK_TRAVEL,
    TypeOfContract.DK_TRAVEL_STUDENT,
    TypeOfContract.NO_HOME_CONTENT_OWN,
    TypeOfContract.NO_HOME_CONTENT_RENT,
    TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
    TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
    TypeOfContract.NO_TRAVEL,
    TypeOfContract.NO_TRAVEL_YOUTH,
    TypeOfContract.SE_ACCIDENT,
    TypeOfContract.SE_ACCIDENT_STUDENT -> false
    TypeOfContract.SE_APARTMENT_BRF,
    TypeOfContract.SE_APARTMENT_RENT,
    TypeOfContract.SE_APARTMENT_STUDENT_BRF,
    TypeOfContract.SE_APARTMENT_STUDENT_RENT,
    TypeOfContract.SE_HOUSE -> true
    TypeOfContract.UNKNOWN__ -> false
}
