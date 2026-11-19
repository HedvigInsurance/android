package com.hedvig.android.feature.login.swedishlogin

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

@Composable
internal actual fun rememberBankIdState(bankIdUri: Uri): BankIdState = DesktopBankIdState

/**
 * The desktop build offers only the QR path: it never hands the login over to a BankID app, so the member scans the
 * QR code with BankID on their phone.
 */
private object DesktopBankIdState : BankIdState {
  override val canOpenBankId: Boolean = false

  override fun tryOpenBankId() {
    logcat(LogPriority.INFO) { "BankID not found, showing QR code instead" }
  }
}
