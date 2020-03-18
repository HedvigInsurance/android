package com.hedvig.app.util.extensions

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.hedvig.app.SplashActivity
import com.hedvig.app.feature.marketpicker.Market
import kotlin.system.exitProcess

private const val SHARED_PREFERENCE_NAME = "hedvig_shared_preference"
private const val SHARED_PREFERENCE_IS_LOGGED_IN = "shared_preference_is_logged_in"

private const val SHARED_PREFERENCE_AUTHENTICATION_TOKEN = "shared_preference_authentication_token"
const val SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN = "shared_preference_tried_migration_of_token"
const val SHARED_PREFERENCE_ASKED_FOR_PERMISSION_PREFIX_KEY =
    "shared_preference_asked_for_permission_prefix"

fun Context.compatColor(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun Context.compatFont(@FontRes font: Int) = ResourcesCompat.getFont(this, font)

fun Context.compatDrawable(@DrawableRes drawable: Int) =
    AppCompatResources.getDrawable(this, drawable)

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.triggerRestartActivity(activity: Class<*> = SplashActivity::class.java) {
    val startActivity = Intent(this, activity)
    startActivity.flags =
        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    val pendingIntentId = 56665 // Randomly chosen identifier, this number has no significance.
    val pendingIntent =
        PendingIntent.getActivity(
            this.applicationContext,
            pendingIntentId,
            startActivity,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    val mgr = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
    exitProcess(0)
}

fun Context.setAuthenticationToken(token: String?) =
    getSharedPreferences().edit().putString(SHARED_PREFERENCE_AUTHENTICATION_TOKEN, token).commit()

fun Context.getAuthenticationToken(): String? =
    getSharedPreferences().getString(SHARED_PREFERENCE_AUTHENTICATION_TOKEN, null)

fun Context.setIsLoggedIn(isLoggedIn: Boolean) =
    getSharedPreferences().edit().putBoolean(SHARED_PREFERENCE_IS_LOGGED_IN, isLoggedIn).commit()

fun Context.isLoggedIn(): Boolean =
    getSharedPreferences().getBoolean(SHARED_PREFERENCE_IS_LOGGED_IN, false)

fun Context.getMarket(): Market? {
    val marketId = getSharedPreferences().getInt(Market.MARKET_SHARED_PREF, -1)
    return if (marketId == -1) {
        null
    } else {
        Market.values()[marketId]
    }
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
        Intent.createChooser(intent, title)
    )
}

/**
 * Note: This extension will not accept an Application Context
 */
fun Context.showAlert(
    @StringRes title: Int,
    @StringRes message: Int? = null,
    @StringRes positiveLabel: Int = android.R.string.ok,
    @StringRes negativeLabel: Int = android.R.string.cancel,
    positiveAction: () -> Unit,
    negativeAction: (() -> Unit)? = null
): AlertDialog =
    AlertDialog
        .Builder(this)
        .setTitle(resources.getString(title))
        .setPositiveButton(resources.getString(positiveLabel)) { _, _ ->
            positiveAction()
        }
        .setNegativeButton(resources.getString(negativeLabel)) { _, _ ->
            negativeAction?.let { it() }
        }
        .apply {
            message?.let { setMessage(it) }
        }
        .show()
        .apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            getButton(AlertDialog.BUTTON_POSITIVE)?.isAllCaps = false
            getButton(AlertDialog.BUTTON_NEGATIVE)?.isAllCaps = false
        }

fun Context.copyToClipboard(
    text: String
) {
    (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
        ?.setPrimaryClip(ClipData.newPlainText(null, text))
}

fun Context.makeToast(
    @StringRes text: Int,
    length: Int = Toast.LENGTH_LONG
) = makeToast(resources.getString(text), length)

fun Context.makeToast(
    text: String,
    length: Int = Toast.LENGTH_LONG
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
                permission
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
