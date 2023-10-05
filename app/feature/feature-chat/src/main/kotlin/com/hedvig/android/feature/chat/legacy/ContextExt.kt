package com.hedvig.android.feature.chat.legacy

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hedvig.android.core.common.android.SHARED_PREFERENCE_NAME
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

/**
 * Note: This extension will not accept an Application Context
 */
internal fun Context.showAlert(
  @StringRes title: Int,
  @StringRes message: Int? = null,
  @StringRes positiveLabel: Int? = android.R.string.ok,
  @StringRes negativeLabel: Int? = android.R.string.cancel,
  positiveAction: () -> Unit,
  negativeAction: (() -> Unit)? = null,
): androidx.appcompat.app.AlertDialog? =
  MaterialAlertDialogBuilder(this)
    .apply {
      setTitle(resources.getString(title))
      if (positiveLabel != null) {
        setPositiveButton(resources.getString(positiveLabel)) { _, _ ->
          positiveAction()
        }
      }
      if (negativeLabel != null) {
        setNegativeButton(resources.getString(negativeLabel)) { _, _ ->
          negativeAction?.let { it() }
        }
      }
      message?.let { setMessage(it) }
    }
    .show()

internal fun Context.composeContactSupportEmail(
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
    Toast.makeText(this, hedvig.resources.R.string.login_open_email_app_button, Toast.LENGTH_LONG).show()
  }
}

internal fun Context.openUri(uri: Uri) = startActivity(Intent(Intent.ACTION_VIEW, uri))

fun Context.getStoredBoolean(key: String): Boolean =
  getSharedPreferences().getBoolean(key, false)

internal fun Context.storeBoolean(key: String, value: Boolean): Boolean =
  getSharedPreferences().edit().putBoolean(key, value).commit()

private fun Context.getSharedPreferences() =
  this.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
