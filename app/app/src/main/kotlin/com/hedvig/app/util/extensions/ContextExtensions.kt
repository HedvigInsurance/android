package com.hedvig.app.util.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hedvig.android.core.common.android.SHARED_PREFERENCE_NAME
import com.hedvig.app.R
import com.hedvig.app.feature.chat.ui.ChatActivity
import kotlinx.coroutines.delay
import slimber.log.e
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

const val SHARED_PREFERENCE_ASKED_FOR_PERMISSION_PREFIX_KEY =
  "shared_preference_asked_for_permission_prefix"

fun Context.compatColor(@ColorRes color: Int) = ContextCompat.getColor(this, color)

@ColorInt
fun Context.colorAttr(
  @AttrRes color: Int,
  typedValue: TypedValue = TypedValue(),
  resolveRefs: Boolean = true,
): Int {
  theme.resolveAttribute(color, typedValue, resolveRefs)
  return typedValue.data
}

@DrawableRes
fun Context.drawableAttr(
  @AttrRes drawable: Int,
  typedValue: TypedValue = TypedValue(),
  resolveRefs: Boolean = true,
): Int {
  theme.resolveAttribute(drawable, typedValue, resolveRefs)
  return typedValue.resourceId
}

fun Context.compatDrawable(@DrawableRes drawable: Int) =
  AppCompatResources.getDrawable(this, drawable)

fun Context.hideKeyboard(view: View) {
  val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
  inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.showKeyboard(view: View) {
  if (view.requestFocus()) {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED)
  }
}

suspend fun Context.hideKeyboardWithDelay(inputView: View, delayDuration: Duration = 0.milliseconds) {
  val windowInsets = WindowInsetsCompat.toWindowInsetsCompat(inputView.rootView.rootWindowInsets)
  if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) {
    hideKeyboard(inputView)
    delay(delayDuration)
  }
}

suspend fun Context.showKeyboardWithDelay(inputView: View?, delayDuration: Duration = 0.milliseconds) {
  if (inputView == null) return
  val windowInsets = WindowInsetsCompat.toWindowInsetsCompat(inputView.rootView.rootWindowInsets)
  if (!windowInsets.isVisible(WindowInsetsCompat.Type.ime())) {
    delay(delayDuration)
    showKeyboard(inputView)
  }
}

private fun Context.getSharedPreferences() =
  this.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

/**
 * Note: This extension will not accept an Application Context
 */
fun Context.showAlert(
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

fun Context.showAlert(
  title: String,
  message: String? = null,
  @StringRes positiveLabel: Int? = android.R.string.ok,
  @StringRes negativeLabel: Int? = android.R.string.cancel,
  positiveAction: () -> Unit,
  negativeAction: (() -> Unit)? = null,
): androidx.appcompat.app.AlertDialog? =
  MaterialAlertDialogBuilder(this)
    .apply {
      setTitle(title)
      if (positiveLabel != null) {
        setPositiveButton(positiveLabel) { _, _ ->
          positiveAction()
        }
      }
      if (negativeLabel != null) {
        setNegativeButton(negativeLabel) { _, _ ->
          negativeAction?.let { it() }
        }
      }
      message?.let { setMessage(it) }
    }
    .show()

fun Context.showErrorDialog(message: String, positiveAction: () -> Unit) {
  MaterialAlertDialogBuilder(this)
    .setTitle(com.adyen.checkout.dropin.R.string.error_dialog_title)
    .setMessage(message)
    .setPositiveButton(hedvig.resources.R.string.ALERT_OK) { _, _ -> positiveAction() }
    .show()
}

fun Context.makeToast(
  @StringRes text: Int,
  length: Int = Toast.LENGTH_LONG,
) = makeToast(resources.getString(text), length)

fun Context.makeToast(
  text: String,
  length: Int = Toast.LENGTH_LONG,
) = Toast.makeText(this, text, length).show()

fun Context.openUri(uri: Uri) = startActivity(Intent(Intent.ACTION_VIEW, uri))

fun Context.getStoredBoolean(key: String): Boolean =
  getSharedPreferences().getBoolean(key, false)

fun Context.storeBoolean(key: String, value: Boolean): Boolean =
  getSharedPreferences().edit().putBoolean(key, value).commit()

fun Context.hasPermissions(vararg permissions: String): Boolean {
  for (permission in permissions) {
    if (ActivityCompat.checkSelfPermission(
        this,
        permission,
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      return false
    }
  }
  return true
}

fun Context.canOpenUri(uri: Uri) =
  Intent(Intent.ACTION_VIEW, uri).resolveActivity(packageManager) != null

tailrec fun Context?.getActivity(): Activity? = when (this) {
  is Activity -> this
  else -> (this as? ContextWrapper)?.baseContext?.getActivity()
}

fun Context.tryOpenUri(uri: Uri) {
  fun showError() {
    showAlert(
      title = com.adyen.checkout.dropin.R.string.error_dialog_title,
      message = com.adyen.checkout.dropin.R.string.component_error,
      positiveAction = {},
    )
  }

  if (canOpenUri(uri)) {
    try {
      openUri(uri)
    } catch (throwable: Throwable) {
      showError()
    }
  } else {
    showError()
  }
}

fun Context.startChat() {
  val intent = Intent(this, ChatActivity::class.java)

  val options =
    ActivityOptionsCompat.makeCustomAnimation(
      this,
      R.anim.chat_slide_up_in,
      R.anim.stay_in_place,
    )

  ActivityCompat.startActivity(this, intent, options.toBundle())
}

fun Context.openWebBrowser(uri: Uri) {
  val browserIntent = Intent(Intent.ACTION_VIEW, uri)

  if (browserIntent.resolveActivity(packageManager) != null) {
    startActivity(browserIntent)
  } else {
    e { "Tried to launch $uri but the phone has nothing to support such an intent." }
    makeToast(hedvig.resources.R.string.general_unknown_error)
  }
}
