package com.hedvig.app.util.extensions

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Typeface
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hedvig.android.core.common.preferences.PreferenceKey
import com.hedvig.android.market.Language
import com.hedvig.app.R
import com.hedvig.app.SplashActivity
import com.hedvig.app.feature.chat.ui.ChatActivity
import kotlinx.coroutines.delay
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val SHARED_PREFERENCE_NAME = "hedvig_shared_preference"

const val SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN = "shared_preference_tried_migration_of_token"
const val SHARED_PREFERENCE_ASKED_FOR_PERMISSION_PREFIX_KEY =
  "shared_preference_asked_for_permission_prefix"
private const val SHARED_PREFERENCE_LAST_OPEN = "shared_preference_last_open"

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

fun Context.compatFont(@FontRes font: Int) = ResourcesCompat.getFont(this, font)

fun Context.fontAttr(
  @AttrRes font: Int,
  typedValue: TypedValue = TypedValue(),
  resolveRefs: Boolean = true,
): Typeface? {
  theme.resolveAttribute(font, typedValue, resolveRefs)
  return ResourcesCompat.getFont(this, typedValue.resourceId)
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

suspend fun Context.showKeyboardWithDelay(inputView: View, delayDuration: Duration = 0.milliseconds) {
  val windowInsets = WindowInsetsCompat.toWindowInsetsCompat(inputView.rootView.rootWindowInsets)
  if (!windowInsets.isVisible(WindowInsetsCompat.Type.ime())) {
    delay(delayDuration)
    showKeyboard(inputView)
  }
}

fun Context.triggerRestartActivity(activity: Class<*> = SplashActivity::class.java) {
  val startActivity = Intent(this, activity)
  startActivity.flags =
    Intent.FLAG_ACTIVITY_NEW_TASK or
    Intent.FLAG_ACTIVITY_CLEAR_TASK or
    Intent.FLAG_ACTIVITY_CLEAR_TOP or
    Intent.FLAG_ACTIVITY_SINGLE_TOP
  val pendingIntentId = 56665 // Randomly chosen identifier, this number has no significance.
  val pendingIntent =
    PendingIntent.getActivity(
      this.applicationContext,
      pendingIntentId,
      startActivity,
      PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  val mgr = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
  mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
  exitProcess(0)
}

fun Context.setLastOpen(date: Long) =
  getSharedPreferences().edit().putLong(SHARED_PREFERENCE_LAST_OPEN, date).commit()

fun Context.getLastOpen() =
  getSharedPreferences().getLong(SHARED_PREFERENCE_LAST_OPEN, 0)

fun Context.getLanguage(): Language? {
  val pref = PreferenceManager.getDefaultSharedPreferences(this)
  val language = pref.getString(PreferenceKey.SETTING_LANGUAGE, null)
  return language?.let { Language.from(it) }
}

private fun Context.getSharedPreferences() =
  this.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

fun Context.showShareSheet(@StringRes title: Int, configureClosure: ((Intent) -> Unit)?) =
  showShareSheet(resources.getString(title), configureClosure)

fun Context.showShareSheet(title: String, configureClosure: ((Intent) -> Unit)?) {
  val intent = Intent().apply {
    action = Intent.ACTION_SEND
  }

  configureClosure?.let { it(intent) }
  startActivity(
    Intent.createChooser(intent, title),
  )
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

fun Context.copyToClipboard(
  text: String,
) {
  getSystemService<ClipboardManager>()?.setPrimaryClip(ClipData.newPlainText(null, text))
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

val Context.isDarkThemeActive: Boolean
  get() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

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

fun Context.startChat(
  closable: Boolean = true,
) {
  val intent = Intent(this, ChatActivity::class.java)
  intent.putExtra(ChatActivity.EXTRA_SHOW_CLOSE, closable)

  val options =
    ActivityOptionsCompat.makeCustomAnimation(
      this,
      R.anim.chat_slide_up_in,
      R.anim.stay_in_place,
    )

  ActivityCompat.startActivity(this, intent, options.toBundle())
}
