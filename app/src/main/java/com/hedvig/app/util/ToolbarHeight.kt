package com.hedvig.app.util

import android.util.TypedValue
import androidx.fragment.app.Fragment


fun getToolbarBarHeight(context: Fragment): Int {
    val tv = TypedValue()
    return if (context.requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
        TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
    } else {
        0
    }
}
