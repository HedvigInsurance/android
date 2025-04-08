package com.hedvig.android.design.system.hedvig.a11y

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import hedvig.resources.R

@Composable
fun UiMoney?.getDescription(): String {
  return when (this?.currencyCode) {
    UiCurrencyCode.SEK -> this.amount.toInt().toString() + stringResource(R.string.TALKBACK_SWEDISH_KRONAS)
    UiCurrencyCode.DKK -> this.amount.toInt().toString()
    UiCurrencyCode.NOK -> this.amount.toInt().toString()
    null -> ""
  }
}
// todo: danish, norw?
