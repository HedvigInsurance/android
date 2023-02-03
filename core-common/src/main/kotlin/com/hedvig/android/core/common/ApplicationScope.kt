package com.hedvig.android.core.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

/**
 * A [CoroutineScope] which lives for the entire lifecycle of the application. Can be used to launch top-level actions
 * which need to out-live the current CoroutineScope, like even after a ViewModel would be killed.
 * Defaults to [MainScope] but can be provided with whatever [CoroutineScope] needed if constructed for tests.
 */
class ApplicationScope(scope: CoroutineScope = MainScope()) : CoroutineScope by scope
