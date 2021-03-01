package com.hedvig.app.util.apollo

import android.content.Context
import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.getLocale
import org.javamoney.moneta.Money
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import javax.money.MonetaryAmount

fun defaultLocale(context: Context, marketManager: MarketManager) =
    when (getLocale(Language.fromSettings(context, marketManager.market).apply(context), marketManager.market).toString()) {
        "en_NO" -> Locale.EN_NO
        "nb_NO" -> Locale.NB_NO
        "sv_SE" -> Locale.SV_SE
        "en_SE" -> Locale.EN_SE
        "da_DK" -> Locale.DA_DK
        "en_DK" -> Locale.EN_DK
        else -> Locale.EN_SE
    }

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
