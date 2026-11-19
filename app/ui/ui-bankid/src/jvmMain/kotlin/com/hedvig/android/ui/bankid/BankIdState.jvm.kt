package com.hedvig.android.ui.bankid

import androidx.compose.runtime.Composable
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

@Composable
actual fun rememberBankIdState(autoStartToken: String): BankIdState = DesktopBankIdState

/**
 * The desktop build never hands signing over to a BankID app, so the member scans the QR code with BankID on their
 * phone.
 */
private object DesktopBankIdState : BankIdState {
  override val canOpenBankId: Boolean = false

  override fun tryOpenBankId() {
    logcat(LogPriority.INFO) { "BankID not found, showing QR code instead" }
  }
}
