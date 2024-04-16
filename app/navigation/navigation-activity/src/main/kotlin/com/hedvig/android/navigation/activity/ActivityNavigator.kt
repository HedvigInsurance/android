package com.hedvig.android.navigation.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import hedvig.resources.R

interface ActivityNavigator {
  fun openAppSettings(context: Context)

  fun tryOpenPlayStore(context: Context)
}

class ActivityNavigatorImpl(private val buildConfigApplicationId: String) : ActivityNavigator {
  override fun openAppSettings(context: Context) {
    val permissionActivity = Intent(
      Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
      Uri.parse("package:$buildConfigApplicationId"),
    )
    if (context.packageManager.resolveActivity(permissionActivity, 0) != null) {
      context.startActivity(permissionActivity)
      return
    }
    context.startActivity(Intent(Intent(Settings.ACTION_SETTINGS)))
  }

  override fun tryOpenPlayStore(context: Context) {
    context.tryOpenPlayStore()
  }
}

private fun Context.tryOpenPlayStore() {
  if (canOpenPlayStore()) {
    openPlayStore()
  } else {
    Toast.makeText(
      this,
      getString(R.string.TOAST_PLAY_STORE_MISSING_ON_DEVICE),
      Toast.LENGTH_LONG,
    ).show()
  }
}

@SuppressLint("QueryPermissionsNeeded")
private fun Context.canOpenPlayStore() = playStoreIntent().resolveActivity(packageManager) != null

private fun Context.playStoreIntent() = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))

private fun Context.openPlayStore() {
  val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
  intent.flags = (
    Intent.FLAG_ACTIVITY_NO_HISTORY
      or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
  )
  startActivity(intent)
}
