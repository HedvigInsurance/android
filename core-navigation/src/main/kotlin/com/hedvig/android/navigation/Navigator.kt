package com.hedvig.android.navigation

import android.content.Context

class Navigator(
  private val navigateToMarketingActivity: Context.() -> Unit,
  private val navigateToChat: Context.() -> Unit,
) {
  fun navigateToMarketingActivity(context: Context) {
    context.navigateToMarketingActivity()
  }

  fun navigateToChat(context: Context) {
    context.navigateToChat()
  }
}
