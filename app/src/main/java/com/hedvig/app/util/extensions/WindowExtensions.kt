package com.hedvig.app.util.extensions

import android.view.Window
import androidx.core.view.WindowCompat

fun Window.compatSetDecorFitsSystemWindows(fits: Boolean) = WindowCompat.setDecorFitsSystemWindows(this, fits)

