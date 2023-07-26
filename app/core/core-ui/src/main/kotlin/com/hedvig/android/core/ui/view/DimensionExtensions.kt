package com.hedvig.android.core.ui.view

import android.content.res.Resources

// Formerly `Int.dp` but renamed to not conflict with compose's `Int.dp`
inline val Int.viewDps: Int
  get() = (this * Resources.getSystem().displayMetrics.density).toInt()

inline val Float.viewDps: Float
  get() = this * Resources.getSystem().displayMetrics.density
