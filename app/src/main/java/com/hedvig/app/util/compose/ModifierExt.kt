package com.hedvig.app.util.compose

import android.view.KeyEvent
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.submitOnEnter(action: () -> Unit) = composed {
    val keyboardController = LocalSoftwareKeyboardController.current
    onKeyEvent {
        if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
            keyboardController?.hide()
            action()
            true
        } else {
            false
        }
    }
}
