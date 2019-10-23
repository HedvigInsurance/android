package com.hedvig.app.feature.ratings

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.openPlayStore() {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
    intent.flags = (
        Intent.FLAG_ACTIVITY_NO_HISTORY
            or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        )
    startActivity(intent)
}
