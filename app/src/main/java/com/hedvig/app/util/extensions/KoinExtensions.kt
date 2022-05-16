package com.hedvig.app.util.extensions

import android.content.ComponentCallbacks
import org.koin.android.ext.android.getKoin

inline fun <reified T : Any> ComponentCallbacks.injectAll(
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
) = lazy(mode) {
    getKoin().getAll<T>().distinct()
}
