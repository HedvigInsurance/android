package com.hedvig.android.app.externalnavigator

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.activity.ExternalNavigator
import hedvig.resources.R

internal class ExternalNavigatorImpl(
  private val activity: Activity,
  private val buildConfigApplicationId: String,
) : ExternalNavigator {
  override fun openAppSettings() {
    val permissionActivity = Intent(
      Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
      Uri.parse("package:$buildConfigApplicationId"),
    )
    if (activity.packageManager.resolveActivity(permissionActivity, 0) != null) {
      activity.startActivity(permissionActivity)
      return
    }
    activity.startActivity(Intent(Intent(Settings.ACTION_SETTINGS)))
  }

  override fun tryOpenPlayStore() {
    activity.tryOpenPlayStore()
  }

  /**
   * Currently opens a selection sheet with the title "Select code" always.
   * Todo look for a way to just open the default email app without having to also start a draft email in the process
   */
  override fun openEmailApp() {
    activity.openEmail(activity.getString(R.string.login_bottom_sheet_view_code))
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

private fun Activity.openEmail(title: String) {
  val emailIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))

  val resInfo = packageManager.queryIntentActivities(emailIntent, 0)
  if (resInfo.isNotEmpty()) {
    // First create an intent with only the package name of the first registered email app
    // and build a picked based on it
    val intentChooser = packageManager.getLaunchIntentForPackage(
      resInfo.first().activityInfo.packageName,
    )
    val openInChooser = Intent.createChooser(intentChooser, title)

    try {
      // Then create a list of LabeledIntent for the rest of the registered email apps and add to the picker selection
      val emailApps = resInfo.toLabeledIntentArray(packageManager)
      openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, emailApps)
    } catch (_: NullPointerException) {
      // OnePlus crash prevention. Simply go with the initial email app found, don't give more options.
      // console.firebase.google.com/u/0/project/hedvig-app/crashlytics/app/android:com.hedvig.app/issues/06823149a4ff8a411f4508e0cbfae9f4
    }

    startActivity(openInChooser)
  } else {
    logcat(LogPriority.ERROR) { "No email app found" }
  }
}

private fun List<ResolveInfo>.toLabeledIntentArray(packageManager: PackageManager): Array<LabeledIntent> = map {
  val packageName = it.activityInfo.packageName
  val intent = packageManager.getLaunchIntentForPackage(packageName)
  LabeledIntent(intent, packageName, it.loadLabel(packageManager), it.icon)
}.toTypedArray()
