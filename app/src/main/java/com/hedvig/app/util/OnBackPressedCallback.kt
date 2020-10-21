package com.hedvig.app.util

import androidx.activity.OnBackPressedCallback

inline fun onBackPressedCallback(crossinline callback: () -> Unit, enabled: Boolean = true) =
    object : OnBackPressedCallback(enabled) {
        override fun handleOnBackPressed() {
            callback()
        }
    }
