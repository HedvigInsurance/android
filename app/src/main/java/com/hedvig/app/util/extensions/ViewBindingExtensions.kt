package com.hedvig.app.util.extensions

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(crossinline binder: (View) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        binder(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    }

inline fun <T : ViewBinding> RecyclerView.ViewHolder.viewBinding(crossinline binder: (View) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        binder(itemView)
    }

inline fun <T : ViewBinding> ViewGroup.viewBinding(crossinline binder: (View) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        binder(this)
    }
