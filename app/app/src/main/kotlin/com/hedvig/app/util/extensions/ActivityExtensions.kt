package com.hedvig.app.util.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsCompat
import com.google.android.play.core.review.ReviewManagerFactory
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.app.util.extensions.view.setupToolbar

val Activity.screenWidth: Int
  get() = window.decorView.measuredWidth

val Activity.windowHeight: Int
  get() {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      val metrics = windowManager.currentWindowMetrics
      val insets = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
      metrics.bounds.height() - insets.bottom - insets.top
    } else {
      val view = window.decorView
      val insets = WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets, view)
        .getInsets(WindowInsetsCompat.Type.systemBars())
      resources.displayMetrics.heightPixels - insets.bottom - insets.top
    }
  }

fun AppCompatActivity.setupToolbar(
  @IdRes toolbar: Int,
  @DrawableRes icon: Int? = null,
  usingEdgeToEdge: Boolean = false,
  rootLayout: View? = null,
  backAction: (() -> Unit)?,
) {
  this.findViewById<Toolbar>(toolbar).setupToolbar(
    activity = this,
    usingEdgeToEdge = usingEdgeToEdge,
    icon = icon,
    rootLayout = rootLayout,
    backAction = backAction,
  )
}

fun Activity.showPermissionExplanationDialog(permission: String) {
  when (permission) {
    android.Manifest.permission.RECORD_AUDIO ->
      showAlert(
        title = hedvig.resources.R.string.PERMISSION_DIALOG_TITLE,
        message = hedvig.resources.R.string.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE,
        positiveAction = { openAppSettings() },
      )
    android.Manifest.permission.CAMERA ->
      showAlert(
        title = hedvig.resources.R.string.PERMISSION_DIALOG_TITLE,
        message = hedvig.resources.R.string.PERMISSION_DIALOG_CAMERA_MESSAGE,
        positiveAction = { openAppSettings() },
      )
    else -> {
      logcat(LogPriority.ERROR) { "No dialog for permission $permission!" }
    }
  }
}

private fun Activity.openAppSettings() {
  val intent = Intent()
  intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
  val uri = Uri.fromParts("package", packageName, null)
  intent.data = uri
  startActivity(intent)
}

fun Activity.openEmail(title: String) {
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

fun Context.composeContactSupportEmail(
  emailAddresses: List<String> = listOf(getString(hedvig.resources.R.string.GENERAL_EMAIL)),
  subject: String? = null,
) {
  val intent = Intent(Intent.ACTION_SENDTO).apply {
    data = Uri.parse("mailto:") // only email apps should handle this
    putExtra(Intent.EXTRA_EMAIL, emailAddresses.toTypedArray())
    putExtra(Intent.EXTRA_SUBJECT, subject)
  }
  if (intent.resolveActivity(packageManager) != null) {
    startActivity(intent)
  } else {
    logcat(LogPriority.ERROR) { "Failed to open email app through `composeEmail`" }
    makeToast(hedvig.resources.R.string.login_open_email_app_button)
  }
}

private fun List<ResolveInfo>.toLabeledIntentArray(packageManager: PackageManager): Array<LabeledIntent> = map {
  val packageName = it.activityInfo.packageName
  val intent = packageManager.getLaunchIntentForPackage(packageName)
  LabeledIntent(intent, packageName, it.loadLabel(packageManager), it.icon)
}.toTypedArray()

fun Activity.showReviewDialog() {
  val manager = ReviewManagerFactory.create(this)
  val request = manager.requestReviewFlow()
  request.addOnCompleteListener { task ->
    if (task.isSuccessful) {
      val reviewInfo = task.result
      manager.launchReviewFlow(this, reviewInfo)
    }
  }
}
