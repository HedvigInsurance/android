package com.hedvig.android.design.system.hedvig.a11y

import androidx.compose.runtime.Composable
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import hedvig.resources.Res
import hedvig.resources.TALKBACK_PER_MONTH
import hedvig.resources.TALKBACK_SWEDISH_KRONAS
import org.jetbrains.compose.resources.stringResource

@Composable
fun UiMoney?.getDescription(): String {
  return when (this?.currencyCode) {
    UiCurrencyCode.SEK -> this.amount.toString() + " " + stringResource(Res.string.TALKBACK_SWEDISH_KRONAS)
    UiCurrencyCode.DKK -> this.amount.toString()
    UiCurrencyCode.NOK -> this.amount.toString()
    null -> ""
  }
}

@Composable
fun UiMoney?.getPerMonthDescription(): String {
  return stringResource(Res.string.TALKBACK_PER_MONTH, this.getDescription())
}
