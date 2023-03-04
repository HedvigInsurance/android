package com.hedvig.android.navigation.activity

import android.app.Application
import android.content.Context
import android.content.Intent

class Navigator(
  private val application: Application,
  private val loggedOutActivityClass: Class<*>,
  private val navigateToChat: Context.() -> Unit,
) {
  fun navigateToMarketingActivity() {
    application.startActivity(
      Intent(application, loggedOutActivityClass)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
    )
  }

  fun navigateToChat(context: Context) {
    context.navigateToChat()
  }
}
