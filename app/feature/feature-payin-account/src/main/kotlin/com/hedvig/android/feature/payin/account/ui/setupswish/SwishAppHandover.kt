package com.hedvig.android.feature.payin.account.ui.setupswish

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

/**
 * Hands the member over to the Swish app to approve a payin setup, following
 * https://developer.swish.nu/documentation/guides/trigger-the-swish-app.
 *
 * [isSwishInstalled] decides whether the approval screen offers to open Swish at all. [open] is safe
 * to call either way: with no Swish app on the device it falls back to viewing the url.
 */
@Stable
internal interface SwishAppHandover {
  val isSwishInstalled: Boolean

  fun open(approvalUrl: String)
}

/**
 * @param allowSandboxApp whether the Swish sandbox app counts as installed. Only it can approve the
 *   orders the staging backend issues, so builds pointing there accept it in place of the real app.
 */
@Composable
internal fun rememberSwishAppHandover(allowSandboxApp: Boolean): SwishAppHandover {
  val context = LocalContext.current
  return remember(context, allowSandboxApp) {
    SwishAppHandoverImpl(context, allowSandboxApp)
  }
}

@Stable
private class SwishAppHandoverImpl(
  private val context: Context,
  allowSandboxApp: Boolean,
) : SwishAppHandover {
  private val installedPackage: String? = listOfNotNull(
    SwishPackageName,
    SwishSandboxPackageName.takeIf { allowSandboxApp },
  ).firstOrNull { context.isPackageInstalled(it) }.also {
    logcat { "Resolved Swish app for payin approval: ${it ?: "none installed"}" }
  }

  override val isSwishInstalled: Boolean
    get() = installedPackage != null

  override fun open(approvalUrl: String) {
    val uri = approvalUrl.toUri()
    // The url comes from the backend and carries the order's token, so only a Swish or https link is
    // ever launched, and never through the app's own url handling, which may attach member credentials.
    if (uri.scheme !in AllowedSchemes) {
      logcat(LogPriority.WARN) { "Refusing Swish approval url with scheme ${uri.scheme}" }
      return
    }
    // Addressing the intent at Swish keeps the approval out of a browser or an app chooser.
    val packageName = installedPackage
    if (packageName != null && startView(uri, packageName)) return
    startView(uri, packageName = null)
  }

  private fun startView(uri: Uri, packageName: String?): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, uri)
      .addCategory(Intent.CATEGORY_BROWSABLE)
      .setPackage(packageName)
    return try {
      context.startActivity(intent)
      true
    } catch (e: ActivityNotFoundException) {
      logcat(LogPriority.WARN, e) { "${packageName ?: "No app"} would not take the Swish approval url" }
      false
    }
  }
}

private fun Context.isPackageInstalled(packageName: String): Boolean {
  return try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
      @Suppress("DEPRECATION")
      packageManager.getPackageInfo(packageName, 0)
    }
    true
  } catch (e: PackageManager.NameNotFoundException) {
    false
  }
}

private const val SwishPackageName = "se.bankgirot.swish"
private const val SwishSandboxPackageName = "se.bankgirot.swish.sandbox"
private val AllowedSchemes = setOf("swish", "https")
