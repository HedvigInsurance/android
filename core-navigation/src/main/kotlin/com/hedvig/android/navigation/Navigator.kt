package com.hedvig.android.navigation

import android.content.Context

class Navigator(
  private val navigateToChat: Context.() -> Unit,
) {
  fun navigateToChat(context: Context) {
    context.navigateToChat()
  }
}
