package com.hedvig.app.util.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

/**
 * While this composable is displayed the screen stays awake. Clears this behavior when it leaves composition.
 */
@Composable
fun AwakeScreen(content: @Composable () -> Unit) {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = view.context.findWindow()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    content()
}

// https://github.com/google/accompanist/blob/175fee33ac9351cb10a750a46cfc43ea4e4d962c/systemuicontroller/src/main/java/com/google/accompanist/systemuicontroller/SystemUiController.kt#L263-L270
private fun Context.findWindow(): Window? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context.window
        context = context.baseContext
    }
    return null
}
