package com.hedvig.android.feature.chat.legacy

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

internal inline fun <T : ViewBinding> AppCompatActivity.viewBinding(crossinline binder: (View) -> T) =
  lazy(LazyThreadSafetyMode.NONE) {
    binder(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
  }

internal inline fun <T : ViewBinding> RecyclerView.ViewHolder.viewBinding(crossinline binder: (View) -> T) =
  lazy(LazyThreadSafetyMode.NONE) {
    binder(itemView)
  }

internal inline fun <T : ViewBinding> ViewGroup.viewBinding(crossinline binder: (View) -> T) =
  lazy(LazyThreadSafetyMode.NONE) {
    binder(this)
  }
