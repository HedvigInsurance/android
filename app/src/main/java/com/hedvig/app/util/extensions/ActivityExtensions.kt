package com.hedvig.app.util.extensions

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.feature.chat.ChatActivity
import com.hedvig.app.feature.offer.OfferActivity
import com.hedvig.app.service.LoginStatusService.Companion.IS_VIEWING_OFFER
import com.hedvig.app.util.extensions.view.setupLargeTitle
import com.hedvig.app.util.hasNotch
import com.hedvig.app.util.whenApiVersion
import kotlinx.android.synthetic.main.app_bar.*
import timber.log.Timber

fun Activity.setLightNavigationBar() {
    window.navigationBarColor = compatColor(R.color.off_white)
    whenApiVersion(Build.VERSION_CODES.O) {
        val flags = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }
}

fun Activity.setDarkNavigationBar() {
    window.navigationBarColor = compatColor(R.color.black)
    whenApiVersion(Build.VERSION_CODES.O) {
        val flags = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = flags xor View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }
}

fun Activity.showStatusBar() {
    whenApiVersion(Build.VERSION_CODES.M) {
        if (hasNotch()) {
            val flags = window.decorView.systemUiVisibility
            window.decorView.systemUiVisibility = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = compatColor(R.color.off_white)
            return
        }
    }

    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}

fun Activity.hideStatusBar() {
    whenApiVersion(Build.VERSION_CODES.M) {
        if (hasNotch()) {
            val flags = window.decorView.systemUiVisibility
            window.decorView.systemUiVisibility = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = compatColor(R.color.black)
            return
        }
    }

    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}

fun Activity.setLightStatusBarText() {
    val flags = window.decorView.systemUiVisibility
    window.decorView.systemUiVisibility = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
}

val Activity.displayMetrics: DisplayMetrics
    get() {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        return metrics
    }

val Activity.screenWidth: Int
    get() = window.decorView.measuredWidth

fun AppCompatActivity.setupLargeTitle(
    @StringRes title: Int,
    @FontRes font: Int,
    @DrawableRes icon: Int? = null,
    @ColorInt backgroundColor: Int? = null,
    backAction: (() -> Unit)? = null
) {
    setupLargeTitle(
        getString(title),
        font,
        icon,
        backgroundColor,
        backAction
    )
}

fun AppCompatActivity.setupLargeTitle(
    title: String,
    @FontRes font: Int,
    @DrawableRes icon: Int? = null,
    @ColorInt backgroundColor: Int? = null,
    backAction: (() -> Unit)? = null
) {
    appBarLayout.setupLargeTitle(
        title,
        font,
        this,
        icon,
        backgroundColor,
        backAction
    )
}

val Activity.localBroadcastManager get() = androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)

fun Activity.startClosableChat(restartable: Boolean = false) {
    val intent = Intent(this, ChatActivity::class.java)
    intent.putExtra(ChatActivity.EXTRA_SHOW_CLOSE, true)

    if (restartable) {
        intent.putExtra(ChatActivity.EXTRA_SHOW_RESTART, true)
    }

    val options =
        ActivityOptionsCompat.makeCustomAnimation(this, R.anim.activity_slide_up_in, R.anim.stay_in_place)

    ActivityCompat.startActivity(this, intent, options.toBundle())
}

fun Activity.askForPermissions(permissions: Array<String>, requestCode: Int, shouldNotAskAction: (() -> Unit)? = null) {
    permissions.forEach {
        if (ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED) {
            when {
                !getStoredBoolean(SHARED_PREFERENCE_ASKED_FOR_PERMISSION_PREFIX_KEY + it) -> {
                    storeBoolean(SHARED_PREFERENCE_ASKED_FOR_PERMISSION_PREFIX_KEY + it, true)
                    ActivityCompat.requestPermissions(this, permissions, requestCode)
                }
                !ActivityCompat.shouldShowRequestPermissionRationale(this, it) -> {
                    shouldNotAskAction?.invoke()
                    showPermissionExplanationDialog(it)
                }
                else -> ActivityCompat.requestPermissions(this, permissions, requestCode)
            }
        }
    }
}

private fun Activity.showPermissionExplanationDialog(permission: String) {
    when (permission) {
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE ->
            showAlert(
                title = R.string.PERMISSION_DIALOG_TITLE,
                message = R.string.PERMISSION_DIALOG_EXTERNAL_STORAGE_MESSAGE,
                positiveAction = { openAppSettings() }
            )
        android.Manifest.permission.RECORD_AUDIO ->
            showAlert(
                title = R.string.PERMISSION_DIALOG_TITLE,
                message = R.string.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE,
                positiveAction = { openAppSettings() }
            )
        android.Manifest.permission.CAMERA ->
            showAlert(
                title = R.string.PERMISSION_DIALOG_TITLE,
                message = R.string.PERMISSION_DIALOG_CAMERA_MESSAGE,
                positiveAction = { openAppSettings() }
            )
        else -> {
            Timber.e("No dialog for permission $permission!")
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

fun AppCompatActivity.handleSingleSelectLink(value: String) = when (value) {
    "message.forslag.dashboard" -> {
        storeBoolean(IS_VIEWING_OFFER, true)
        startActivity(Intent(this, OfferActivity::class.java).also {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
    }
    "message.bankid.start" -> {
        AuthenticateDialog().show(supportFragmentManager, AuthenticateDialog.TAG)
    }
    else -> {
        Timber.e("Can't handle the link $value")
    }
}

fun Activity.canOpenUri(uri: Uri) = Intent(Intent.ACTION_VIEW, uri).resolveActivity(packageManager) != null
