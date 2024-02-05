package com.hedvig.android.feature.connect.payment.trustly.webview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.JavascriptInterface

class TrustlyJavascriptInterface(
  private val activity: Activity,
) {
  @JavascriptInterface
  fun openURLScheme(packageName: String, URIScheme: String): Boolean {
    if (activity.isPackageInstalledAndEnabled(packageName)) {
      activity.startActivityForResult(
        Intent().apply {
          `package` = packageName
          action = Intent.ACTION_VIEW
          data = Uri.parse(URIScheme)
        },
        0,
      )
      return true
    }

    return false
  }

  companion object {
    const val NAME = "TrustlyAndroid"

    private fun Context.isPackageInstalledAndEnabled(packageName: String) = try {
      packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
      packageManager.getApplicationInfo(packageName, 0).enabled
    } catch (e: PackageManager.NameNotFoundException) {
      false
    }
  }
}
