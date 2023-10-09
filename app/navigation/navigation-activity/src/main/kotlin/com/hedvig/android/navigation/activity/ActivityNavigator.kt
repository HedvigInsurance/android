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

class ActivityNavigator(
  private val application: Application,
  private val loggedOutActivityClass: Class<*>,
  private val buildConfigApplicationId: String,
  private val navigateToEmbark: Context.(storyName: String, storyTitle: String) -> Unit,
  private val navigateToLoggedInActivity: Context.(clearBackstack: Boolean) -> Unit,
) {
  @SuppressLint("IntentWithNullActionLaunch")
  fun navigateToMarketingActivity() {
    application.startActivity(
      Intent(application, loggedOutActivityClass)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
    )
  }

  @Suppress("DEPRECATION")
  fun openAppSettings(context: Context) {
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

  fun navigateToEmbark(
    context: Context,
    storyName: String,
    storyTitle: String,
  ) {
    context.navigateToEmbark(storyName, storyTitle)
  }

  fun navigateToLoggedInScreen(
    context: Context,
    clearBackstack: Boolean = true,
  ) {
    context.navigateToLoggedInActivity(clearBackstack)
  }

  fun tryOpenPlayStore(context: Context) {
    context.tryOpenPlayStore()
  }

  @SuppressLint("QueryPermissionsNeeded")
  fun openWebsite(context: Context, uri: Uri) {
    val browserIntent = Intent(Intent.ACTION_VIEW, uri)

    if (browserIntent.resolveActivity(context.packageManager) != null) {
      context.startActivity(browserIntent)
    } else {
      logcat(LogPriority.WARN) { "Tried to launch $uri but the phone has nothing to support such an intent." }
    }
  }
}
