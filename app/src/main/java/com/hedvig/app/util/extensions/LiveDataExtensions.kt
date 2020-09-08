package com.hedvig.app.util.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

@Deprecated("Use the one from core-ktx instead", replaceWith = ReplaceWith("observe", "androidx.lifecycle.observe"))
inline fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, crossinline action: (t: T?) -> Unit) =
    observe(lifecycleOwner, Observer { action(it) })
