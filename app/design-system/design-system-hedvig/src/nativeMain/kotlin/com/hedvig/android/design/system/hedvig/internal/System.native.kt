package com.hedvig.android.design.system.hedvig.internal

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.identityHashCode

@OptIn(ExperimentalNativeApi::class)
internal actual fun identityHashCode(instance: Any?): Int = instance.identityHashCode()
