package com.hedvig.android.molecule.public

import app.cash.molecule.DisplayLinkClock
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

internal actual val mainDispatcher: CoroutineContext = Dispatchers.Main + DisplayLinkClock
