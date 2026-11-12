package com.hedvig.android.feature.login.swedishlogin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.eygraber.uri.Uri

@Stable
internal interface BankIdState {
  val canOpenBankId: Boolean

  fun tryOpenBankId()
}

@Composable
internal expect fun rememberBankIdState(bankIdUri: Uri): BankIdState
