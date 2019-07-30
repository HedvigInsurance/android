package com.hedvig.app.util.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

inline fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, crossinline action: (t: T?) -> Unit) =
    observe(lifecycleOwner, Observer { action(it) })
