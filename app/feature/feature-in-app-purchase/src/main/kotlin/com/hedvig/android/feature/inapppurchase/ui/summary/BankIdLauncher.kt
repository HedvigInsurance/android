package com.hedvig.android.feature.inapppurchase.ui.summary

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

@Stable
internal class BankIdLauncher(private val context: Context) {
  val canOpenBankId: Boolean = context.isBankIdInstalled()

  fun open(autoStartToken: String) {
    if (!canOpenBankId) return
    logcat(LogPriority.INFO) { "Opened BankID to sign an in-app purchase" }
    context.startActivity(
      Intent(Intent.ACTION_VIEW, "https://app.bankid.com/?autostarttoken=$autoStartToken&redirect=null".toUri()),
    )
  }
}

@Composable
internal fun rememberBankIdLauncher(): BankIdLauncher {
  val context = LocalContext.current
  return remember(context) { BankIdLauncher(context) }
}

private fun Context.isBankIdInstalled(): Boolean {
  return try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      packageManager.getPackageInfo(BankIdAppPackageName, PackageManager.PackageInfoFlags.of(0))
    } else {
      @Suppress("DEPRECATION")
      packageManager.getPackageInfo(BankIdAppPackageName, 0)
    }
    true
  } catch (e: PackageManager.NameNotFoundException) {
    logcat(LogPriority.WARN) { "BankID app is not installed: $e" }
    false
  }
}

private const val BankIdAppPackageName = "com.bankid.bus"
