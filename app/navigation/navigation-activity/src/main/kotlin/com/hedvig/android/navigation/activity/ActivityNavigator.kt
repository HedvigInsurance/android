package com.hedvig.android.navigation.activity

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.hedvig.android.core.common.android.tryOpenPlayStore
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

interface ActivityNavigator {
  fun navigateToMarketingActivity()

  fun openAppSettings(context: Context)

  fun navigateToLoggedInScreen(context: Context, clearBackstack: Boolean = true)

  fun tryOpenPlayStore(context: Context)

  fun openWebsite(context: Context, uri: Uri)
}

class ActivityNavigatorImpl(
  private val application: Application,
  private val loggedOutActivityClass: Class<*>,
  private val buildConfigApplicationId: String,
  private val navigateToLoggedInActivity: Context.(clearBackstack: Boolean) -> Unit,
) : ActivityNavigator {
  @SuppressLint("IntentWithNullActionLaunch")
  override fun navigateToMarketingActivity() {
    application.startActivity(
      Intent(application, loggedOutActivityClass)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
    )
  }

  @Suppress("DEPRECATION")
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

  @SuppressLint("QueryPermissionsNeeded")
  override fun openWebsite(context: Context, uri: Uri) {
    val browserIntent = Intent(Intent.ACTION_VIEW, uri)

    if (browserIntent.resolveActivity(context.packageManager) != null) {
      context.startActivity(browserIntent)
    } else {
      logcat(LogPriority.ERROR) { "Tried to launch $uri but the phone has nothing to support such an intent." }
    }
  }
}
