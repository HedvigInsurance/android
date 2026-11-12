package com.hedvig.android.feature.chat

import android.content.Context
import android.provider.Settings
import androidx.compose.ui.platform.AndroidUiDispatcher
import kotlin.coroutines.CoroutineContext

internal actual val chatViewModelDispatcher: CoroutineContext = AndroidUiDispatcher.Main

internal actual typealias ChatContext = Context

internal actual fun isAccessibilityEnabled(context: ChatContext): Boolean {
  val enabledServices = Settings.Secure.getString(
    context.contentResolver,
    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
  )
  val accessibilityEnabled =
    Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1
  return accessibilityEnabled && enabledServices != null
}
