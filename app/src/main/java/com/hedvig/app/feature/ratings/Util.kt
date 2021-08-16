package com.hedvig.app.feature.ratings

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.hedvig.app.R
import com.hedvig.app.util.extensions.makeToast

private fun Context.playStoreIntent() = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))

fun Context.tryOpenPlayStore() {
    if (canOpenPlayStore()) {
        openPlayStore()
    } else {
        makeToast(R.string.TOAST_PLAY_STORE_MISSING_ON_DEVICE)
    }
}

private fun Context.canOpenPlayStore() = playStoreIntent().resolveActivity(packageManager) != null

private fun Context.openPlayStore() {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
    intent.flags = (
        Intent.FLAG_ACTIVITY_NO_HISTORY
            or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        )
    startActivity(intent)
}
