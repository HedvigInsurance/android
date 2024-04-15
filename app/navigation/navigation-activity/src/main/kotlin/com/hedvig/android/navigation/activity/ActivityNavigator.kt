package com.hedvig.android.navigation.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.hedvig.android.core.common.android.tryOpenPlayStore

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
