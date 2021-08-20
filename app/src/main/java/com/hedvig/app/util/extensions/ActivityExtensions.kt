package com.hedvig.app.util.extensions

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.play.core.review.ReviewManagerFactory
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.util.extensions.view.setupToolbar
import e

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
        backAction = backAction
    )
}

fun Activity.startClosableChat(restartable: Boolean = false) {
    val intent = Intent(this, ChatActivity::class.java)
    intent.putExtra(ChatActivity.EXTRA_SHOW_CLOSE, true)

    if (restartable) {
        intent.putExtra(ChatActivity.EXTRA_SHOW_RESTART, true)
    }

    val options =
        ActivityOptionsCompat.makeCustomAnimation(
            this,
            R.anim.activity_slide_up_in,
            R.anim.stay_in_place
        )

    ActivityCompat.startActivity(this, intent, options.toBundle())
}

fun Activity.askForPermissions(
    permissions: Array<String>,
    requestCode: Int,
    shouldNotAskAction: (() -> Unit)? = null,
) {
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
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        ->
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

fun AppCompatActivity.handleSingleSelectLink(value: String) = when (value) {
    "message.forslag.dashboard" -> {
        startActivity(
            OfferActivity.newInstance(
                context = this,
                quoteIds = emptyList(),
                shouldShowOnNextAppStart = true
            ).also {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
    }
    "message.bankid.start", "message.bankid.autostart.respond", "message.bankid.autostart.respond.two" -> {
        AuthenticateDialog().show(supportFragmentManager, AuthenticateDialog.TAG)
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
    onComplete: () -> Unit
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
