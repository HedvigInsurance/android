package com.hedvig.android.molecule.public

import app.cash.molecule.DisplayLinkClock
import kotlin.coroutines.CoroutineContext

internal actual val mainDispatcher: CoroutineContext = DisplayLinkClock
