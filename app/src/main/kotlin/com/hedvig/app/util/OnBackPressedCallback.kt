package com.hedvig.app.util

import androidx.activity.OnBackPressedCallback

inline fun onBackPressedCallback(enabled: Boolean = true, crossinline callback: () -> Unit) =
  object : OnBackPressedCallback(enabled) {
    override fun handleOnBackPressed() {
      callback()
    }
  }
