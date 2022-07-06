package com.hedvig.app.util.apollo

import android.content.Context
import com.hedvig.android.owldroid.graphql.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.graphql.type.Locale
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.getLocale
import org.javamoney.moneta.Money
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import javax.money.MonetaryAmount

fun Locale.toLocaleString() = when (this) {
  Locale.en_SE -> "en_SE"
  Locale.sv_SE -> "sv_SE"
  Locale.en_NO -> "en_NO"
  Locale.nb_NO -> "nb_NO"
  Locale.da_DK -> "da_DK"
  Locale.en_DK -> "en_DK"
  Locale.UNKNOWN__ -> ""
}

fun Locale.toWebLocaleTag() = when (this) {
  Locale.en_SE -> "se-en"
  Locale.sv_SE -> "se"
  Locale.nb_NO -> "no"
  Locale.en_NO -> "no-en"
  Locale.da_DK -> "dk"
  Locale.en_DK -> "dk-en"
  Locale.UNKNOWN__ -> "se-en"
}

fun MonetaryAmountFragment.toMonetaryAmount(): MonetaryAmount =
  Money.of(amount.toBigDecimal(), currency)

fun MonetaryAmount.format(context: Context, market: Market?, minimumDecimals: Int = 0): String {
  val locale = getLocale(context, market)
  return format(locale, minimumDecimals)
}

fun MonetaryAmount.format(locale: java.util.Locale, minimumDecimals: Int = 0): String =
  NumberFormat.getCurrencyInstance(locale).also {
    it.currency = Currency.getInstance(currency.currencyCode)
    it.minimumFractionDigits = minimumDecimals
  }.format(this.number.numberValueExact(BigDecimal::class.java))

fun MonetaryAmount.formatOnlyNumber(
  locale: java.util.Locale,
  minimumDecimals: Int = 0,
  maximumDecimals: Int = minimumDecimals,
): String =
  NumberFormat.getNumberInstance(locale).also {
    it.currency = Currency.getInstance(currency.currencyCode)
    it.minimumFractionDigits = minimumDecimals
    it.maximumFractionDigits = maximumDecimals
  }.format(this.number.numberValueExact(BigDecimal::class.java))
