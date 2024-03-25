package com.hedvig.android.navigation.activity

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.hedvig.android.core.common.android.tryOpenPlayStore

interface ActivityNavigator {
  fun navigateToMarketingActivity()

  fun openAppSettings(context: Context)

  fun navigateToLoggedInScreen(context: Context, clearBackstack: Boolean = true)

  fun tryOpenPlayStore(context: Context)
}

class ActivityNavigatorImpl(
  private val application: Application,
  private val loggedOutActivityClass: Class<*>,
  private val buildConfigApplicationId: String,
  private val navigateToLoggedInActivity: Context.(clearBackstack: Boolean) -> Unit,
) : ActivityNavigator {
  override fun navigateToMarketingActivity() {
    application.startActivity(
      Intent(application, loggedOutActivityClass)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
    )
  }

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

  override fun navigateToLoggedInScreen(context: Context, clearBackstack: Boolean) {
    context.navigateToLoggedInActivity(clearBackstack)
  }

  override fun tryOpenPlayStore(context: Context) {
    context.tryOpenPlayStore()
  }
}
