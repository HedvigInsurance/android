package com.hedvig.android.ui.bankid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
interface BankIdState {
  val canOpenBankId: Boolean

  fun tryOpenBankId()
}

/**
 * Opens the BankID app on this device with [autoStartToken], when it is installed.
 */
@Composable
expect fun rememberBankIdState(autoStartToken: String): BankIdState
