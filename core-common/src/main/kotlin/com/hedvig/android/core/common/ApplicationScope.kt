package com.hedvig.android.core.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

/**
 * A [CoroutineScope] which lives for the entire lifecycle of the application. Can be used to launch top-level actions
 * which need to out-live the current CoroutineScope, like even after a ViewModel would be killed.
 */
class ApplicationScope : CoroutineScope by MainScope()
