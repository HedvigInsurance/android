package com.hedvig.app.util.extensions

import android.content.ComponentCallbacks
import org.koin.android.ext.android.getDefaultScope

inline fun <reified T : Any> ComponentCallbacks.injectAll(
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
) = lazy(mode) {
    getDefaultScope().getAll<T>().distinct()
}
