package com.hedvig.app.util.extensions

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hedvig.app.util.safeLet
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

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

