package com.hedvig.android.app.notification

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import com.hedvig.android.core.buildconstants.HedvigBuildConstants

// Used to refer to the MainActivity class fully qualified name when trying to make a notification deep link to
// specific screen of it
private const val MainActivityFullyQualifiedName = "com.hedvig.android.app.MainActivity"

/**
 * [null] [deepLinkUri] just opens the app without deep linking anywhere
 */
fun HedvigBuildConstants.intentForNotification(deepLinkUri: Uri?): Intent = Intent().apply {
  data = deepLinkUri
  component = ComponentName(this@intentForNotification.appId, MainActivityFullyQualifiedName)
}

const val DATA_MESSAGE_TITLE = "DATA_MESSAGE_TITLE"
const val DATA_MESSAGE_BODY = "DATA_MESSAGE_BODY"
