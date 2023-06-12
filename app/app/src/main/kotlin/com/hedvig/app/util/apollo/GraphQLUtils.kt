package com.hedvig.app.util.apollo

import android.icu.util.CurrencyAmount
import giraffe.fragment.MonetaryAmountFragment
import giraffe.type.Locale
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

fun MonetaryAmountFragment.format(locale: java.util.Locale, minimumDecimals: Int = 0): String {
  val currencyAmount = CurrencyAmount(this.amount.toDouble(), android.icu.util.Currency.getInstance(this.currency))
  return NumberFormat.getCurrencyInstance(locale).also {
    it.currency = currencyAmount.currency.toJavaCurrency()
    it.minimumFractionDigits = minimumDecimals
  }.format(currencyAmount.number)
}

fun MonetaryAmount.format(locale: java.util.Locale, minimumDecimals: Int = 0): String =
  NumberFormat.getCurrencyInstance(locale).also {
    it.currency = Currency.getInstance(currency.currencyCode)
    it.minimumFractionDigits = minimumDecimals
  }.format(this.number.numberValueExact(BigDecimal::class.java))
