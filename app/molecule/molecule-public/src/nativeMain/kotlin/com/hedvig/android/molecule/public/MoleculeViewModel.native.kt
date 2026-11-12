package com.hedvig.android.molecule.public

import app.cash.molecule.DisplayLinkClock
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

actual val mainDispatcher: CoroutineContext = Dispatchers.Main + DisplayLinkClock
