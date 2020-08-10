package com.hedvig.app.util.extensions

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(crossinline binder: (View) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        binder(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    }
