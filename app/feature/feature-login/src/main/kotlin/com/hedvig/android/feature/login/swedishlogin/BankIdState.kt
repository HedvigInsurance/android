package com.hedvig.android.feature.login.swedishlogin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

@Stable
internal interface BankIdState {
  val canOpenBankId: Boolean

  fun tryOpenBankId()
}

@Composable
internal fun rememberBankIdState(bankIdUri: Uri): BankIdState {
  val context = LocalContext.current
  return remember(context, bankIdUri) {
    BankIdStateImpl(bankIdUri, context).also {
      it.initialize()
    }
  }
}

@Stable
private class BankIdStateImpl(
  val bankIdUri: Uri,
  val context: Context,
) : BankIdState {
  override var canOpenBankId: Boolean by mutableStateOf(false)

  override fun tryOpenBankId() {
    if (!canOpenBankId) {
      logcat(LogPriority.INFO) { "BankID not found, showing QR code instead" }
      return
    }
    logcat(LogPriority.INFO) { "Opened BankID to handle login" }
    context.startActivity(Intent(Intent.ACTION_VIEW, bankIdUri))
  }

  fun initialize() {
    canOpenBankId = context.canBankIdAppHandleUri(bankIdUri).also {
      logcat { "Trying to resolve BankID app with bankIdUri:$bankIdUri | result: canOpenBankId=$it" }
    }
  }

  @SuppressLint("QueryPermissionsNeeded")
  private fun Context.canBankIdAppHandleUri(uri: Uri): Boolean {
    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(
          BankIdAppPackageName,
          PackageManager.PackageInfoFlags.of(0),
        )
      } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(BankIdAppPackageName, 0)
      }
      true
    } catch (e: PackageManager.NameNotFoundException) {
      logcat(LogPriority.WARN) {
        "Could not resolve BankID app with bankIdUri:$uri + exception: $e"
      }
      false
    }
  }
}

private const val BankIdAppPackageName = "com.bankid.bus"
