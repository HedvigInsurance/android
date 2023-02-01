package com.hedvig.app.util.extensions

import android.app.Activity
import android.app.AlertDialog
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
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.play.core.review.ReviewManagerFactory
import com.hedvig.app.authenticate.BankIdLoginDialog
import com.hedvig.app.util.extensions.view.setupToolbar
import slimber.log.e

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
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    android.Manifest.permission.READ_EXTERNAL_STORAGE,
    ->
      showAlert(
        title = hedvig.resources.R.string.PERMISSION_DIALOG_TITLE,
        message = hedvig.resources.R.string.PERMISSION_DIALOG_EXTERNAL_STORAGE_MESSAGE,
        positiveAction = { openAppSettings() },
      )
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
      e { "No dialog for permission $permission!" }
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
    e { "No email app found" }
  }
}

private fun List<ResolveInfo>.toLabeledIntentArray(packageManager: PackageManager): Array<LabeledIntent> = map {
  val packageName = it.activityInfo.packageName
  val intent = packageManager.getLaunchIntentForPackage(packageName)
  LabeledIntent(intent, packageName, it.loadLabel(packageManager), it.icon)
}.toTypedArray()

fun AppCompatActivity.handleSingleSelectLink(
  value: String,
  onLinkHandleFailure: () -> Unit,
) = when (value) {
  "message.forslag.dashboard" -> {
    e { "Can't handle going to the offer page without a QuoteCartId from link: `$value`" }
    AlertDialog.Builder(this)
      .setTitle(com.adyen.checkout.dropin.R.string.error_dialog_title)
      .setMessage(getString(hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE))
      .setPositiveButton(com.adyen.checkout.dropin.R.string.error_dialog_button) { _, _ ->
        // no-op. Action handled by `setOnDismissListener`
      }
      .setOnDismissListener {
        onLinkHandleFailure()
      }
      .create()
      .show()
  }
  "message.bankid.start", "message.bankid.autostart.respond", "message.bankid.autostart.respond.two" -> {
    BankIdLoginDialog().show(supportFragmentManager, BankIdLoginDialog.TAG)
  }
  // bot-service is weird. it sends this when the user gets the option to go to `Hem`.
  // We simply dismiss the activity for now in this case
  "hedvig.com",
  "claim.done", "callme.phone.dashboard",
  -> {
    finish()
  }
  else -> {
    e { "Can't handle the link $value" }
  }
}

fun Activity.makeACall(uri: Uri) {
  val intent = Intent(Intent.ACTION_DIAL)
  intent.data = uri
  startActivity(intent)
}

fun Activity.showReviewDialog(
  onComplete: () -> Unit = {},
) {
  val manager = ReviewManagerFactory.create(this)
  val request = manager.requestReviewFlow()
  request.addOnCompleteListener { task ->
    if (task.isSuccessful) {
      val reviewInfo = task.result
      val flow = manager.launchReviewFlow(this, reviewInfo)
      flow.addOnCompleteListener {
        onComplete()
      }
    }
  }
}

fun Activity.openWebBrowser(uri: Uri) {
  val browserIntent = Intent(Intent.ACTION_VIEW, uri)

  if (browserIntent.resolveActivity(packageManager) != null) {
    startActivity(browserIntent)
  } else {
    e { "Tried to launch $uri but the phone has nothing to support such an intent." }
    makeToast(hedvig.resources.R.string.general_unknown_error)
  }
}
