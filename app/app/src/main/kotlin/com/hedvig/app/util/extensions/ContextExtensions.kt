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
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

const val SHARED_PREFERENCE_ASKED_FOR_PERMISSION_PREFIX_KEY =
  "shared_preference_asked_for_permission_prefix"

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

fun Context.openUri(uri: Uri) = startActivity(Intent(Intent.ACTION_VIEW, uri))

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
